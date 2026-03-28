package com.urimaigal.controller;

import com.urimaigal.dto.ApiResponse;
import com.urimaigal.service.LawyerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Dedicated Elasticsearch search endpoints.
 *
 * GET /api/search/lawyers?q=criminal
 * GET /api/search/lawyers/filter?specialization=Family&available=true
 */
@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final LawyerService lawyerService;

    public SearchController(LawyerService lawyerService) {
        this.lawyerService = lawyerService;
    }

    @GetMapping("/lawyers")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> searchLawyers(
            @RequestParam String q) {
        return ResponseEntity.ok(ApiResponse.ok(lawyerService.searchLawyers(q)));
    }

    @GetMapping("/lawyers/filter")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> filterLawyers(
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) Boolean available,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Double maxFee) {
        return ResponseEntity.ok(ApiResponse.ok(
                lawyerService.filteredSearchElastic(specialization, language, available, minRating, maxFee)));
    }
}
