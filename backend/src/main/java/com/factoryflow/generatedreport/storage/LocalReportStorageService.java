package com.factoryflow.generatedreport.storage;

import com.factoryflow.shared.error.ApiErrorCode;
import com.factoryflow.shared.error.ApiException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class LocalReportStorageService implements ReportStorageService {

    private final Path root;

    public LocalReportStorageService(ReportStorageProperties properties) {
        this.root = Path.of(properties.storageRoot()).toAbsolutePath().normalize();
    }

    @Override
    public String store(String fileName, byte[] content) {
        Path target = safeResolve(fileName);
        try {
            Files.createDirectories(root);
            Path temporary = Files.createTempFile(root, ".factoryflow-", ".tmp");
            try {
                Files.write(temporary, content);
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } finally {
                Files.deleteIfExists(temporary);
            }
            return root.relativize(target).toString().replace('\\', '/');
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiErrorCode.REPORT_GENERATION_FAILED,
                    "The generated report could not be stored.");
        }
    }

    @Override
    public StoredReportFile read(String storageReference) {
        Path file = safeResolve(storageReference);
        try {
            if (!Files.isRegularFile(file)) {
                throw new ApiException(HttpStatus.NOT_FOUND, ApiErrorCode.REPORT_FILE_NOT_FOUND,
                        "The generated report file was not found.");
            }
            return new StoredReportFile(new FileSystemResource(file), Files.size(file));
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.NOT_FOUND, ApiErrorCode.REPORT_FILE_NOT_FOUND,
                    "The generated report file was not found.");
        }
    }

    @Override
    public void delete(String storageReference) {
        try {
            Files.deleteIfExists(safeResolve(storageReference));
        } catch (IOException ignored) {
            // A failed best-effort cleanup must not conceal the original generation failure.
        }
    }

    private Path safeResolve(String reference) {
        Path resolved = root.resolve(reference).normalize();
        if (!resolved.startsWith(root)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ApiErrorCode.VALIDATION_ERROR,
                    "Invalid report storage reference.");
        }
        return resolved;
    }
}
