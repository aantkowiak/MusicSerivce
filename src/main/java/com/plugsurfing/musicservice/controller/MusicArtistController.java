package com.plugsurfing.musicservice.controller;

import com.plugsurfing.musicservice.service.musicartist.MusicArtistDto;
import com.plugsurfing.musicservice.service.musicartist.MusicArtistService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1")
@AllArgsConstructor
public class MusicArtistController {

    private final MusicArtistService musicArtistService;

    @GetMapping(value = "/musify/music-artist/details/{mbid}")
    public Mono<ResponseEntity<MusicArtistDto>> fetchMusicArtistDetails(@PathVariable String mbid) {
        return musicArtistService
                .fetchMusicArtist(mbid)
                .map(musicArtistDto -> ResponseEntity.status(HttpStatus.OK).body(musicArtistDto));
    }
}
