package com.factoryflow.intelligence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.factoryflow.intelligence.application.HistoricalKpiDataPreparer;
import com.factoryflow.intelligence.domain.ConfirmedKpiHistoryRecord;
import com.factoryflow.intelligence.domain.KpiIdentity;
import com.factoryflow.intelligence.domain.PreparedKpiSeries.CadenceState;
import com.factoryflow.intelligence.domain.PreparedKpiSeries.CadenceAmbiguity;
import com.factoryflow.intelligence.domain.PreparedKpiSeries.CadenceBasis;
import com.factoryflow.report.domain.ReportStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class HistoricalKpiDataPreparerTest {
    private static final LocalDate START = LocalDate.of(2026, 1, 1);
    private static final KpiIdentity KPI = new KpiIdentity(7L, "TEMP", "Température", "°C");
    private final HistoricalKpiDataPreparer preparer = new HistoricalKpiDataPreparer();

    @Test
    void excludesMissingValuesPreservesZeroSortsChronologicallyAndRetainsSourceIdentity() {
        var result = preparer.prepare(7L, START, START.plusDays(3), List.of(
                record(3, 103, 3, "5", ReportStatus.CONFIRMED),
                record(1, 101, 1, "0", ReportStatus.CONFIRMED),
                record(2, 102, 2, null, ReportStatus.CONFIRMED)));

        assertThat(result.sourceRecordCount()).isEqualTo(3);
        assertThat(result.usableObservationCount()).isEqualTo(2);
        assertThat(result.missingValueCount()).isEqualTo(1);
        assertThat(result.observations()).extracting(point -> point.entryId()).containsExactly(1L, 3L);
        assertThat(result.observations().getFirst().value()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.observations().getFirst().reportId()).isEqualTo(101L);
        assertThat(result.observations().getFirst().confirmedAt()).isEqualTo(Instant.parse("2026-01-01T00:00:01Z"));
        assertThat(result.cadence().state()).isEqualTo(CadenceState.REGULAR);
        assertThat(result.cadence().observedCadenceDays()).isEqualTo(2);
        assertThat(result.cadence().cadenceBasis()).isEqualTo(CadenceBasis.UNKNOWN);
        assertThat(result.cadence().ambiguity()).isEqualTo(CadenceAmbiguity.MISSING_OBSERVATIONS);
        assertThat(result.cadence().missingValueCount()).isEqualTo(1);
        assertThat(result.cadence().resamplingApplied()).isFalse();
        assertThat(result.cadence().resamplingPolicy()).isEqualTo("NONE");
    }

    @Test
    void reportsAllMissingHistoryWithoutInventingObservations() {
        var result = preparer.prepare(7L, START, START.plusDays(1), List.of(
                record(1, 101, 0, null, ReportStatus.CONFIRMED),
                record(2, 102, 1, null, ReportStatus.CONFIRMED)));
        assertThat(result.observations()).isEmpty();
        assertThat(result.missingValueCount()).isEqualTo(2);
        assertThat(result.cadence().state()).isEqualTo(CadenceState.EMPTY);
    }

    @Test
    void identifiesRegularAndDuplicateDateCadenceExplicitly() {
        var regular = preparer.prepare(7L, START, START.plusDays(4), List.of(
                record(1, 101, 0, "1", ReportStatus.CONFIRMED),
                record(2, 102, 2, "2", ReportStatus.CONFIRMED),
                record(3, 103, 4, "3", ReportStatus.CONFIRMED)));
        assertThat(regular.cadence().state()).isEqualTo(CadenceState.REGULAR);
        assertThat(regular.cadence().observedCadenceDays()).isEqualTo(2);
        assertThat(regular.cadence().cadenceBasis()).isEqualTo(CadenceBasis.INFERRED_OBSERVED);
        assertThat(regular.cadence().ambiguity()).isEqualTo(CadenceAmbiguity.NONE);

        var duplicate = preparer.prepare(7L, START, START.plusDays(1), List.of(
                record(1, 101, 0, "1", ReportStatus.CONFIRMED),
                record(2, 102, 0, "2", ReportStatus.CONFIRMED)));
        assertThat(duplicate.cadence().state()).isEqualTo(CadenceState.IRREGULAR);
        assertThat(duplicate.cadence().duplicateDateCount()).isEqualTo(1);
        assertThat(duplicate.cadence().ambiguity()).isEqualTo(CadenceAmbiguity.DUPLICATE_EFFECTIVE_DATES);
    }

    @Test
    void distinguishesInferredDailyCadenceFromOptionalConfiguredExpectation() {
        var daily = preparer.prepare(7L, START, START.plusDays(2), List.of(
                record(1, 101, 0, "1", ReportStatus.CONFIRMED),
                record(2, 102, 1, "2", ReportStatus.CONFIRMED),
                record(3, 103, 2, "3", ReportStatus.CONFIRMED)));
        assertThat(daily.cadence().observedCadenceDays()).isEqualTo(1);
        assertThat(daily.cadence().expectedCadenceDays()).isNull();
        assertThat(daily.cadence().cadenceBasis()).isEqualTo(CadenceBasis.INFERRED_OBSERVED);

        var configured = preparer.prepare(7L, START, START.plusDays(2), dailyRecords(), 1);
        assertThat(configured.cadence().expectedCadenceDays()).isEqualTo(1);
        assertThat(configured.cadence().cadenceBasis()).isEqualTo(CadenceBasis.CONFIGURED_EXPECTED);
        assertThat(configured.cadence().ambiguity()).isEqualTo(CadenceAmbiguity.NONE);
        assertThatThrownBy(() -> preparer.prepare(7L, START, START.plusDays(2), dailyRecords(), 0))
                .hasMessageContaining("positive day count");
    }

    @Test
    void rejectsDraftHistoryAndCrossKpiOrUnitMixing() {
        assertThatThrownBy(() -> preparer.prepare(7L, START, START, List.of(
                record(1, 101, 0, "1", ReportStatus.DRAFT))))
                .hasMessageContaining("confirmed reports only");

        var otherKpi = new ConfirmedKpiHistoryRecord(new KpiIdentity(8L, "OTHER", "Other", "bar"),
                2L, 102L, START, Instant.parse("2026-01-01T00:00:02Z"), ReportStatus.CONFIRMED, BigDecimal.ONE);
        assertThatThrownBy(() -> preparer.prepare(7L, START, START, List.of(otherKpi)))
                .hasMessageContaining("Cross-KPI");

        var changedUnit = new ConfirmedKpiHistoryRecord(new KpiIdentity(7L, "TEMP", "Température", "K"),
                2L, 102L, START.plusDays(1), Instant.parse("2026-01-02T00:00:02Z"),
                ReportStatus.CONFIRMED, BigDecimal.ONE);
        assertThatThrownBy(() -> preparer.prepare(7L, START, START.plusDays(1), List.of(
                record(1, 101, 0, "1", ReportStatus.CONFIRMED), changedUnit)))
                .hasMessageContaining("identity or unit changed");
    }

    private ConfirmedKpiHistoryRecord record(long entryId, long reportId, int day, String value, ReportStatus status) {
        return new ConfirmedKpiHistoryRecord(KPI, entryId, reportId, START.plusDays(day),
                Instant.parse("2026-01-01T00:00:00Z").plusSeconds(entryId), status,
                value == null ? null : new BigDecimal(value));
    }

    private List<ConfirmedKpiHistoryRecord> dailyRecords() {
        return List.of(record(1, 101, 0, "1", ReportStatus.CONFIRMED),
                record(2, 102, 1, "2", ReportStatus.CONFIRMED),
                record(3, 103, 2, "3", ReportStatus.CONFIRMED));
    }
}
