package com.jobsearch.service;

import com.jobsearch.dto.JSearchDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class JSearchService {

    @Value("${jsearch.api.url}")
    private String apiUrl;

    @Value("${jsearch.api.key}")
    private String apiKey;

    @Value("${jsearch.api.host}")
    private String apiHost;

    private final WebClient.Builder webClientBuilder;

    public JSearchDTO.JobSearchResponse searchJobs(String query, String location, Integer page) {
        try {
            WebClient webClient = webClientBuilder.build();

            Mono<JSearchDTO.JobSearchResponse> response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host(apiHost)
                            .path("/search")
                            .queryParam("query", query)
                            .queryParam("page", page != null ? page : 1)
                            .queryParam("num_pages", 1)
                            .build())
                    .header("X-RapidAPI-Key", apiKey)
                    .header("X-RapidAPI-Host", apiHost)
                    .retrieve()
                    .bodyToMono(JSearchDTO.JobSearchResponse.class);

            return response.block();

        } catch (Exception e) {
            log.error("Erro ao buscar vagas no JSearch: ", e);
            throw new RuntimeException("Erro ao buscar vagas", e);
        }
    }

    public JSearchDTO.JobSearchResponse searchJobsWithFilters(String query, String location,
                                                              String employmentType, Integer page) {
        try {
            WebClient webClient = webClientBuilder.build();

            final String searchQuery = buildSearchQuery(query, location);
            final String finalEmploymentType = employmentType;
            final Integer finalPage = page != null ? page : 1;

            Mono<JSearchDTO.JobSearchResponse> response = webClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder
                                .scheme("https")
                                .host(apiHost)
                                .path("/search")
                                .queryParam("query", searchQuery)
                                .queryParam("page", finalPage)
                                .queryParam("num_pages", 1);

                        if (finalEmploymentType != null && !finalEmploymentType.isEmpty()) {
                            builder = builder.queryParam("employment_types", finalEmploymentType);
                        }

                        return builder.build();
                    })
                    .header("X-RapidAPI-Key", apiKey)
                    .header("X-RapidAPI-Host", apiHost)
                    .retrieve()
                    .bodyToMono(JSearchDTO.JobSearchResponse.class);

            return response.block();

        } catch (Exception e) {
            log.error("Erro ao buscar vagas com filtros no JSearch: ", e);
            throw new RuntimeException("Erro ao buscar vagas", e);
        }
    }

    private String buildSearchQuery(String query, String location) {
        String fullQuery = query;
        if (location != null && !location.isEmpty()) {
            fullQuery += " in " + location;
        }
        return fullQuery;
    }
}