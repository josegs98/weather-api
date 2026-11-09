package com.josegs98.weather_api.dto;

import java.time.LocalDate;

public record DailyWeather(
		LocalDate date,
		Double tempMax,
		Double tempMin,
		Double precipProb,
		String conditions) {
}
