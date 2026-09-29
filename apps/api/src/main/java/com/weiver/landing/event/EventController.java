package com.weiver.landing.event;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private static final Logger log = LoggerFactory.getLogger(EventController.class);
    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public ResponseEntity<Void> track(@Valid @RequestBody TrackEventRequest req) {
        try {
            eventService.track(req);
        } catch (Exception e) {
            log.error("Failed to track event: {}", req.eventName(), e);
        }
        return ResponseEntity.ok().build();
    }
}
