package com.factoryflow.intelligence;
import static org.assertj.core.api.Assertions.assertThat;
import com.factoryflow.intelligence.application.ContextualIntelligenceDecisionEngine;
import com.factoryflow.intelligence.domain.*;
import org.junit.jupiter.api.Test;
class ContextualIntelligenceDecisionEngineTest {
    private final ContextualIntelligenceDecisionEngine engine = new ContextualIntelligenceDecisionEngine();
    @Test void combinesAnomalyAndGenuineDeviationIntoHighAttention() {
        var decision = engine.decide(true, true).orElseThrow();
        assertThat(decision.type()).isEqualTo(ContextualAlertType.STRONG_CONTEXTUAL_DEVIATION);
        assertThat(decision.attentionLevel()).isEqualTo(IntelligenceAttentionLevel.HIGH);
    }
    @Test void emitsOnlyTheEvidenceActuallyPresent() {
        assertThat(engine.decide(true, null).orElseThrow().type()).isEqualTo(ContextualAlertType.ANOMALOUS_OBSERVATION);
        assertThat(engine.decide(false, true).orElseThrow().type()).isEqualTo(ContextualAlertType.FORECAST_DEVIATION);
        assertThat(engine.decide(false, false)).isEmpty();
        assertThat(engine.decide(false, null)).isEmpty();
    }
}
