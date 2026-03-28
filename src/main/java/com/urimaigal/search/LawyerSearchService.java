package com.urimaigal.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import com.urimaigal.model.Lawyer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

@Service
public class LawyerSearchService {

    private static final Logger log = LoggerFactory.getLogger(LawyerSearchService.class);

    private final ElasticsearchClient esClient;
    private final String lawyersIndex;

    public LawyerSearchService(ElasticsearchClient esClient,
                               @Value("${elasticsearch.index.lawyers}") String lawyersIndex) {
        this.esClient = esClient;
        this.lawyersIndex = lawyersIndex;
    }

    /**
     * Create the lawyers index if it doesn't already exist.
     */
    @PostConstruct
    public void initIndex() {
        try {
            boolean exists = esClient.indices()
                    .exists(ExistsRequest.of(e -> e.index(lawyersIndex)))
                    .value();
            if (!exists) {
                esClient.indices().create(CreateIndexRequest.of(c -> c
                        .index(lawyersIndex)
                        .mappings(m -> m
                                .properties("id",            p -> p.keyword(k -> k))
                                .properties("name",          p -> p.text(t -> t.analyzer("standard")))
                                .properties("specialization",p -> p.keyword(k -> k))
                                .properties("location",      p -> p.keyword(k -> k))
                                .properties("languages",     p -> p.keyword(k -> k))
                                .properties("bio",           p -> p.text(t -> t.analyzer("standard")))
                                .properties("rating",        p -> p.double_(d -> d))
                                .properties("consultationFee", p -> p.double_(d -> d))
                                .properties("available",     p -> p.boolean_(b -> b))
                                .properties("badge",         p -> p.keyword(k -> k))
                        )
                ));
                log.info("Elasticsearch index '{}' created", lawyersIndex);
            } else {
                log.info("Elasticsearch index '{}' already exists", lawyersIndex);
            }
        } catch (Exception e) {
            log.warn("Could not initialise Elasticsearch index '{}': {}", lawyersIndex, e.getMessage());
        }
    }

    /**
     * Index (upsert) a single lawyer document.
     */
    public void indexLawyer(Lawyer lawyer) {
        try {
            Map<String, Object> doc = toDocument(lawyer);
            esClient.index(IndexRequest.of(i -> i
                    .index(lawyersIndex)
                    .id(lawyer.getId())
                    .document(doc)
            ));
            log.debug("Indexed lawyer '{}' in Elasticsearch", lawyer.getId());
        } catch (IOException e) {
            log.error("Failed to index lawyer '{}': {}", lawyer.getId(), e.getMessage());
        }
    }

    /**
     * Full-text search across name, bio, specialization, location.
     */
    public List<Map<String, Object>> fullTextSearch(String query) {
        try {
            SearchResponse<Map> response = esClient.search(s -> s
                    .index(lawyersIndex)
                    .query(q -> q
                            .multiMatch(mm -> mm
                                    .query(query)
                                    .fields("name^3", "specialization^2", "location^2", "bio", "languages")
                                    .fuzziness("AUTO")
                            )
                    )
                    .size(20),
                    Map.class
            );
            return extractHits(response);
        } catch (IOException e) {
            log.error("Elasticsearch full-text search failed for query '{}': {}", query, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Filter search with bool query.
     */
    public List<Map<String, Object>> filteredSearch(String specialization, String language,
                                                     Boolean available, Double minRating,
                                                     Double maxFee) {
        try {
            SearchResponse<Map> response = esClient.search(s -> s
                    .index(lawyersIndex)
                    .query(q -> q
                            .bool(b -> {
                                if (specialization != null && !specialization.equals("All")) {
                                    b.filter(f -> f.term(t -> t.field("specialization").value(specialization)));
                                }
                                if (language != null && !language.equals("All")) {
                                    b.filter(f -> f.term(t -> t.field("languages").value(language)));
                                }
                                if (available != null && available) {
                                    b.filter(f -> f.term(t -> t.field("available").value(true)));
                                }
                                if (minRating != null) {
                                    b.filter(f -> f.range(r -> r.field("rating").gte(co.elastic.clients.json.JsonData.of(minRating))));
                                }
                                if (maxFee != null) {
                                    b.filter(f -> f.range(r -> r.field("consultationFee").lte(co.elastic.clients.json.JsonData.of(maxFee))));
                                }
                                return b;
                            })
                    )
                    .sort(sort -> sort.field(f -> f.field("rating").order(co.elastic.clients.elasticsearch._types.SortOrder.Desc)))
                    .size(50),
                    Map.class
            );
            return extractHits(response);
        } catch (IOException e) {
            log.error("Elasticsearch filtered search failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Delete a lawyer document from the index.
     */
    public void deleteLawyer(String lawyerId) {
        try {
            esClient.delete(DeleteRequest.of(d -> d.index(lawyersIndex).id(lawyerId)));
            log.debug("Deleted lawyer '{}' from Elasticsearch", lawyerId);
        } catch (IOException e) {
            log.error("Failed to delete lawyer '{}' from Elasticsearch: {}", lawyerId, e.getMessage());
        }
    }

    // ==========================================
    // Helpers
    // ==========================================

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractHits(SearchResponse<Map> response) {
        List<Map<String, Object>> results = new ArrayList<>();
        for (Hit<Map> hit : response.hits().hits()) {
            if (hit.source() != null) {
                results.add(hit.source());
            }
        }
        return results;
    }

    private Map<String, Object> toDocument(Lawyer lawyer) {
        Map<String, Object> doc = new HashMap<>();
        doc.put("id", lawyer.getId());
        doc.put("name", lawyer.getName());
        doc.put("avatar", lawyer.getAvatar());
        doc.put("specialization", lawyer.getSpecialization());
        doc.put("experience", lawyer.getExperience());
        doc.put("rating", lawyer.getRating());
        doc.put("reviewCount", lawyer.getReviewCount());
        doc.put("location", lawyer.getLocation());
        doc.put("languages", lawyer.getLanguages());
        doc.put("consultationFee", lawyer.getConsultationFee());
        doc.put("bio", lawyer.getBio());
        doc.put("available", lawyer.isAvailable());
        doc.put("badge", lawyer.getBadge());
        return doc;
    }
}
