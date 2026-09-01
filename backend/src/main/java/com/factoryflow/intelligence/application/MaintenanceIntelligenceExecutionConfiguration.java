package com.factoryflow.intelligence.application;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
@Configuration
public class MaintenanceIntelligenceExecutionConfiguration {
    @Bean("maintenanceIntelligenceExecutor")
    ThreadPoolTaskExecutor maintenanceIntelligenceExecutor(MaintenanceIntelligenceExecutionProperties properties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.corePoolSize()); executor.setMaxPoolSize(properties.maxPoolSize());
        executor.setQueueCapacity(properties.queueCapacity()); executor.setThreadNamePrefix("factoryflow-mi-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy()); executor.initialize(); return executor;
    }
}
