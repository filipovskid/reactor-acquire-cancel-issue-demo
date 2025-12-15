package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

@Configuration
public class WebClientConfig {
  
  @Bean
  public WebClient webClient() {
    Duration maxIdleTime = Duration.ofSeconds(55);
    ConnectionProvider connectionProvider = ConnectionProvider.builder("default")
        .maxConnections(1)
        .pendingAcquireMaxCount(1)
        .maxIdleTime(maxIdleTime)
        .metrics(true)
        .build();
    HttpClient httpClient = HttpClient.create(connectionProvider);
    ReactorClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);

    return WebClient.create()
        .mutate()
        .clientConnector(connector)
        .build();
  }
}
