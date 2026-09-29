package com.weiver.landing.reservation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepo;
    private final ObjectMapper objectMapper;

    public ReservationService(ReservationRepository reservationRepo, ObjectMapper objectMapper) {
        this.reservationRepo = reservationRepo;
        this.objectMapper = objectMapper;
    }

    public UUID reserve(CreateReservationRequest req) {
        try {
            return reservationRepo.insert(req, toJson(req.utm()));
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateEmailException();
        }
    }

    private String toJson(Map<String, String> utm) {
        if (utm == null) return "{}";
        try {
            return objectMapper.writeValueAsString(utm);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
