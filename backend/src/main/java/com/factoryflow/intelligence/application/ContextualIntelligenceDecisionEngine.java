package com.factoryflow.intelligence.application;
import com.factoryflow.intelligence.domain.*;
import java.util.Optional;
import org.springframework.stereotype.Component;
@Component
public class ContextualIntelligenceDecisionEngine {
    public Optional<ContextualDecision> decide(MaintenanceIntelligenceAnalysis analysis) {
        return decide(Boolean.TRUE.equals(analysis.getLatestAnomalous()), analysis.getOutsideExpectedInterval());
    }
    public Optional<ContextualDecision> decide(boolean anomaly, Boolean outsideExpectedInterval) {
        boolean deviation = Boolean.TRUE.equals(outsideExpectedInterval);
        if (anomaly && deviation) return Optional.of(new ContextualDecision(ContextualAlertType.STRONG_CONTEXTUAL_DEVIATION, IntelligenceAttentionLevel.HIGH, true, true));
        if (anomaly) return Optional.of(new ContextualDecision(ContextualAlertType.ANOMALOUS_OBSERVATION, IntelligenceAttentionLevel.MEDIUM, true, false));
        if (deviation) return Optional.of(new ContextualDecision(ContextualAlertType.FORECAST_DEVIATION, IntelligenceAttentionLevel.MEDIUM, false, true));
        return Optional.empty();
    }
}
