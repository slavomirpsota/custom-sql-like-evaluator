package com.example.demo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PlaceholderData {
    private String name;
    private String value;
    private PlaceholderDataType placeholderDataType;

}
