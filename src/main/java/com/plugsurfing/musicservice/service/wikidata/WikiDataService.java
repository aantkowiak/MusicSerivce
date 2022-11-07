package com.plugsurfing.musicservice.service.wikidata;

import io.micrometer.core.annotation.Timed;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
@Slf4j
public class WikiDataService {

  private static final String DUMMY_WIKIPEDIA_URL =
      "https://en.wikipedia.org/api/rest_v1/page/summary/Michael_Jackson";

  @Timed(value = "externalService.wikiDataService.fetchWikipediaUrl")
  public Mono<String> fetchWikipediaUrl(String resourceId) {
    log.info("Fetching wikipedia url for resource: {}", resourceId);
    return Mono.just(DUMMY_WIKIPEDIA_URL);
  }
}
