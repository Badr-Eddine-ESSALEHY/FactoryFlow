package com.factoryflow.generatedreport.storage;

import org.springframework.core.io.Resource;

public record StoredReportFile(Resource resource, long contentLength) { }
