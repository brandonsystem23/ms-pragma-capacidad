package com.pragma.capacidad_service.infrastructure.out.webclient.adapter;

import com.pragma.capacidad_service.domain.model.Technology;
import com.pragma.capacidad_service.infrastructure.exception.ExternalServiceException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.List;

class TechnologyWebClientAdapterTest {

    private MockWebServer mockWebServer;
    private TechnologyWebClientAdapter technologyWebClientAdapter;

    @BeforeEach
    void setUp() throws IOException {

        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient technologyWebClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();

        technologyWebClientAdapter = new TechnologyWebClientAdapter(
                technologyWebClient,
                "/api/v1/technology/exists-by-ids",
                "/api/v1/technology/by-ids"
        );
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void shouldFindExistingTechnologyIdsSuccessfully() {

        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .addHeader("Content-Type", "application/json")
                        .setBody("""
                                {
                                    "existingIds": [1, 2, 3]
                                }
                                """)
        );

        StepVerifier.create(
                        technologyWebClientAdapter.existsByIds(
                                List.of(1L, 2L, 3L),
                                "token"
                        )
                )
                .assertNext(existingIds ->
                        Assertions.assertEquals(
                                List.of(1L, 2L, 3L),
                                existingIds
                        )
                )
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyListWhenNoTechnologiesExist() {

        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .addHeader("Content-Type", "application/json")
                        .setBody("""
                                {
                                    "existingIds": []
                                }
                                """)
        );

        StepVerifier.create(
                        technologyWebClientAdapter.existsByIds(
                                List.of(999L, 1000L),
                                "token"
                        )
                )
                .assertNext(existingIds ->
                        Assertions.assertTrue(existingIds.isEmpty())
                )
                .verifyComplete();
    }

    @Test
    void shouldFindTechnologiesByIdsSuccessfully() {

        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .addHeader("Content-Type", "application/json")
                        .setBody("""
                                [
                                    {
                                        "id": 1,
                                        "name": "Java"
                                    },
                                    {
                                        "id": 2,
                                        "name": "Spring"
                                    }
                                ]
                                """)
        );

        StepVerifier.create(
                        technologyWebClientAdapter.findByIds(
                                List.of(1L, 2L),
                                "token"
                        )
                )
                .assertNext(technologies -> {
                    Assertions.assertEquals(2, technologies.size());

                    Assertions.assertEquals(1L, technologies.get(0).getId());
                    Assertions.assertEquals("Java", technologies.get(0).getName());

                    Assertions.assertEquals(2L, technologies.get(1).getId());
                    Assertions.assertEquals("Spring", technologies.get(1).getName());
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyListWhenNoTechnologiesFoundByIds() {

        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .addHeader("Content-Type", "application/json")
                        .setBody("[]")
        );

        StepVerifier.create(
                        technologyWebClientAdapter.findByIds(
                                List.of(999L),
                                "token"
                        )
                )
                .assertNext(technologies ->
                        Assertions.assertTrue(technologies.isEmpty())
                )
                .verifyComplete();
    }

    @Test
    void shouldReturnExternalServiceExceptionWhenClientErrorOccurs() {

        enqueueErrorResponse(
                400,
                "Error consultando las tecnologías"
        );

        StepVerifier.create(
                        technologyWebClientAdapter.existsByIds(
                                List.of(1L, 2L, 3L),
                                "token"
                        )
                )
                .expectErrorSatisfies(throwable -> {

                    Assertions.assertInstanceOf(
                            ExternalServiceException.class,
                            throwable
                    );

                    ExternalServiceException exception =
                            (ExternalServiceException) throwable;

                    Assertions.assertEquals(
                            HttpStatus.BAD_REQUEST,
                            exception.getStatus()
                    );

                    Assertions.assertEquals(
                            "Error consultando las tecnologías",
                            exception.getMessage()
                    );
                })
                .verify();
    }

    @Test
    void shouldReturnDefaultMessageWhenClientErrorBodyIsEmpty() {

        enqueueErrorResponse(401, null);

        StepVerifier.create(
                        technologyWebClientAdapter.existsByIds(
                                List.of(1L, 2L, 3L),
                                "token"
                        )
                )
                .expectErrorSatisfies(throwable -> {

                    Assertions.assertInstanceOf(
                            ExternalServiceException.class,
                            throwable
                    );

                    ExternalServiceException exception =
                            (ExternalServiceException) throwable;

                    Assertions.assertEquals(
                            HttpStatus.UNAUTHORIZED,
                            exception.getStatus()
                    );

                    Assertions.assertEquals(
                            "Error al consultar las tecnologías",
                            exception.getMessage()
                    );
                })
                .verify();
    }

    @Test
    void shouldReturnExternalServiceExceptionWhenServerErrorOccurs() {

        enqueueErrorResponse(
                500,
                "Error interno del servicio de tecnologías"
        );

        StepVerifier.create(
                        technologyWebClientAdapter.existsByIds(
                                List.of(1L, 2L, 3L),
                                "token"
                        )
                )
                .expectErrorSatisfies(throwable -> {

                    Assertions.assertInstanceOf(
                            ExternalServiceException.class,
                            throwable
                    );

                    ExternalServiceException exception =
                            (ExternalServiceException) throwable;

                    Assertions.assertEquals(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            exception.getStatus()
                    );

                    Assertions.assertEquals(
                            "Error interno del servicio de tecnologías",
                            exception.getMessage()
                    );
                })
                .verify();
    }

    @Test
    void shouldReturnDefaultMessageWhenServerErrorBodyIsEmpty() {

        enqueueErrorResponse(503, null);

        StepVerifier.create(
                        technologyWebClientAdapter.existsByIds(
                                List.of(1L, 2L, 3L),
                                "token"
                        )
                )
                .expectErrorSatisfies(throwable -> {

                    Assertions.assertInstanceOf(
                            ExternalServiceException.class,
                            throwable
                    );

                    ExternalServiceException exception =
                            (ExternalServiceException) throwable;

                    Assertions.assertEquals(
                            HttpStatus.SERVICE_UNAVAILABLE,
                            exception.getStatus()
                    );

                    Assertions.assertEquals(
                            "Error interno del servicio de tecnologías",
                            exception.getMessage()
                    );
                })
                .verify();
    }

    @Test
    void shouldReturnExternalServiceExceptionWhenFindByIdsClientErrorOccurs() {

        enqueueErrorResponse(
                400,
                "Error consultando las tecnologías"
        );

        StepVerifier.create(
                        technologyWebClientAdapter.findByIds(
                                List.of(1L, 2L),
                                "token"
                        )
                )
                .expectErrorSatisfies(throwable -> {

                    Assertions.assertInstanceOf(
                            ExternalServiceException.class,
                            throwable
                    );

                    ExternalServiceException exception =
                            (ExternalServiceException) throwable;

                    Assertions.assertEquals(
                            HttpStatus.BAD_REQUEST,
                            exception.getStatus()
                    );

                    Assertions.assertEquals(
                            "Error consultando las tecnologías",
                            exception.getMessage()
                    );
                })
                .verify();
    }

    @Test
    void shouldReturnExternalServiceExceptionWhenFindByIdsServerErrorOccurs() {

        enqueueErrorResponse(
                500,
                "Error interno del servicio de tecnologías"
        );

        StepVerifier.create(
                        technologyWebClientAdapter.findByIds(
                                List.of(1L, 2L),
                                "token"
                        )
                )
                .expectErrorSatisfies(throwable -> {

                    Assertions.assertInstanceOf(
                            ExternalServiceException.class,
                            throwable
                    );

                    ExternalServiceException exception =
                            (ExternalServiceException) throwable;

                    Assertions.assertEquals(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            exception.getStatus()
                    );

                    Assertions.assertEquals(
                            "Error interno del servicio de tecnologías",
                            exception.getMessage()
                    );
                })
                .verify();
    }

    private void enqueueErrorResponse(
            int statusCode,
            String body
    ) {

        for (int i = 0; i < 3; i++) {

            MockResponse response = new MockResponse()
                    .setResponseCode(statusCode);

            if (body != null) {
                response.addHeader(
                        "Content-Type",
                        "text/plain"
                ).setBody(body);
            }

            mockWebServer.enqueue(response);
        }
    }

}