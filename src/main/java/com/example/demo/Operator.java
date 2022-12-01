package com.example.demo;

import java.util.Arrays;
import java.util.Optional;

public enum Operator {
    LESS_THAN("lt"),
    GREATER_THAN("gt"),
    LESS_THAN_EQUAL("le"),
    GREATER_THAN_EQUAL("ge"),
    EQUALS("eq"),
    NOT_EQUALS("ne"),
    IN("in");
    private final String operator;

    Operator(String operator) {
        this.operator = operator;
    }

    public String getOperator() {
        return operator;
    }

    //Reverse lookup
    public static Optional<Operator> get(String operator) {
        return Arrays.stream(Operator.values())
                .filter(env -> env.operator.equals(operator))
                .findFirst();
    }
}
