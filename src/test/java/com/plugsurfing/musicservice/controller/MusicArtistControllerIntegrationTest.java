package com.plugsurfing.musicservice.controller;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.plugsurfing.musicservice.MusicServiceApplication;
import com.plugsurfing.musicservice.util.ResourceReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.lang.String.format;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = {MusicServiceApplication.class})
@AutoConfigureWebTestClient(timeout = "360000")
@ActiveProfiles("test")
class MusicArtistControllerIntegrationTest {

  private static final String MBID = "f90e8b26-9e52-4669-a5c9-e28529c47894";
  private static final String MUSIC_BRAINZ_PATH =
      format("/.org/ws/2/artist/%s?fmt=json&inc=url-rels+release-groups", MBID);

  public static final String COVER_ART_ARCHIVE_URL_PATTERN = "/release-group/.*";

  @Autowired public WebTestClient webTestClient;

  public static final WireMockServer MOCK_SERVER = new WireMockServer(wireMockConfig().port(2345));

  @BeforeEach
  void initClient() {
    initStubs();
    MOCK_SERVER.start();
  }

  @Test
  void fetchMusicArtistDetails() {
    webTestClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder.path("/api/v1/musify/music-artist/details/").path(MBID).build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.mbid")
        .isEqualTo(MBID)
        .jsonPath("$.name")
        .isEqualTo("The Snoop Dogg")
        .jsonPath("$.gender")
        .isEqualTo("Male")
        .jsonPath("$.country")
        .isEqualTo("US")
        .jsonPath("$.description")
        .isNotEmpty()
        .jsonPath("$.disambiguation")
        .isEqualTo("US rapper")
        .jsonPath("$.albums.length()")
        .isEqualTo(25)
        .jsonPath("$.albums[1].title")
        .isEqualTo("Tha Doggfather");
  }

  public void initStubs() {
    ResourceLoader resourceLoader = new DefaultResourceLoader();
    Resource musicBrainzResource =
        resourceLoader.getResource("classpath:__files/musicBrainzGetResponse.json");
    Resource coverArtArchiveGetResource =
        resourceLoader.getResource("classpath:__files/coverArtArchiveGetResponse.json");
    MOCK_SERVER.stubFor(
        get(urlEqualTo(MUSIC_BRAINZ_PATH))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", APPLICATION_JSON.toString())
                    .withBody(ResourceReader.asString(musicBrainzResource))));
    MOCK_SERVER.stubFor(
        get(urlMatching(COVER_ART_ARCHIVE_URL_PATTERN))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", APPLICATION_JSON.toString())
                    .withBody(ResourceReader.asString(coverArtArchiveGetResource))));
  }
}
