package com.factoryflow.intelligence;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import com.factoryflow.auth.application.AuthenticationService;
import com.factoryflow.auth.domain.UserAccount;
import com.factoryflow.notification.application.NotificationService;
import com.factoryflow.notification.persistence.UserNotificationRepository;
import java.time.*;
import org.junit.jupiter.api.Test;
class NotificationServiceIntelligenceTest {
    @Test void intelligenceAlertCreatesAtMostOneExistingNotification() {
        var repository = mock(UserNotificationRepository.class);
        var service = new NotificationService(repository, mock(AuthenticationService.class), Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
        var user = UserAccount.create("Engineer", "engineer@example.com", "hash");
        when(repository.existsByRelatedIntelligenceAlertId(12L)).thenReturn(false, true);
        org.assertj.core.api.Assertions.assertThat(service.notifyIntelligence(user, "Attention", "Evidence", 5L, 12L)).isTrue();
        org.assertj.core.api.Assertions.assertThat(service.notifyIntelligence(user, "Attention", "Evidence", 5L, 12L)).isFalse();
        verify(repository, times(1)).save(any());
    }
}
