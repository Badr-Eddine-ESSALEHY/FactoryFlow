package com.factoryflow.intelligence.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public interface ConfirmedKpiHistoryProjection {
    Long getKpiDefinitionId();
    String getCode();
    String getDisplayName();
    String getUnit();
    Long getEntryId();
    Long getReportId();
    LocalDate getEffectiveDate();
    Instant getConfirmedAt();
    String getReportStatus();
    BigDecimal getFinalValue();
}
