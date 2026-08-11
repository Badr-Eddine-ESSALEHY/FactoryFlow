package com.factoryflow.report.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public interface LatestKpiProjection {
    Long getKpiDefinitionId();
    String getCode();
    String getDisplayName();
    String getUnit();
    BigDecimal getValue();
    LocalDate getEffectiveDate();
    Long getReportId();
    Instant getConfirmedAt();
}
