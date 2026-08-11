package com.factoryflow.generatedreport.storage;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("factoryflow.reports")
public record ReportStorageProperties(@NotBlank String storageRoot) { }
