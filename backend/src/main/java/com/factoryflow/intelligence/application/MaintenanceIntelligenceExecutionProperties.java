package com.factoryflow.intelligence.application;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
@Validated @ConfigurationProperties("factoryflow.intelligence-execution")
public record MaintenanceIntelligenceExecutionProperties(@Min(1) int corePoolSize, @Min(1) int maxPoolSize,
                                                          @Min(1) int queueCapacity) { }
