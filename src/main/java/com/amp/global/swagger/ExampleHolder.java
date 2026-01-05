package com.amp.global.swagger;


import io.swagger.v3.oas.models.examples.Example;

public record ExampleHolder(
        Example holder,
        String name,
        int status
) {
    public static ExampleHolder of(Example holder, String name, int status) {
        return new ExampleHolder(holder, name, status);
    }
}
