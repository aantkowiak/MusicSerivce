package com.plugsurfing.musicservice.service.musicartist;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class AlbumDto {
  String id;
  String title;
  String imageUrl;
}
