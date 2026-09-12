package com.pragma.capacidad_service.infrastructure.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean(name = "technologyWebClient")
    public WebClient technologyWebClient(
            WebClient.Builder builder,
            @Value("${clients.technology.base-url}") String usersBaseUrl
    ) {
        return builder
                .baseUrl(usersBaseUrl)
                .build();
    }

}
