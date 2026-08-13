package com.factoryflow.parser.application;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("factoryflow.parser")
public record ParserProperties(
        @DecimalMin("0.0") @DecimalMax("1.0") double fuzzyThreshold,
        @DecimalMin("0.0") @DecimalMax("1.0") double fuzzyAmbiguityMargin,
        @DecimalMin("0.0") @DecimalMax("1.0") double suggestionThreshold
) {
}
