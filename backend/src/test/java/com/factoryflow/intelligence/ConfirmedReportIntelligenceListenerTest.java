package com.factoryflow.intelligence;
import static org.mockito.Mockito.*;
import com.factoryflow.intelligence.application.*;
import com.factoryflow.report.application.ReportConfirmedEvent;
import java.time.*;
import java.util.Set;
import org.junit.jupiter.api.Test;
class ConfirmedReportIntelligenceListenerTest {
    @Test void schedulesEveryDistinctConfirmedKpiAfterTheEventIsDelivered() {
        var coordinator = mock(MaintenanceIntelligenceRefreshCoordinator.class);
        var listener = new ConfirmedReportIntelligenceListener(coordinator,
                Clock.fixed(Instant.parse("2026-02-10T12:00:00Z"), ZoneOffset.UTC));
        listener.afterConfirmed(new ReportConfirmedEvent(4L, LocalDate.of(2026, 2, 8), Set.of(7L, 8L)));
        verify(coordinator).submit(7L, LocalDate.of(2026, 2, 10)); verify(coordinator).submit(8L, LocalDate.of(2026, 2, 10));
    }
}
