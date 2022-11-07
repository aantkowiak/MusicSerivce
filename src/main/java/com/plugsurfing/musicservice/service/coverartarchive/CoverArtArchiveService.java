package com.plugsurfing.musicservice.service.coverartarchive;

import com.plugsurfing.musicservice.config.ExternalServicesProperties;
import com.plugsurfing.musicservice.service.coverartarchive.dto.CoverDto;
import com.plugsurfing.musicservice.service.coverartarchive.dto.CoverImageDto;
import com.plugsurfing.musicservice.service.coverartarchive.dto.CoverImagesDto;
import com.plugsurfing.musicservice.util.WebClientWrapper;
import io.micrometer.core.annotation.Timed;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

@Service
@AllArgsConstructor
@Slf4j
public class CoverArtArchiveService {

  private static final ParameterizedTypeReference<CoverImagesDto> TYPE_REFERENCE =
      new ParameterizedTypeReference<>() {};
  private ExternalServicesProperties serviceProperties;

  private WebClientWrapper webClientWrapper;

  @Timed(value = "externalService.coverArtArchive.fetchAlbumsCoverMetadata")
  public Mono<List<CoverDto>> fetchAlbumsCoverMetadata(List<String> albumIds) {
    return Flux.fromIterable(albumIds)
        .flatMap(
            albumId ->
                fetchAlbumCoverMetadata(albumId)
                    .map(
                        coverImagesDto ->
                            CoverDto.builder()
                                .albumId(albumId)
                                .imageUrl(
                                    coverImagesDto.getImages().stream()
                                        .findFirst()
                                        .map(CoverImageDto::getImage)
                                        .orElse(null))
                                .build()))
        .collect(toList());
  }

  @Timed(value = "externalService.coverArtArchive.fetchAlbumCoverData")
  private Mono<CoverImagesDto> fetchAlbumCoverMetadata(String albumId) {
    log.info("Fetching data with album id: {}", albumId);
    String baseUrl = serviceProperties.getCoverArtArchive().getBaseUrl();
    Function<UriBuilder, URI> buildUri = uriBuilder -> uriBuilder.path(albumId).build();
    return webClientWrapper.httpGet(baseUrl, buildUri, TYPE_REFERENCE);
  }
}
