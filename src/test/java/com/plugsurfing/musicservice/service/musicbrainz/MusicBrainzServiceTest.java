package com.plugsurfing.musicservice.service.musicbrainz;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.plugsurfing.musicservice.MusicServiceApplication;
import com.plugsurfing.musicservice.service.musicbrainz.dto.ArtistDto;
import com.plugsurfing.musicservice.util.ResourceReader;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ActiveProfiles;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = {MusicServiceApplication.class})
@AutoConfigureWebTestClient(timeout = "360000")
@ActiveProfiles("test")
final class MusicBrainzServiceTest {

  private static final String MBID = "f90e8b26-9e52-4669-a5c9-e28529c47894";
  private static final String MUSIC_BRAINZ_PATH =
      format("/.org/ws/2/artist/%s?fmt=json&inc=url-rels+release-groups", MBID);

  private static final WireMockServer wireMockServer =
      new WireMockServer(wireMockConfig().port(2345));

  @Autowired MusicBrainzService client;

  @BeforeAll
  static void init() {
    initStubs();
    wireMockServer.start();
  }

  @AfterAll
  static void shutDown() {
    wireMockServer.stop();
  }

  @Test
  void should_buildTheCorrectDto_when_mbidParamIsNull() {
    Exception exception =
        assertThrows(
            IncorrectMbidParameterException.class, () -> client.fetchArtistDetails(null).block());

    String expectedMessage = "Incorrect mbid parameter";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  void should_buildCorrectArtistDtoRootLevel_when_externalApiResponseIsCorrect() {
    ArtistDto artistDto = client.fetchArtistDetails(MBID).block();
    wireMockServer.verify(getRequestedFor(urlEqualTo(MUSIC_BRAINZ_PATH)));
    assertNotNull(artistDto);
    assertEquals(MBID, artistDto.getId());
    assertEquals("The Snoop Dogg", artistDto.getName());
    assertEquals("US", artistDto.getCountry());
    assertEquals("Male", artistDto.getGender());
    assertEquals("US rapper", artistDto.getDisambiguation());
  }

  @Test
  void should_buildCorrectRelationsDto_when_externalApiResponseIsCorrect() {
    ArtistDto artistDto = client.fetchArtistDetails(MBID).block();
    wireMockServer.verify(getRequestedFor(urlEqualTo(MUSIC_BRAINZ_PATH)));
    assertNotNull(artistDto);
    assertEquals(44, artistDto.getRelations().size());
    assertEquals("allmusic", artistDto.getRelations().get(0).getType());
    assertEquals(
        "https://www.allmusic.com/artist/mn0000029086",
        artistDto.getRelations().get(0).getUrl().getResource());
  }

  @Test
  void should_buildCorrectReleaseGroups_when_externalApiResponseIsCorrect() {
    ArtistDto artistDto = client.fetchArtistDetails(MBID).block();
    wireMockServer.verify(getRequestedFor(urlEqualTo(MUSIC_BRAINZ_PATH)));
    assertNotNull(artistDto);
    assertEquals(25, artistDto.getReleaseGroups().size());
    assertEquals(
        "626249ea-8291-3901-a294-881309b84dee", artistDto.getReleaseGroups().get(1).getId());
    assertEquals("Tha Doggfather", artistDto.getReleaseGroups().get(1).getTitle());
  }

  private static void initStubs() {
    ResourceLoader resourceLoader = new DefaultResourceLoader();
    Resource resource = resourceLoader.getResource("classpath:__files/musicBrainzGetResponse.json");
    wireMockServer.stubFor(
        get(urlEqualTo(MUSIC_BRAINZ_PATH))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", APPLICATION_JSON.toString())
                    .withBody(ResourceReader.asString(resource))));
  }
}
