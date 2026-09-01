package com.factoryflow.intelligence.application;
import com.factoryflow.intelligence.domain.*;
public record ContextualDecision(ContextualAlertType type, IntelligenceAttentionLevel attentionLevel,
        boolean anomalySignal, boolean forecastDeviationSignal) { }
