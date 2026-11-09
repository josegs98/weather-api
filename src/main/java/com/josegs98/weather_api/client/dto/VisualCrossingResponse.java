package com.josegs98.weather_api.client.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record VisualCrossingResponse(
		String resolvedAddress,
		String timezone,
		List<VisualCrossingDay> days) {
}
