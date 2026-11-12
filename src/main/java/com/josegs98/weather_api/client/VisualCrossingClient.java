package com.josegs98.weather_api.client;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import com.josegs98.weather_api.constant.CacheNamesConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import com.josegs98.weather_api.client.dto.VisualCrossingResponse;
import com.josegs98.weather_api.config.VisualCrossingProperties;
import com.josegs98.weather_api.exception.CityNotFoundException;
import com.josegs98.weather_api.exception.ExternalApiException;
import com.josegs98.weather_api.exception.ExternalApiRateLimitException;

@Slf4j
@Component
public class VisualCrossingClient {

	private static final String ELEMENTS = "datetime,tempmax,tempmin,precipprob,conditions";

	private final RestClient restClient;
	private final VisualCrossingProperties properties;

	public VisualCrossingClient(RestClient.Builder builder, VisualCrossingProperties properties) {
		this.restClient = builder.baseUrl(properties.baseUrl()).build();
		this.properties = properties;
	}

	@Cacheable(CacheNamesConstant.FORECAST_TODAY)
	public VisualCrossingResponse getToday(String city) {
		return fetch(city, "/{city}/today", city);
	}

	@Cacheable(CacheNamesConstant.FORECAST_RANGE)
	public VisualCrossingResponse getRange(String city, LocalDate from, LocalDate to) {
		return fetch(city, "/{city}/{from}/{to}", city, from, to);
	}

	private VisualCrossingResponse fetch(String city, String path, Object... uriVariables) {
		try {
			return restClient.get()
					.uri(uriBuilder -> uriBuilder.path(path)
							.queryParam("key", properties.apiKey())
							.queryParam("unitGroup", properties.unitGroup())
							.queryParam("lang", properties.lang())
							.queryParam("include", "days")
							.queryParam("elements", ELEMENTS)
							.queryParam("contentType", "json")
							.build(uriVariables))
					.accept(MediaType.APPLICATION_JSON)
					.retrieve()
					.onStatus(HttpStatusCode::isError, (request, response) -> {
						String body = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
						throw translateError(city, response.getStatusCode(), body);
					})
					.body(VisualCrossingResponse.class);
		}
		catch (ResourceAccessException ex) {
			log.error("Error de conexión con Visual Crossing", ex);
			throw new ExternalApiException("No se pudo contactar con el servicio meteorológico", ex);
		}
	}

	private RuntimeException translateError(String city, HttpStatusCode status, String body) {
		if (status.value() == HttpStatus.BAD_REQUEST.value() && body != null && body.contains("Invalid location")) {
			return new CityNotFoundException(city);
		}
		if (status.value() == HttpStatus.UNAUTHORIZED.value()) {
			log.error("Visual Crossing rechazó la API key (401): {}", body);
		}
		else if (status.value() == HttpStatus.TOO_MANY_REQUESTS.value()) {
			log.warn("Visual Crossing: límite de peticiones excedido (429)");
			return new ExternalApiRateLimitException("Límite de consultas al servicio meteorológico excedido");
		}
		else {
			log.error("Visual Crossing respondió {}: {}", status.value(), body);
		}
		return new ExternalApiException("Error en el servicio meteorológico externo");
	}
}
