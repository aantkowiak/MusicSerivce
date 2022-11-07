package com.plugsurfing.musicservice.service.musicbrainz;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class IncorrectMbidParameterException extends ResponseStatusException {

  public IncorrectMbidParameterException() {
    this(HttpStatus.BAD_REQUEST, "Incorrect mbid parameter");
  }

  public IncorrectMbidParameterException(HttpStatus status, String message) {
    super(status, message);
  }
}
