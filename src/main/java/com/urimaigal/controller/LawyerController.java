package com.urimaigal.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.urimaigal.dto.ApiResponse;
import com.urimaigal.model.Lawyer;
import com.urimaigal.service.LawyerService;

/**
 * GET    /api/lawyers                    — list all
 * GET    /api/lawyers/{id}               — get by id
 * GET    /api/lawyers/specialization/{s} — filter by specialization
 * GET    /api/lawyers/available          — only available lawyers
 * GET    /api/lawyers/filter             — multi-field filter
 * GET    /api/lawyers/search?q=          — ES full-text search
 * POST   /api/lawyers                    — create (admin)
 * PUT    /api/lawyers/{id}               — update (admin)
 * PATCH  /api/lawyers/{id}/availability  — toggle availability
 * DELETE /api/lawyers/{id}              — delete (admin)
 * POST   /api/lawyers/reindex            — bulk ES reindex
 */
@RestController
@RequestMapping("/api/lawyers")
public class LawyerController {

    private final LawyerService lawyerService;

    public LawyerController(LawyerService lawyerService) {
        this.lawyerService = lawyerService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Lawyer>>> getAllLawyers() {
        return ResponseEntity.ok(ApiResponse.ok(lawyerService.getAllLawyers()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Lawyer>> getLawyerById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(lawyerService.getLawyerById(id)));
    }

    @GetMapping("/specialization/{specialization}")
    public ResponseEntity<ApiResponse<List<Lawyer>>> getBySpecialization(
            @PathVariable String specialization) {
        return ResponseEntity.ok(ApiResponse.ok(lawyerService.getLawyersBySpecialization(specialization)));
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<Lawyer>>> getAvailable() {
        return ResponseEntity.ok(ApiResponse.ok(lawyerService.getAvailableLawyers()));
    }

    /**
     * Filter lawyers by multiple criteria (MySQL NamedJdbc query).
     */
    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<List<Lawyer>>> filterLawyers(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) BigDecimal maxFee,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) Boolean available) {
        List<Lawyer> result = lawyerService.filterLawyers(category, minRating, maxFee, language, available);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * Full-text search via Elasticsearch (falls back to MySQL if ES is down).
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> searchLawyers(
            @RequestParam String q) {
        return ResponseEntity.ok(ApiResponse.ok(lawyerService.searchLawyers(q)));
    }

    /**
     * Elasticsearch filtered search endpoint.
     */
    @GetMapping("/search/filter")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> elasticFilter(
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) Boolean available,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Double maxFee) {
        return ResponseEntity.ok(ApiResponse.ok(
                lawyerService.filteredSearchElastic(specialization, language, available, minRating, maxFee)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADVOCATE')")
    public ResponseEntity<ApiResponse<Lawyer>> createLawyer(@RequestBody Lawyer lawyer) {
        Lawyer created = lawyerService.createLawyer(lawyer);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Lawyer created", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADVOCATE')")
    public ResponseEntity<ApiResponse<Lawyer>> updateLawyer(
            @PathVariable String id,
            @RequestBody Lawyer lawyer) {
        return ResponseEntity.ok(ApiResponse.ok("Lawyer updated", lawyerService.updateLawyer(id, lawyer)));
    }

    @PatchMapping("/{id}/availability")
    public ResponseEntity<ApiResponse<Void>> updateAvailability(
            @PathVariable String id,
            @RequestParam boolean available) {
        lawyerService.updateAvailability(id, available);
        return ResponseEntity.ok(ApiResponse.ok("Availability updated", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLawyer(@PathVariable String id) {
        lawyerService.deleteLawyer(id);
        return ResponseEntity.ok(ApiResponse.ok("Lawyer deleted", null));
    }

    @PostMapping("/reindex")
    public ResponseEntity<ApiResponse<String>> reindex() {
        int count = lawyerService.reindexAll();
        return ResponseEntity.ok(ApiResponse.ok("Reindexed " + count + " lawyers into Elasticsearch", null));
    }
}
