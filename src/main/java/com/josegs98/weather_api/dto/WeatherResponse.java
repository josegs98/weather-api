package com.josegs98.weather_api.dto;

import java.util.List;

public record WeatherResponse(
		String city,
		String resolvedAddress,
		String timezone,
		List<DailyWeather> days) {
}
