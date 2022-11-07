package com.plugsurfing.musicservice.service.musicartist;

import com.plugsurfing.musicservice.service.coverartarchive.CoverArtArchiveService;
import com.plugsurfing.musicservice.service.coverartarchive.dto.CoverDto;
import com.plugsurfing.musicservice.service.musicbrainz.MusicBrainzService;
import com.plugsurfing.musicservice.service.musicbrainz.dto.ArtistDto;
import com.plugsurfing.musicservice.service.musicbrainz.dto.RelationDto;
import com.plugsurfing.musicservice.service.musicbrainz.dto.ReleaseGroupDto;
import com.plugsurfing.musicservice.service.musicbrainz.dto.UrlDto;
import com.plugsurfing.musicservice.service.wikidata.WikiDataService;
import com.plugsurfing.musicservice.service.wikipedia.WikipediaEntryDto;
import com.plugsurfing.musicservice.service.wikipedia.WikipediaService;
import io.micrometer.core.annotation.Timed;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

@Service
@AllArgsConstructor
public class MusicArtistService {

  private MusicBrainzService musicBrainzService;
  private CoverArtArchiveService coverArtArchiveService;

  private WikiDataService wikiDataService;

  private WikipediaService wikipediaService;

  @Timed(value = "externalService.musicArtistService.fetchMusicArtist")
  public Mono<MusicArtistDto> fetchMusicArtist(String mbid) {
    Mono<ArtistDto> musicArtistMono = musicBrainzService.fetchArtistDetails(mbid);
    return musicArtistMono.flatMap(
        (artist -> {
          List<String> albumIds = extractAlbumsIds(artist);

          String wikiDataResourceId = extractWikiDataResourceId(artist);

          Mono<List<CoverDto>> covers = coverArtArchiveService.fetchAlbumsCoverMetadata(albumIds);

          Mono<WikipediaEntryDto> wikipediaEntry =
              wikiDataService
                  .fetchWikipediaUrl(wikiDataResourceId)
                  .flatMap(url -> wikipediaService.fetchWikipediaEntry(url));

          Mono<Tuple2<List<CoverDto>, WikipediaEntryDto>> zippedCoversAndWikipediaData =
              Mono.zip(covers, wikipediaEntry);

          return zippedCoversAndWikipediaData.map(
              tuple -> buildMusicArtistData(artist, tuple.getT1(), tuple.getT2()));
        }));
  }

  private List<String> extractAlbumsIds(ArtistDto artist) {
    return artist.getReleaseGroups().stream().map(ReleaseGroupDto::getId).collect(toList());
  }

  private String extractWikiDataResourceId(ArtistDto artist) {
    return artist.getRelations().stream()
        .filter(relation -> "wikidata".equalsIgnoreCase(relation.getType()))
        .map(RelationDto::getUrl)
        .map(UrlDto::getResource)
        .findFirst()
        .orElse(null);
  }

  private MusicArtistDto buildMusicArtistData(
      ArtistDto artist, List<CoverDto> covers, WikipediaEntryDto wikipediaData) {

    Function<ReleaseGroupDto, AlbumDto> buildAlbumDto =
        releaseGroup ->
            AlbumDto.builder()
                .id(releaseGroup.getId())
                .title(releaseGroup.getTitle())
                .imageUrl(extractImageUrl(covers, releaseGroup.getId()))
                .build();

    return MusicArtistDto.builder()
        .mbid(artist.getId())
        .name(artist.getName())
        .gender(artist.getGender())
        .country(artist.getCountry())
        .disambiguation(artist.getDisambiguation())
        .albums(artist.getReleaseGroups().stream().map(buildAlbumDto).collect(toList()))
        .description(wikipediaData != null ? wikipediaData.getDescription() : null)
        .build();
  }

  private String extractImageUrl(List<CoverDto> covers, String releaseGroupId) {
    return covers.stream()
        .filter(cover -> releaseGroupId.equals(cover.getAlbumId()))
        .findFirst()
        .map(CoverDto::getImageUrl)
        .orElse(null);
  }
}
