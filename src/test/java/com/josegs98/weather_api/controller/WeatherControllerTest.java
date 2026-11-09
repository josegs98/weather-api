package com.josegs98.weather_api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.josegs98.weather_api.dto.DailyWeather;
import com.josegs98.weather_api.dto.WeatherResponse;
import com.josegs98.weather_api.exception.CityNotFoundException;
import com.josegs98.weather_api.exception.ExternalApiException;
import com.josegs98.weather_api.exception.ExternalApiRateLimitException;
import com.josegs98.weather_api.exception.InvalidDateRangeException;
import com.josegs98.weather_api.service.WeatherService;

@WebMvcTest(WeatherController.class)
class WeatherControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private WeatherService weatherService;

	private final WeatherResponse sample = new WeatherResponse("Madrid", "Madrid, España", "Europe/Madrid",
			List.of(new DailyWeather(LocalDate.of(2026, 11, 9), 17.2, 8.1, 35.0, "Despejado")));

	@Test
	void todayReturns200() throws Exception {
		when(weatherService.getToday("Madrid")).thenReturn(sample);

		mockMvc.perform(get("/api/v1/weather/Madrid/today"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.city").value("Madrid"))
				.andExpect(jsonPath("$.timezone").value("Europe/Madrid"))
				.andExpect(jsonPath("$.days[0].date").value("2026-11-09"))
				.andExpect(jsonPath("$.days[0].tempMax").value(17.2))
				.andExpect(jsonPath("$.days[0].tempMin").value(8.1))
				.andExpect(jsonPath("$.days[0].precipProb").value(35.0))
				.andExpect(jsonPath("$.days[0].conditions").value("Despejado"));
	}

	@Test
	void rangeReturns200() throws Exception {
		when(weatherService.getRange("Madrid", LocalDate.of(2026, 11, 9), LocalDate.of(2026, 11, 12)))
				.thenReturn(sample);

		mockMvc.perform(get("/api/v1/weather/Madrid").param("from", "2026-11-09").param("to", "2026-11-12"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.resolvedAddress").value("Madrid, España"));
	}

	@Test
	void malformedDateReturns400() throws Exception {
		mockMvc.perform(get("/api/v1/weather/Madrid").param("from", "2026-13-01").param("to", "2026-11-12"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("from")));
	}

	@Test
	void missingParamReturns400() throws Exception {
		mockMvc.perform(get("/api/v1/weather/Madrid").param("from", "2026-11-09"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("to")));
	}

	@Test
	void invalidRangeReturns400() throws Exception {
		when(weatherService.getRange(eq("Madrid"), any(), any()))
				.thenThrow(new InvalidDateRangeException("La fecha 'from' no puede ser posterior a 'to'"));

		mockMvc.perform(get("/api/v1/weather/Madrid").param("from", "2026-11-12").param("to", "2026-11-09"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title").value("Rango de fechas inválido"));
	}

	@Test
	void cityNotFoundReturns404() throws Exception {
		when(weatherService.getToday("xxqqzz")).thenThrow(new CityNotFoundException("xxqqzz"));

		mockMvc.perform(get("/api/v1/weather/xxqqzz/today"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));
	}

	@Test
	void externalErrorReturns502() throws Exception {
		when(weatherService.getToday("Madrid")).thenThrow(new ExternalApiException("boom"));

		mockMvc.perform(get("/api/v1/weather/Madrid/today")).andExpect(status().isBadGateway());
	}

	@Test
	void rateLimitReturns503() throws Exception {
		when(weatherService.getToday("Madrid")).thenThrow(new ExternalApiRateLimitException("limit"));

		mockMvc.perform(get("/api/v1/weather/Madrid/today")).andExpect(status().isServiceUnavailable());
	}
}
