package com.factoryflow.intelligence.api;
import com.factoryflow.intelligence.domain.MaintenanceIntelligenceResult;
public record IntelligenceAnalysisDetailResponse(IntelligenceAnalysisSummaryResponse summary,
                                                  MaintenanceIntelligenceResult result) { }
