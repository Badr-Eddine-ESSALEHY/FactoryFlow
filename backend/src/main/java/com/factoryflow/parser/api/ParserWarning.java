package com.factoryflow.parser.api;

public record ParserWarning(String code, String message, String severity) {
    public static ParserWarning warning(String code, String message) {
        return new ParserWarning(code, message, "WARNING");
    }
}
