package com.factoryflow.intelligence;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import com.factoryflow.intelligence.application.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
class MaintenanceIntelligenceRefreshCoordinatorTest {
    @Test void simultaneousManualRefreshForSameKpiIsRejected() throws Exception {
        var entered = new CountDownLatch(1); var release = new CountDownLatch(1);
        var orchestrator = mock(MaintenanceIntelligenceRefreshOrchestrator.class);
        when(orchestrator.refresh(7L, null, false)).thenAnswer(invocation -> { entered.countDown(); release.await(5, TimeUnit.SECONDS); return null; });
        var coordinator = new MaintenanceIntelligenceRefreshCoordinator(orchestrator, mock(ThreadPoolTaskExecutor.class));
        var first = CompletableFuture.runAsync(() -> coordinator.refreshNow(7L, null));
        entered.await(5, TimeUnit.SECONDS);
        assertThatThrownBy(() -> coordinator.refreshNow(7L, null)).hasMessageContaining("already running");
        release.countDown(); first.get(5, TimeUnit.SECONDS);
    }
}
