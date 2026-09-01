package com.factoryflow.intelligence.application;
import com.factoryflow.intelligence.persistence.MaintenanceIntelligenceAlertRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
@Service
public class AlertNotificationStateService {
    private final MaintenanceIntelligenceAlertRepository alerts;
    public AlertNotificationStateService(MaintenanceIntelligenceAlertRepository alerts) { this.alerts = alerts; }
    @Transactional(propagation = Propagation.REQUIRES_NEW) public void sent(Long id) { alerts.findById(id).orElseThrow().notificationSent(); }
    @Transactional(propagation = Propagation.REQUIRES_NEW) public void failed(Long id, String message) { alerts.findById(id).orElseThrow().notificationFailed(message); }
}
