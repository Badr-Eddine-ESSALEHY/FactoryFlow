package com.factoryflow.kpi.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ApproveAliasRequest(
        @NotBlank @Size(max = 200) String alias
) {
}
