package com.plugsurfing.musicservice.service.wikipedia;

import io.micrometer.core.annotation.Timed;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static java.lang.String.format;

@Service
@AllArgsConstructor
@Slf4j
public class WikipediaService {

  private static final String DUMMY_DESCRIPTION =
      "This is a mocked description for the artist: %s. The artist is a great musician/singer/performer.";

  @Timed(value = "externalService.wikipedia.fetchWikipediaEntry")
  public Mono<WikipediaEntryDto> fetchWikipediaEntry(String url) {
    return Mono.just(new WikipediaEntryDto(format(DUMMY_DESCRIPTION, url)));
  }
}
