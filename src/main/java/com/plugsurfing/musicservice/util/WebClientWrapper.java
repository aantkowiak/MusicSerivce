package com.plugsurfing.musicservice.util;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.net.URI;
import java.util.function.Function;

import static java.time.Duration.ofMillis;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Service
@AllArgsConstructor
@Slf4j
public class WebClientWrapper {

  private static final int TIMEOUT = 20000;

  private static final HttpClient client =
      HttpClient.create().responseTimeout(ofMillis(TIMEOUT)).followRedirect(true);

  public <T> Mono<T> httpGet(
      String baseUrl,
      Function<UriBuilder, URI> buildUri,
      ParameterizedTypeReference<T> typeReference) {
    return WebClient.builder()
        .baseUrl(baseUrl)
        .clientConnector(new ReactorClientHttpConnector(client))
        .build()
        .get()
        .uri(buildUri)
        .accept(APPLICATION_JSON)
        .retrieve()
        .bodyToMono(typeReference);
  }
}
