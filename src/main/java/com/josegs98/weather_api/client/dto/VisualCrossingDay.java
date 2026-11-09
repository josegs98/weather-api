package com.josegs98.weather_api.client.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record VisualCrossingDay(
		LocalDate datetime,
		Double tempmax,
		Double tempmin,
		Double precipprob,
		String conditions) {
}
