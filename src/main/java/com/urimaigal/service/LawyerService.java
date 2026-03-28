package com.urimaigal.service;

import com.urimaigal.exception.ResourceNotFoundException;
import com.urimaigal.model.Lawyer;
import com.urimaigal.repository.LawyerRepository;
import com.urimaigal.search.LawyerSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class LawyerService {

    private static final Logger log = LoggerFactory.getLogger(LawyerService.class);

    private final LawyerRepository lawyerRepository;
    private final LawyerSearchService lawyerSearchService;

    public LawyerService(LawyerRepository lawyerRepository,
                         LawyerSearchService lawyerSearchService) {
        this.lawyerRepository = lawyerRepository;
        this.lawyerSearchService = lawyerSearchService;
    }

    public List<Lawyer> getAllLawyers() {
        return lawyerRepository.findAll();
    }

    public Lawyer getLawyerById(String id) {
        return lawyerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lawyer not found with id: " + id));
    }

    public List<Lawyer> getLawyersBySpecialization(String specialization) {
        return lawyerRepository.findBySpecialization(specialization);
    }

    public List<Lawyer> getAvailableLawyers() {
        return lawyerRepository.findAvailable();
    }

    public List<Lawyer> filterLawyers(String category, Double minRating,
                                      BigDecimal maxFee, String language, Boolean available) {
        return lawyerRepository.findWithFilters(category, minRating,
                maxFee != null ? maxFee.doubleValue() : null, language, available);
    }

    /**
     * Full-text search via Elasticsearch, fallback to MySQL LIKE if ES is unavailable.
     */
    public List<Map<String, Object>> searchLawyers(String query) {
        List<Map<String, Object>> esResults = lawyerSearchService.fullTextSearch(query);
        if (!esResults.isEmpty()) {
            log.debug("Elasticsearch returned {} results for query '{}'", esResults.size(), query);
            return esResults;
        }
        // ES returned nothing — fallback to DB
        log.debug("Elasticsearch empty; falling back to MySQL for query '{}'", query);
        List<Lawyer> dbResults = lawyerRepository.searchByName(query);
        return dbResults.stream().map(this::lawyerToMap).toList();
    }

    public List<Map<String, Object>> filteredSearchElastic(String specialization, String language,
                                                            Boolean available, Double minRating,
                                                            Double maxFee) {
        return lawyerSearchService.filteredSearch(specialization, language, available, minRating, maxFee);
    }

    public Lawyer createLawyer(Lawyer lawyer) {
        lawyer.setId(UUID.randomUUID().toString());
        lawyerRepository.save(lawyer);
        lawyerSearchService.indexLawyer(lawyer);
        log.info("Created lawyer: id={}, name={}", lawyer.getId(), lawyer.getName());
        return lawyer;
    }

    public Lawyer updateLawyer(String id, Lawyer lawyer) {
        getLawyerById(id); // throws 404 if not found
        lawyer.setId(id);
        lawyerRepository.update(lawyer);
        lawyerSearchService.indexLawyer(lawyer);
        log.info("Updated lawyer: id={}", id);
        return lawyer;
    }

    public void updateAvailability(String id, boolean available) {
        getLawyerById(id);
        lawyerRepository.updateAvailability(id, available);
        Lawyer updated = getLawyerById(id);
        lawyerSearchService.indexLawyer(updated);
    }

    public void deleteLawyer(String id) {
        getLawyerById(id);
        lawyerRepository.deleteById(id);
        lawyerSearchService.deleteLawyer(id);
        log.info("Deleted lawyer: id={}", id);
    }

    /**
     * Bulk index all lawyers from MySQL into Elasticsearch (useful on startup / re-index).
     */
    public int reindexAll() {
        List<Lawyer> lawyers = lawyerRepository.findAll();
        lawyers.forEach(lawyerSearchService::indexLawyer);
        log.info("Re-indexed {} lawyers into Elasticsearch", lawyers.size());
        return lawyers.size();
    }

    // ==========================================
    // Helper
    // ==========================================
    private Map<String, Object> lawyerToMap(Lawyer l) {
        return Map.of(
                "id", l.getId(),
                "name", l.getName(),
                "specialization", l.getSpecialization(),
                "rating", l.getRating(),
                "location", l.getLocation(),
                "consultationFee", l.getConsultationFee(),
                "available", l.isAvailable(),
                "badge", l.getBadge() != null ? l.getBadge() : ""
        );
    }
}
