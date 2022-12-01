package com.example.demo;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.tomcat.util.buf.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);

        //Dummy data
        Map<String, PlaceholderData> data = new HashMap<>();
        data.put("EMP_DEPARTMENT", new PlaceholderData().
                setPlaceholderDataType(PlaceholderDataType.INTEGER).
                setValue("1111").
                setName("EMP_DEPARTMENT"));
        data.put("EMP_HIRE_DATE", new PlaceholderData().
                setPlaceholderDataType(PlaceholderDataType.DATE).
                setValue(LocalDateTime.of(2022, 7, 6, 14,20, 0).toString()).
                setName("EMP_HIRE_DATE"));
        data.put("EMP_ABCD", new PlaceholderData().
                setPlaceholderDataType(PlaceholderDataType.BOOLEAN).
                setValue("TRUE").
                setName("EMP_ABCD"));
        data.put("EMP_STRING", new PlaceholderData().
                setPlaceholderDataType(PlaceholderDataType.STRING).
                setValue("Jozef").
                setName("EMP_STRING"));
        List<Expression> expressionsList = new ArrayList<>();

        String inputExpression = "EMP_DEPARTMENT IN (1111,2222) AND EMP_HIRE_DATE EQ " + LocalDateTime.of(2022, 7, 6, 14,20, 0) + " and EMP_ABCD EQ TRUE AND EMP_STRING EQ Jozef";

        //Trim multiple spaces into one
        inputExpression = inputExpression.replaceAll(" +", " ");

        //split individual expressions by AND/OR keyword
        String[] splitInputExpression = inputExpression.split("\\b(?:AND |and |OR |or )\\b");
        List<String> words = Arrays.asList(inputExpression.split(" "));

        //queue and or operators for later joining to final expression
        Queue<String> operatorQueue = words.stream().
                filter(element -> element.equalsIgnoreCase("and") || element.equalsIgnoreCase("or")).
                map(String::toLowerCase).
                collect(Collectors.toCollection(PriorityQueue::new));


        //add individual expressions into expression array also check for
        for(String i : splitInputExpression) {
            String[] expressionElements = i.split("\\s+");
            if(checkParenthesis(expressionElements[2])) {
                fillExpressionListWhenSetPresent(data, expressionsList, i, expressionElements);
            } else {
                fillExpressionListWhenValuePresent(data, expressionsList, i, expressionElements);
            }
        }

        //sout individual expressions
        System.out.println(expressionsList.stream().
                map(object -> Objects.toString(object, null)).
                collect(Collectors.toList()));

        //evaluate individual expression and join into trivial expression e.g. (true AND false AND true) and return final boolean output
        System.out.println(evaluateIndividualToFinal(expressionsList, operatorQueue));

    }

    /**
     * Fills expressionList with objects used to evaluate expression, each object represents one expression with ONLY ONE Value (e.g. EMP_DEPARTMENT EQ 1111)
     *
     * @param data - values from placeholder service we are comparing
     * @param expressionsList - list of expression objects filled {@link Expression}, {@link Operator}, {@link PlaceholderDataType}
     * @param i - whole expression string (operand, operator, value)
     * @param expressionElements - 0 element - operand, 1 element - operator, 2 element - value
     */
    private static void fillExpressionListWhenValuePresent(Map<String, PlaceholderData> data, List<Expression> expressionsList, String i, String[] expressionElements) {
        Expression expression = new Expression(i,
                data.get(expressionElements[0]).getValue(),
                Operator.get(expressionElements[1].toLowerCase()).orElseThrow(() ->
                        new IllegalArgumentException("Operator: " + expressionElements[1] + " not found")),
                expressionElements[2],
                data.get(expressionElements[0]).getPlaceholderDataType(),
                new ArrayList<>());

        if(!validateExpressionObject(expression))
            throw new IllegalArgumentException("Error parsing values for expression: " + expression);

        expressionsList.add(expression);
    }

    /**
     * Fills expressionList with objects used to evaluate expression, each object represents one expression with SET of Values (e.g. EMP_DEPARTMENT IN (1111,2222))
     *
     * @param data - values from placeholder service we are comparing
     * @param expressionsList - list of expression objects filled {@link Expression}, {@link Operator}, {@link PlaceholderDataType}
     * @param i - whole expression string (operand, operator, values set)
     * @param expressionElements - 0 element - operand, 1 element - operator, 2 element - values set
     */
    private static void fillExpressionListWhenSetPresent(Map<String, PlaceholderData> data, List<Expression> expressionsList, String i, String[] expressionElements) {
        List<String> listValues = Arrays.asList(expressionElements[2].replaceAll("[{()}]", "").split(","));

        Expression expression = new Expression(i, data.get(expressionElements[0]).getValue(),
                Operator.get(expressionElements[1].toLowerCase()).orElseThrow(() ->
                        new IllegalArgumentException("Operator: " + expressionElements[1] + " not found")),
                "",
                data.get(expressionElements[0]).getPlaceholderDataType(),
                listValues);

        if(!validateExpressionObject(expression))
            throw new IllegalArgumentException("Error parsing values for expression: " + expression);

        expressionsList.add(expression);
    }

    /**
     * Evaluates individual expressions, joins them into trivial, joining with operator queue (e.g. true AND false AND true)
     * @param expressionsList - list filled with Expression data {@link Expression}
     * @param operatorQueue - queued operators from initial/input expression, used to join trivial expressions
     * @return -> final boolean output
     */
    private static boolean evaluateIndividualToFinal(List<Expression> expressionsList, Queue<String> operatorQueue) {
        StringBuilder sb = new StringBuilder();
        ExpressionParser expressionParser = new SpelExpressionParser();

        //evaluate individual expressions into trivial and join by operator queue
        for (Expression expression : expressionsList) {
            sb.append(evaluateExpression(expression)).append(" ");
            if(!operatorQueue.isEmpty())
                sb.append(operatorQueue.remove()).append(" ");
        }

        //evaluate final expression and get boolean value
        String finalExpression = Objects.requireNonNull(expressionParser.parseExpression(sb.toString()).getValue()).toString();
        return Boolean.parseBoolean(finalExpression);
    }



    private static boolean evaluateExpression(Expression expression) {
        switch (expression.getPlaceholderDataType()) {
            case STRING:
                return evaluateString(expression);
            case INTEGER:
                return evaluateInteger(expression);
            case DATE:
                return evaluateDate(expression);
            case BOOLEAN:
                return evaluateBoolean(expression);
            default:
                return false;
        }
    }

    private static boolean evaluateDate(Expression expression) {
        LocalDateTime operand = LocalDateTime.parse(expression.getOperand());
        LocalDateTime value = LocalDateTime.parse(expression.getValue());
        switch (expression.getOperator()) {
            case LESS_THAN:
                return operand.isBefore(value);
            case GREATER_THAN:
                return operand.isAfter(value);
            case LESS_THAN_EQUAL:
                return operand.isBefore(value) || operand.isEqual(value);
            case GREATER_THAN_EQUAL:
                return operand.isAfter(value) || operand.isEqual(value);
            case EQUALS:
                return operand.isEqual(value);
            case NOT_EQUALS:
                return !operand.isEqual(value);
            case IN:
                throw new IllegalArgumentException("\"IN\" operator not allowed comparing dates");
            default:
                return false;
        }
    }

    private static boolean evaluateString(Expression expression) {
        String operand = null;
        String value = null;
        if(!expression.getOperator().equals(Operator.IN)) {
            operand = expression.getOperand();;
            value = expression.getValue();
        }
        switch (expression.getOperator()) {
            case EQUALS:
                return operand.equalsIgnoreCase(value);
            case NOT_EQUALS:
                return !operand.equalsIgnoreCase(value);
            case IN:
                if(expression.getValuesList().isEmpty())
                    throw new IllegalArgumentException("No found values in set");
                return expression.getValuesList().stream().anyMatch(c -> c.equalsIgnoreCase(expression.getOperand()));
            default:
                throw new IllegalArgumentException("Only \"EQUALS, NOT_EQUALS, IN\" operators allowed");
        }
    }

    private static boolean evaluateInteger(Expression expression) {
        Integer operand = null;
        Integer value = null;
        if(!expression.getOperator().equals(Operator.IN)) {
            operand = Integer.parseInt(expression.getOperand());
            value = Integer.parseInt(expression.getValue());
        }
        switch (expression.getOperator()) {
            case LESS_THAN:
                return operand < value;
            case GREATER_THAN:
                return operand > value;
            case LESS_THAN_EQUAL:
                return operand <= value;
            case GREATER_THAN_EQUAL:
                return operand >= value;
            case EQUALS:
                return operand.equals(value);
            case NOT_EQUALS:
                return !operand.equals(value);
            case IN:
                if(expression.getValuesList().isEmpty())
                    throw new IllegalArgumentException("No found values in set");
                System.out.println(expression);
                return expression.getValuesList().stream().map(Integer::parseInt).anyMatch(c -> c.equals(Integer.parseInt(expression.getOperand())));
            default:
                return false;
        }
    }

    private static boolean evaluateBoolean(Expression expression) {
        boolean operand = Boolean.parseBoolean(expression.getOperand());
        boolean value = Boolean.parseBoolean(expression.getValue());
        switch (expression.getOperator()) {
            case EQUALS:
                return operand == value;
            case NOT_EQUALS:
                return !operand == value;
            default:
                throw new IllegalArgumentException("Only \"EQUALS, NOT_EQUALS\" operators allowed");
        }
    }

    /**
     * validates that all needed properties are set in Expression object {@link Expression}
     * we don't check content only if it's set
     * @param expression
     * @return true/false filled object
     */
    private static boolean validateExpressionObject(Expression expression) {
        return Objects.nonNull(expression.getOperand()) &&
                Objects.nonNull(expression.getOperator()) &&
                Objects.nonNull(expression.getPlaceholderDataType()) &&
                (Objects.nonNull(expression.getValue()) || !expression.getValuesList().isEmpty());
    }

    /**
     * algorithm checks for correct usage of parenthesis, if they are valid, we assume there is SET of Values
     * @param str - initial/input expresssion
     * @return true/false valid parenthesis
     */
    private static boolean checkParenthesis(String str) {
        Stack<Character> stack = new Stack<>();
        for (int i = 0; i < str.length(); i++) {
            char x = str.charAt(i);
            if (x == '(' || x == '[' || x == '{') {
                stack.push(x);
                continue;
            }
            if (stack.isEmpty()) return false;
            char check;
            switch (x) {
                case ')':
                    check = stack.pop();
                    if (check == '{' || check == '[') return false;
                    break;
                case '}':
                    check = stack.pop();
                    if (check == '(' || check == '[') return false;
                    break;
                case ']':
                    check = stack.pop();
                    if (check == '(' || check == '{') return false;
                    break;
            }
        }
        return (stack.isEmpty());
    }

}
