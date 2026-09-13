package com.pragma.capacidad_service.infrastructure.out.webclient.adapter;

import com.pragma.capacidad_service.domain.model.Technology;
import com.pragma.capacidad_service.domain.spi.ITechnologyWebClientPort;
import com.pragma.capacidad_service.infrastructure.exception.ExternalServiceException;
import com.pragma.capacidad_service.infrastructure.out.webclient.dto.TechnologyDetailResponse;
import com.pragma.capacidad_service.infrastructure.out.webclient.dto.TechnologyRequest;
import com.pragma.capacidad_service.infrastructure.out.webclient.dto.TechnologyResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TechnologyWebClientAdapter implements ITechnologyWebClientPort {

    private final WebClient technologyWebClient;
    private final String technologyPath;
    private final String technologyByIdsPath;

    public TechnologyWebClientAdapter(
            @Qualifier("technologyWebClient") WebClient technologyWebClient,
            @Value("${clients.technology.path}") String technologyPath,
            @Value("${clients.technology.by-ids-path}") String technologyByIdsPath) {
        this.technologyWebClient = technologyWebClient;
        this.technologyPath = technologyPath;
        this.technologyByIdsPath = technologyByIdsPath;
    }

    @Override
    public Mono<List<Long>> existsByIds(List<Long> ids, String token) {

        Map<String, String> mapHeaders = new HashMap<>();
        mapHeaders.put(HttpHeaders.AUTHORIZATION, "Bearer " + token);

        return bodyClientPath(technologyWebClient, technologyPath, mapHeaders, ids)
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        this::handleClientError
                )
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        this::handleServerError
                )
                .bodyToMono(TechnologyResponse.class)
                .map(TechnologyResponse::existingIds)
                .retry(2);
    }

    @Override
    public Mono<List<Technology>> findByIds(List<Long> ids, String token) {

        return getClientPath(technologyWebClient, technologyByIdsPath, token, ids)
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        this::handleClientError
                )
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        this::handleServerError
                )
                .bodyToFlux(TechnologyDetailResponse.class)
                .map(response -> Technology.builder()
                        .id(response.id())
                        .name(response.name())
                        .build())
                .collectList()
                .retry(2);
    }

    private static WebClient.ResponseSpec bodyClientPath(
            WebClient webClient,
            String path,
            Map<String, String> mapHeaders,
            List<Long> ids) {

        log.info("Consultando tecnologías con IDs: {}", ids);

        TechnologyRequest request = TechnologyRequest.builder()
                .ids(ids)
                .build();

        return webClient.post()
                .uri(path)
                .headers(httpHeaders -> mapHeaders.forEach(httpHeaders::set))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve();
    }

    private static WebClient.ResponseSpec getClientPath(
            WebClient webClient,
            String path,
            String token,
            List<Long> ids) {

        String idsParam = ids.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        log.info("Consultando tecnologías por IDs: {}", ids);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParam("ids", idsParam)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve();
    }

    private Mono<Throwable> handleClientError(ClientResponse clientResponse) {
        return clientResponse
                .bodyToMono(String.class)
                .defaultIfEmpty("Error al consultar las tecnologías")
                .flatMap(message -> Mono.error(
                        new ExternalServiceException(
                                HttpStatus.valueOf(clientResponse.statusCode().value()),
                                message
                        )
                ));
    }

    private Mono<Throwable> handleServerError(ClientResponse clientResponse) {
        return clientResponse
                .bodyToMono(String.class)
                .defaultIfEmpty("Error interno del servicio de tecnologías")
                .flatMap(message -> Mono.error(
                        new ExternalServiceException(
                                HttpStatus.valueOf(clientResponse.statusCode().value()),
                                message
                        )
                ));
    }
}
