package com.plugsurfing.musicservice.service.musicbrainz;

import com.plugsurfing.musicservice.config.ExternalServicesProperties;
import com.plugsurfing.musicservice.service.musicbrainz.dto.ArtistDto;
import com.plugsurfing.musicservice.util.WebClientWrapper;
import io.micrometer.core.annotation.Timed;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.function.Function;

import static org.springframework.util.StringUtils.hasLength;

@Service
@AllArgsConstructor
@Slf4j
public class MusicBrainzService {

  private static final ParameterizedTypeReference<ArtistDto> TYPE_REFERENCE =
      new ParameterizedTypeReference<>() {};

  private ExternalServicesProperties serviceProperties;

  private WebClientWrapper webClientWrapper;

  @Timed(value = "externalService.musicBrainzService.fetchArtistDetails")
  @Cacheable("musicBrainzArtistDto")
  public Mono<ArtistDto> fetchArtistDetails(String mbid) {
    if (!hasLength(mbid)) {
      log.warn("Incorrect mbid parameter: {}", mbid);
      return Mono.error(new IncorrectMbidParameterException());
    }

    log.info("Fetching artist details for mbid: {}", mbid);
    String baseUrl = serviceProperties.getMusicBrainz().getBaseUrl();
    Function<UriBuilder, URI> buildUri =
        uriBuilder ->
            uriBuilder
                .path(mbid)
                .queryParam("fmt", "json")
                .queryParam("inc", "url-rels+release-groups")
                .build();
    return webClientWrapper.httpGet(baseUrl, buildUri, TYPE_REFERENCE);
  }
}
