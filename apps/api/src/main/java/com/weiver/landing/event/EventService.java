package com.weiver.landing.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EventService {

    private static final Logger log = LoggerFactory.getLogger(EventService.class);
    private final EventRepository eventRepo;

    public EventService(EventRepository eventRepo) {
        this.eventRepo = eventRepo;
    }

    // AC-2-2: DB 오류는 삼키고 200 유지 (fire-and-forget)
    public void track(TrackEventRequest req) {
        try {
            eventRepo.insert(req);
        } catch (Exception e) {
            log.error("Failed to track event: {}", req.eventName(), e);
        }
    }
}
