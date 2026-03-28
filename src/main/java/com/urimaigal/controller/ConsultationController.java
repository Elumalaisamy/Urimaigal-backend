package com.urimaigal.controller;

import com.urimaigal.dto.ApiResponse;
import com.urimaigal.dto.BookingRequest;
import com.urimaigal.model.Consultation;
import com.urimaigal.service.ConsultationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * POST   /api/bookings                   — book a consultation
 * GET    /api/bookings                   — get current user's bookings
 * GET    /api/bookings/{id}              — get single booking
 * PATCH  /api/bookings/{id}/cancel       — cancel booking
 * PATCH  /api/bookings/{id}/complete     — mark completed (admin/lawyer)
 * GET    /api/bookings/lawyer/{lawyerId} — get lawyer's bookings
 */
@RestController
@RequestMapping("/api/bookings")
public class ConsultationController {

    private final ConsultationService consultationService;

    public ConsultationController(ConsultationService consultationService) {
        this.consultationService = consultationService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Consultation>> book(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody BookingRequest request) {
        Consultation consultation = consultationService.bookConsultation(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Consultation booked successfully", consultation));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Consultation>>> getClientConsultations(
            @AuthenticationPrincipal String clientId) {
        return ResponseEntity.ok(ApiResponse.ok(consultationService.getClientConsultations(clientId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Consultation>> getById(
            @PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(consultationService.getConsultationById(id)));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Consultation>> cancel(
            @PathVariable String id,
            @AuthenticationPrincipal String userId) {
        Consultation cancelled = consultationService.cancelConsultation(id, userId);
        return ResponseEntity.ok(ApiResponse.ok("Consultation cancelled", cancelled));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<Consultation>> complete(@PathVariable String id) {
        Consultation completed = consultationService.completeConsultation(id);
        return ResponseEntity.ok(ApiResponse.ok("Consultation marked completed", completed));
    }

    @GetMapping("/lawyer/{lawyerId}")
    public ResponseEntity<ApiResponse<List<Consultation>>> getLawyerBookings(
            @PathVariable String lawyerId) {
        return ResponseEntity.ok(ApiResponse.ok(consultationService.getAdvocateConsultations(lawyerId)));
    }
}
