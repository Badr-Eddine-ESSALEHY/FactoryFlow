package com.factoryflow.parser.api;

public record UnrecognizedLine(String lineId, String sourceLine, String reason) {
}
