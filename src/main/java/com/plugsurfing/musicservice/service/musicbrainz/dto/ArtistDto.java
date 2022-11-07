package com.plugsurfing.musicservice.service.musicbrainz.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(NON_NULL)
public class ArtistDto {
  private String id;
  private String name;
  private String gender;
  private String country;
  private String disambiguation;
  private List<RelationDto> relations;

  @JsonProperty("release-groups")
  private List<ReleaseGroupDto> releaseGroups;
}
