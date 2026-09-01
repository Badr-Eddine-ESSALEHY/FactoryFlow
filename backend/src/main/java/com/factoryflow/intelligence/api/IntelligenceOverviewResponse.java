package com.factoryflow.intelligence.api;
import java.util.List;
public record IntelligenceOverviewResponse(List<IntelligenceOverviewItemResponse> kpis,
                                           List<IntelligenceAlertResponse> recentAlerts) { }
