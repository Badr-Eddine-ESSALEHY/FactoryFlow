package com.factoryflow.ocr.api;

import com.factoryflow.ocr.application.OcrService;
import com.factoryflow.ocr.domain.OcrHealth;
import com.factoryflow.ocr.domain.OcrResult;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ocr")
public class OcrController {
    private final OcrService service;
    public OcrController(OcrService service) { this.service = service; }

    @PostMapping(value = "/recognize", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public OcrResult recognize(@RequestPart("image") MultipartFile image) { return service.recognize(image); }

    @GetMapping("/health")
    public OcrHealth health() { return service.health(); }
}
