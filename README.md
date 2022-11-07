## Table of Contents

- [Introduction](#introduction)
- [Features](#features)
- [Requirements](#requirements)
- [Local](#local)
- [Coding standards](#coding-standards)
- [Testing](#testing)
- [API](#api)
- [Monitoring](#monitoring)
- [Comments from the author](#comments-from-the-author)

## Introduction

The project is a REST API for providing clients with information about a specific music artist. The information is
collected from 4 different sources: MusicBrainz, Wikidata, Wikipedia and Cover Art Archive

- MusicBrainz offers an API returning detailed information about music artists e.g. name, birthday, birth country etc.
- WikiData acts as central storage for the structured data of its Wikimedia sister projects including Wikipedia,
  Wikivoyage, Wiktionary, Wikisource,
  and others.
- Wikipedia offers an API returning more descriptive information about many things, one of them are music
  artists.
- Cover Art Archive is a side-project to MusicBrainz which offers an API finding cover art for albums and singles for a
  specific music artist.

## Features

The project contains one REST API endpoint for providing clients with information about a specific music artist.

## Requirements

* [Java 11 SDK](https://www.oracle.com/java/technologies/downloads/#java11)
* [Maven](https://maven.apache.org/download.cgi)

## Local

```bash
$ mvn spring-boot:run
```

Application will run by default on port `8081`

Configure the port by changing `server.port` in __application.yml__

## Coding standards

- The code was formatted using IntelliJ IDEA's "google-java-format" plugin. Consider using the same plugin to keep the
  format of the code consistent.

## Testing

The project provides both unit and integration tests.

For manual testing purposes one can use a few example calls provided in "/test/resources/fetchMusicArtistDetails.http".

## API

The API takes an MBID (MusicBrainz Identifier) and returns a result containing the following:

- The description of the artist fetched from Wikipedia resource. Wikipedia does not contain any MBIDs so the mapping
  between MBID and
  Wik is done via MusicBrainz API.
- The list of all albums the artist has released and links to its corresponding album cover art. The albums can be found
  in MusicBrainz, but
  the cover art is found in Cover Art Archive.

**Get profile for an artist**

```http
GET /musify/music-artist/details/{mbid}
```

For example calls please check "/test/resources/fetchMusicArtistDetails.http".

**Response format**

```code
{
    "mbid" : string,
    "name" : string,
    "gender" : string,
    "country" : string,
    "disambiguation" : string,
    "description" : string,
    "albums" : [
    {
      "id": string,
      "title": string,
      "imageUrl": string,
    }, ...
  ]
}
```

## Monitoring

Metrics are provided by Prometheus under: [Prometheus](http://localhost:8081/actuator/prometheus). Methods calling third-party
functionalities are annotated by `@Timed`. The naming convention
is `"externalService.<externalServiceName>.<methodName>"` (e.g. externalService.musicBrainzService.fetchArtistDetails).
The Prometheus client is not provided by the project.



## Comments from the author

### Comments related to the implementation of business features:

- Only two endpoints were implemented. For other two endpoints the relation between services was reflected (Wikipedia's 
  call depends on the Wikidata response). However, the structure of DTOs for both services was flattened and does not 
  correspond to the structure that would be required to map the data from the real endpoints.
- The URL of the API proposed in the example response (PDF with assignment) contains a business name "musify" which is 
  not recommended. Business names tend to change over time. That causes situation when a project/service has many 
  aliases which is confusing.

### Error handling:

The error handling part in the project is just symbolic. One should consider how the system should respond to failures
of attempts to fetch data from external system. It usually depends on the business case. If an external API is failing
we typically respond to the end client with an error, but in some cases we can return a partial response (optionally
enriching the response with a status indicating that the response is incomplete combined with some more details).

### Validation:

Another aspect that one should consider is validation of the quality of a third party response. For this particular
business case is seems to be not crucial, but there are plenty of cases (especially when dealing with legacy systems)
when verification of the logical/business/consistency-wise correctness of data is vital.

### Logging:

The logging implemented in the code is symbolic. Usually, in real business cases, an increase of the amount of business
logic is inevitable. When that happens, it is worth to utilize multiple levels of logging - not only error, info, warn,
but also debug and trace, to provide convenient ways of analysing more complex parts of the business logic in the
application.

### Metrics:

As it was already mentioned above in the readme: the client for metrics reading is not included in the project build.
For a regular production application a proper monitoring setup is needed - including both visualization of the metrics
and alarming. It is also valuable to document the metrics, describing what they measure and how they are correlated with
each other. This kind of information is can be combined with a set of instruction, how to behave when a particular alarm
is triggered - links to rollback procedures, who to notify, how to change the configuration to reduce the impact, etc.

### Cache:

The code utilizes caching mechanism provided by spring-boot. For a production app that requires high performance, it is
worth to consider another implementation of caching - Spring provides also some specific caching implementations like
EhCache and Caffeine.
When thinking about scaling the application - if we assume that the application is able to handle the predicted load
being scaled vertically, then keeping the caching mechanism within the application might be worth to consider. However,
if the predictions are that we will be forces to scale the application horizontally, then it is strongly recommended to
consider an external caching module (e.g. AWS ElastiCache/Redis).

### Documentation:

The project is missing a proper API documentation in a form of Swagger docs.

### Costs:

It is always worth to calculate different technical options for a particular business case. Some business cases can fit
more within a vertical scaling and other for a horizontal scaling. Using an out-of-the-box functionality like external
module for caching might be more cost-effective than implementing the same solution inside the application. A modern
developer should always care to consider the development process from the client perspective. The client is often
interested in the cost of both - development and maintenance. The cloud solutions offer a lot of tools that have
predictable maintenance cost and strongly reduce the development costs.

