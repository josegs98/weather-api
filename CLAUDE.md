# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

REST API (Java 21, Spring Boot 4.1.2, Gradle) that proxies the Visual Crossing Timeline API and returns, per day, only `datetime`, `tempmax`, `tempmin`, `precipprob` and `conditions`, plus the city's `resolvedAddress` and `timezone`. User-facing messages (errors, OpenAPI descriptions) are in Spanish.

Endpoints:
- `GET /api/v1/weather/{city}/today`
- `GET /api/v1/weather/{city}?from=yyyy-MM-dd&to=yyyy-MM-dd`

Swagger UI: `/swagger-ui.html` (springdoc 3.x, the line compatible with Boot 4).

## Commands

```bash
./gradlew build                      # compile + tests
./gradlew test                       # all tests
./gradlew test --tests '*WeatherControllerTest'                 # one class
./gradlew test --tests '*WeatherServiceImplTest.rejectsFromInThePast'  # one method
./gradlew bootRun                    # run on :8080
```

There is no linter configured.

## API key

The API key is never committed. `application.yaml` sets `visualcrossing.api-key: ${VISUAL_CROSSING_API_KEY:}` and imports `optional:classpath:application-local.yaml`. That file is git-ignored; `application-local.yaml.example` is the template. The app and the `contextLoads` test must keep starting with an empty key.

## Architecture

Request flow: `controller` → `service` → `client` → Visual Crossing, then `mapper` turns the external DTOs into the API's DTOs.

- **Two sets of DTOs.** `client/dto` (`VisualCrossingResponse`, `VisualCrossingDay`) mirror the external JSON field names and ignore unknown fields. `dto` (`WeatherResponse`, `DailyWeather`) is the public contract in camelCase (`tempMax`, `precipProb`, `date`). `WeatherMapper` is the only place that converts between them.
- **Validation is split.** Controller-level checks (date format, missing params, `@NotBlank` city) are handled by Spring and translated in `GlobalExceptionHandler`. The business rules for the date range live in `WeatherServiceImpl.validateRange`: `from <= to`, `from >= today`, and at most `weather.max-range-days` days (inclusive, default 7). "Today" comes from an injected `Clock` bean (server time zone), so tests use `Clock.fixed`.
- **The external client does the error translation.** `VisualCrossingClient` builds the URI (`/{city}/today` or `/{city}/{from}/{to}`) with the fixed query params (`key`, `unitGroup`, `lang`, `include=days`, `elements=...`, `contentType=json`) and maps HTTP errors to domain exceptions in `onStatus`:
  - a 400 whose body contains `Invalid location` → `CityNotFoundException`
  - a 429 → `ExternalApiRateLimitException`
  - any other error, and `ResourceAccessException` → `ExternalApiException`

  The 401 detail (bad API key) is logged only and is not shown to clients.
- **The error contract.** `GlobalExceptionHandler` returns `ProblemDetail` with these statuses: invalid input or range → 400, unknown city → 404, external failure → 502, rate limit → 503.
- **Configuration.** Typed records `VisualCrossingProperties` (`visualcrossing.*`: base-url, api-key, unit-group=metric, lang=es) and `WeatherProperties` (`weather.*`) are registered in `RestClientConfig`, which also provides the `Clock`.

## Testing conventions

Boot 4 moved the test slices into new packages:
- `org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest`
- `org.springframework.boot.restclient.test.autoconfigure.RestClientTest`

Mocks use `@MockitoBean`.

- Client: `@RestClientTest(VisualCrossingClient.class)` + `@Import(RestClientConfig.class)` + `@TestPropertySource`, with `MockRestServiceServer` mocking the external API.
- Controller: `@WebMvcTest` with a mocked `WeatherService`; the tests assert both the JSON body and the `ProblemDetail` fields.
- Service and mapper: plain JUnit 5 + Mockito, with no Spring context.
