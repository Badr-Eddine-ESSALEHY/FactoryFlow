package com.factoryflow.intelligence.application;

import com.factoryflow.intelligence.domain.ConfirmedKpiHistoryRecord;
import com.factoryflow.intelligence.domain.KpiIdentity;
import com.factoryflow.intelligence.domain.PreparedKpiSeries;
import com.factoryflow.intelligence.domain.PreparedKpiSeries.CadenceMetadata;
import com.factoryflow.intelligence.domain.PreparedKpiSeries.CadenceAmbiguity;
import com.factoryflow.intelligence.domain.PreparedKpiSeries.CadenceBasis;
import com.factoryflow.intelligence.domain.PreparedKpiSeries.CadenceState;
import com.factoryflow.report.domain.ReportStatus;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class HistoricalKpiDataPreparer {

    public PreparedKpiSeries prepare(
            Long requestedKpiDefinitionId,
            LocalDate windowStart,
            LocalDate windowEnd,
            List<ConfirmedKpiHistoryRecord> sourceRecords
    ) {
        return prepare(requestedKpiDefinitionId, windowStart, windowEnd, sourceRecords, null);
    }

    public PreparedKpiSeries prepare(
            Long requestedKpiDefinitionId,
            LocalDate windowStart,
            LocalDate windowEnd,
            List<ConfirmedKpiHistoryRecord> sourceRecords,
            Integer expectedBusinessCadenceDays
    ) {
        if (requestedKpiDefinitionId == null) throw new IllegalArgumentException("KPI definition id is required");
        if (windowStart == null || windowEnd == null || windowStart.isAfter(windowEnd)) {
            throw new IllegalArgumentException("A valid historical window is required");
        }
        if (expectedBusinessCadenceDays != null && expectedBusinessCadenceDays <= 0) {
            throw new IllegalArgumentException("Expected business cadence must be a positive day count");
        }

        List<ConfirmedKpiHistoryRecord> records = List.copyOf(sourceRecords);
        records.forEach(record -> validateTrustedRecord(requestedKpiDefinitionId, windowStart, windowEnd, record));
        KpiIdentity identity = records.isEmpty()
                ? new KpiIdentity(requestedKpiDefinitionId, null, null, null)
                : records.getFirst().kpi();
        records.forEach(record -> validateIdentity(identity, record.kpi()));

        List<PreparedKpiSeries.Observation> observations = records.stream()
                .filter(record -> record.finalValue() != null)
                .sorted(Comparator.comparing(ConfirmedKpiHistoryRecord::effectiveDate)
                        .thenComparing(ConfirmedKpiHistoryRecord::confirmedAt,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(ConfirmedKpiHistoryRecord::reportId)
                        .thenComparing(ConfirmedKpiHistoryRecord::entryId))
                .map(record -> new PreparedKpiSeries.Observation(
                        record.entryId(), record.reportId(), record.effectiveDate(), record.confirmedAt(), record.finalValue()))
                .toList();

        return new PreparedKpiSeries(
                identity,
                windowStart,
                windowEnd,
                records.size(),
                observations.size(),
                records.size() - observations.size(),
                cadence(observations, records.size() - observations.size(), expectedBusinessCadenceDays),
                observations
        );
    }

    private void validateTrustedRecord(Long requestedKpiDefinitionId, LocalDate windowStart, LocalDate windowEnd,
                                       ConfirmedKpiHistoryRecord record) {
        if (record == null || record.kpi() == null || record.entryId() == null || record.reportId() == null
                || record.effectiveDate() == null) {
            throw new IllegalArgumentException("Historical records must retain KPI, date, entry, and report identity");
        }
        if (record.reportStatus() != ReportStatus.CONFIRMED) {
            throw new IllegalArgumentException("Maintenance Intelligence accepts confirmed reports only");
        }
        if (!requestedKpiDefinitionId.equals(record.kpi().definitionId())) {
            throw new IllegalArgumentException("Cross-KPI history is forbidden");
        }
        if (record.effectiveDate().isBefore(windowStart) || record.effectiveDate().isAfter(windowEnd)) {
            throw new IllegalArgumentException("Historical record is outside the requested window");
        }
    }

    private void validateIdentity(KpiIdentity expected, KpiIdentity actual) {
        if (!expected.equals(actual)) throw new IllegalArgumentException("KPI identity or unit changed inside one series");
    }

    private CadenceMetadata cadence(List<PreparedKpiSeries.Observation> observations, int missingValueCount,
                                    Integer expectedBusinessCadenceDays) {
        Map<LocalDate, Integer> counts = new LinkedHashMap<>();
        observations.forEach(point -> counts.merge(point.effectiveDate(), 1, Integer::sum));
        int duplicateCount = counts.values().stream().mapToInt(count -> Math.max(0, count - 1)).sum();
        List<LocalDate> dates = new ArrayList<>(counts.keySet());
        CadenceState state;
        Integer observedCadenceDays = null;
        if (dates.isEmpty()) {
            state = CadenceState.EMPTY;
        } else if (duplicateCount > 0) {
            state = CadenceState.IRREGULAR;
        } else if (dates.size() == 1) {
            state = CadenceState.SINGLE_DATE;
        } else {
            List<Long> intervals = new ArrayList<>();
            for (int index = 1; index < dates.size(); index++) {
                intervals.add(ChronoUnit.DAYS.between(dates.get(index - 1), dates.get(index)));
            }
            boolean regular = duplicateCount == 0 && intervals.stream().distinct().count() == 1;
            state = regular ? CadenceState.REGULAR : CadenceState.IRREGULAR;
            if (regular) observedCadenceDays = Math.toIntExact(intervals.getFirst());
        }
        CadenceBasis basis = expectedBusinessCadenceDays == null
                ? (state == CadenceState.REGULAR && missingValueCount == 0
                        ? CadenceBasis.INFERRED_OBSERVED : CadenceBasis.UNKNOWN)
                : CadenceBasis.CONFIGURED_EXPECTED;
        CadenceAmbiguity ambiguity;
        if (duplicateCount > 0) {
            ambiguity = CadenceAmbiguity.DUPLICATE_EFFECTIVE_DATES;
        } else if (missingValueCount > 0) {
            ambiguity = CadenceAmbiguity.MISSING_OBSERVATIONS;
        } else if (state == CadenceState.EMPTY || state == CadenceState.SINGLE_DATE) {
            ambiguity = CadenceAmbiguity.INSUFFICIENT_OBSERVATIONS;
        } else if (state == CadenceState.IRREGULAR) {
            ambiguity = CadenceAmbiguity.IRREGULAR_OBSERVED_SPACING;
        } else if (expectedBusinessCadenceDays != null
                && !expectedBusinessCadenceDays.equals(observedCadenceDays)) {
            ambiguity = CadenceAmbiguity.OBSERVED_SPACING_DIFFERS_FROM_EXPECTED;
        } else {
            ambiguity = CadenceAmbiguity.NONE;
        }
        return new CadenceMetadata(state, observedCadenceDays, expectedBusinessCadenceDays, basis, ambiguity,
                dates.size(), duplicateCount, missingValueCount, false, "NONE");
    }
}
