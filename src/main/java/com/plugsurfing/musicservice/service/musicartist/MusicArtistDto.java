package com.plugsurfing.musicservice.service.musicartist;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MusicArtistDto {

  String mbid;
  String name;
  String gender;
  String country;
  String disambiguation;
  String description;
  List<AlbumDto> albums;
}
