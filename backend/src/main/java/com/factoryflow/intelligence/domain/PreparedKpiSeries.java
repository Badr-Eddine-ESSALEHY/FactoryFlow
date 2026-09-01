package com.factoryflow.intelligence.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record PreparedKpiSeries(
        KpiIdentity kpi,
        LocalDate windowStart,
        LocalDate windowEnd,
        int sourceRecordCount,
        int usableObservationCount,
        int missingValueCount,
        CadenceMetadata cadence,
        List<Observation> observations
) {
    public PreparedKpiSeries {
        observations = List.copyOf(observations);
    }

    public record Observation(
            Long entryId,
            Long reportId,
            LocalDate effectiveDate,
            Instant confirmedAt,
            BigDecimal value
    ) {
    }

    public record CadenceMetadata(
            CadenceState state,
            Integer observedCadenceDays,
            Integer expectedCadenceDays,
            CadenceBasis cadenceBasis,
            CadenceAmbiguity ambiguity,
            int distinctDateCount,
            int duplicateDateCount,
            int missingValueCount,
            boolean resamplingApplied,
            String resamplingPolicy
    ) {
    }

    public enum CadenceState {
        EMPTY,
        SINGLE_DATE,
        REGULAR,
        IRREGULAR
    }

    public enum CadenceBasis {
        INFERRED_OBSERVED,
        CONFIGURED_EXPECTED,
        UNKNOWN
    }

    public enum CadenceAmbiguity {
        NONE,
        INSUFFICIENT_OBSERVATIONS,
        MISSING_OBSERVATIONS,
        IRREGULAR_OBSERVED_SPACING,
        DUPLICATE_EFFECTIVE_DATES,
        OBSERVED_SPACING_DIFFERS_FROM_EXPECTED
    }
}
