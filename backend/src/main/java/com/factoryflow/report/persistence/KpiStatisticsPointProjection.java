package com.factoryflow.report.persistence;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface KpiStatisticsPointProjection {
    Long getKpiDefinitionId();
    String getCode();
    String getDisplayName();
    String getUnit();
    LocalDate getEffectiveDate();
    Long getReportId();
    BigDecimal getValue();
}
