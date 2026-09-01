package com.factoryflow.intelligence.application;

import com.factoryflow.intelligence.domain.ConfirmedKpiHistoryRecord;
import java.time.LocalDate;
import java.util.List;

public interface ConfirmedKpiHistorySource {
    List<ConfirmedKpiHistoryRecord> load(Long kpiDefinitionId, LocalDate windowStart, LocalDate windowEnd);
}
