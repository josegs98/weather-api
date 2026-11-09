package com.josegs98.weather_api.service.impl;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.josegs98.weather_api.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.josegs98.weather_api.client.VisualCrossingClient;
import com.josegs98.weather_api.config.WeatherProperties;
import com.josegs98.weather_api.dto.WeatherResponse;
import com.josegs98.weather_api.exception.InvalidDateRangeException;
import com.josegs98.weather_api.mapper.WeatherMapper;

@Service
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {

	private final VisualCrossingClient client;
	private final WeatherMapper mapper;
	private final WeatherProperties properties;
	private final Clock clock;

	@Override
	public WeatherResponse getToday(String city) {
		return mapper.toResponse(city, client.getToday(city));
	}

	@Override
	public WeatherResponse getRange(String city, LocalDate from, LocalDate to) {
		validateRange(from, to);
		return mapper.toResponse(city, client.getRange(city, from, to));
	}

	private void validateRange(LocalDate from, LocalDate to) {
		if (from.isAfter(to)) {
			throw new InvalidDateRangeException("La fecha 'from' no puede ser posterior a 'to'");
		}
		if (from.isBefore(LocalDate.now(clock))) {
			throw new InvalidDateRangeException("La fecha 'from' no puede ser anterior a hoy");
		}
		long days = ChronoUnit.DAYS.between(from, to) + 1;
		if (days > properties.maxRangeDays()) {
			throw new InvalidDateRangeException(
					"El rango no puede superar " + properties.maxRangeDays() + " días (solicitados: " + days + ")");
		}
	}
}
