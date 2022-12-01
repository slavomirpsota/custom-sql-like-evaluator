package com.example.demo;

import lombok.*;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Expression {

    private String stringExpression;
    private String operand;
    private Operator operator;
    private String value;

    private PlaceholderDataType placeholderDataType;
    private List<String> valuesList = new ArrayList<>();

}
