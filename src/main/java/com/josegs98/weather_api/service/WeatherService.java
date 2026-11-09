package com.josegs98.weather_api.service;

import java.time.LocalDate;

import com.josegs98.weather_api.dto.WeatherResponse;

public interface WeatherService {

	WeatherResponse getToday(String city);

	WeatherResponse getRange(String city, LocalDate from, LocalDate to);
}
