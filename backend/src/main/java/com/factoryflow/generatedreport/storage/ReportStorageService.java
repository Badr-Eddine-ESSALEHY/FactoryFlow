package com.factoryflow.generatedreport.storage;

public interface ReportStorageService {
    String store(String fileName, byte[] content);
    StoredReportFile read(String storageReference);
    void delete(String storageReference);
}
