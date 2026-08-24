package com.factoryflow.generatedreport.domain;

public enum GeneratedReportType {
    INDIVIDUAL,
    DAILY,
    WEEKLY,
    MONTHLY,
    CUSTOM,
    /** Legacy persisted/API value retained for backward compatibility. Use CUSTOM for new requests. */
    MANUAL
}
