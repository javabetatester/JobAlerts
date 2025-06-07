package com.jobsearch.service;

import com.jobsearch.dto.JSearchDTO;
import com.jobsearch.exception.JSearchApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

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

    private final RestTemplate restTemplate;

    public JSearchDTO.JobSearchResponse searchJobs(String query, String location, Integer page) {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Query de busca não pode ser vazia");
        }

        try {
            log.info("Buscando vagas - Query: '{}', Location: '{}', Page: {}", query, location, page);

            String finalQuery = buildSearchQuery(query.trim(), location);
            Integer finalPage = page != null ? page : 1;

            URI uri = UriComponentsBuilder
                    .fromHttpUrl("https://" + apiHost + "/search")
                    .queryParam("query", finalQuery)
                    .queryParam("page", finalPage)
                    .queryParam("num_pages", 1)
                    .build()
                    .toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-RapidAPI-Key", apiKey);
            headers.set("X-RapidAPI-Host", apiHost);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            log.debug("URL da requisição: {}", uri);

            ResponseEntity<JSearchDTO.JobSearchResponse> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    entity,
                    JSearchDTO.JobSearchResponse.class
            );

            JSearchDTO.JobSearchResponse result = response.getBody();

            if (result == null) {
                log.warn("Resposta da API JSearch é null para query: {}", finalQuery);
                return createEmptyResponse();
            }

            log.info("API JSearch retornou {} vagas para query: '{}'",
                    result.getData() != null ? result.getData().size() : 0, finalQuery);

            return result;

        } catch (HttpClientErrorException.Unauthorized e) {
            log.error("Erro de autorização na API JSearch - verifique a chave da API");
            throw new JSearchApiException("Erro de autorização na API JSearch", e);
        } catch (HttpClientErrorException.TooManyRequests e) {
            log.error("Rate limit excedido na API JSearch");
            throw new JSearchApiException("Rate limit excedido na API JSearch", e);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Erro HTTP na API JSearch: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new JSearchApiException("Erro HTTP na API JSearch: " + e.getStatusCode(), e);
        } catch (Exception e) {
            log.error("Erro inesperado ao buscar vagas no JSearch: ", e);
            throw new JSearchApiException("Erro inesperado na busca de vagas", e);
        }
    }

    public JSearchDTO.JobSearchResponse searchJobsWithFilters(String query, String location,
                                                              String employmentType, Integer page) {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Query de busca não pode ser vazia");
        }

        try {
            log.info("Buscando vagas com filtros - Query: '{}', Location: '{}', Type: '{}', Page: {}",
                    query, location, employmentType, page);

            String finalQuery = buildSearchQuery(query.trim(), location);
            String finalEmploymentType = employmentType != null ? employmentType.trim() : null;
            Integer finalPage = page != null ? page : 1;

            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromHttpUrl("https://" + apiHost + "/search")
                    .queryParam("query", finalQuery)
                    .queryParam("page", finalPage)
                    .queryParam("num_pages", 1);

            if (finalEmploymentType != null && !finalEmploymentType.isEmpty()) {
                builder = builder.queryParam("employment_types", finalEmploymentType);
                log.debug("Adicionado filtro de tipo de emprego: {}", finalEmploymentType);
            }

            URI uri = builder.build().toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-RapidAPI-Key", apiKey);
            headers.set("X-RapidAPI-Host", apiHost);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            log.debug("URL da requisição com filtros: {}", uri);

            ResponseEntity<JSearchDTO.JobSearchResponse> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    entity,
                    JSearchDTO.JobSearchResponse.class
            );

            JSearchDTO.JobSearchResponse result = response.getBody();

            if (result == null) {
                log.warn("Resposta da API JSearch é null para query com filtros: {}", finalQuery);
                return createEmptyResponse();
            }

            log.info("API JSearch retornou {} vagas para query com filtros: '{}'",
                    result.getData() != null ? result.getData().size() : 0, finalQuery);

            return result;

        } catch (HttpClientErrorException.Unauthorized e) {
            log.error("Erro de autorização na API JSearch - verifique a chave da API");
            throw new JSearchApiException("Erro de autorização na API JSearch", e);
        } catch (HttpClientErrorException.TooManyRequests e) {
            log.error("Rate limit excedido na API JSearch");
            throw new JSearchApiException("Rate limit excedido na API JSearch", e);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Erro HTTP na API JSearch: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new JSearchApiException("Erro HTTP na API JSearch: " + e.getStatusCode(), e);
        } catch (Exception e) {
            log.error("Erro inesperado ao buscar vagas com filtros no JSearch: ", e);
            throw new JSearchApiException("Erro inesperado na busca de vagas", e);
        }
    }

    private String buildSearchQuery(String query, String location) {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Query não pode ser vazia");
        }

        String finalQuery = query.trim();

        if (location != null && !location.trim().isEmpty()) {
            finalQuery += " in " + location.trim();
        }

        log.debug("Query construída: '{}'", finalQuery);
        return finalQuery;
    }

    private JSearchDTO.JobSearchResponse createEmptyResponse() {
        JSearchDTO.JobSearchResponse emptyResponse = new JSearchDTO.JobSearchResponse();
        emptyResponse.setStatus("success");
        emptyResponse.setData(java.util.Collections.emptyList());
        emptyResponse.setCount(0);

        JSearchDTO.Parameters parameters = new JSearchDTO.Parameters();
        parameters.setQuery("");
        parameters.setPage(1);
        parameters.setNumPages(0);
        emptyResponse.setParameters(parameters);

        return emptyResponse;
    }
}