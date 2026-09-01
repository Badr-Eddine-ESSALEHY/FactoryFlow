package com.factoryflow.intelligence.api;
public record IntelligenceOverviewItemResponse(IntelligenceProfileResponse profile,
        IntelligenceAnalysisSummaryResponse latestSuccessfulAnalysis,
        IntelligenceAnalysisSummaryResponse latestRefreshAttempt, long alertCount) { }
