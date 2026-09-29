package com.weiver.landing.reservation;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> reserve(@Valid @RequestBody CreateReservationRequest req) {
        UUID id = reservationService.reserve(req);
        return ResponseEntity.status(201).body(Map.of("id", id.toString()));
    }
}
