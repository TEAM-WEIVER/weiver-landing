package com.weiver.landing.visitor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;

public record BootstrapRequest(
    @NotNull UUID sessionId,
    @NotBlank String path,
    String referrer,
    Map<String, String> utm
) {}
