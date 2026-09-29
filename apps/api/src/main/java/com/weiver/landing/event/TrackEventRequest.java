package com.weiver.landing.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;
import java.util.Map;

public record TrackEventRequest(
    @NotNull UUID visitorId,
    @NotNull UUID sessionId,
    @NotBlank
    @Pattern(regexp = "page_view|reserve_opened|reservation_completed|section_view|cta_clicked",
             message = "허용되지 않는 eventName")
    String eventName,
    @NotBlank String path,
    Map<String, String> properties
) {}
