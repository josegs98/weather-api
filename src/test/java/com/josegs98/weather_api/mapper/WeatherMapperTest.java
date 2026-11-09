package com.josegs98.weather_api.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.josegs98.weather_api.client.dto.VisualCrossingDay;
import com.josegs98.weather_api.client.dto.VisualCrossingResponse;
import com.josegs98.weather_api.dto.DailyWeather;
import com.josegs98.weather_api.dto.WeatherResponse;

class WeatherMapperTest {

	private final WeatherMapper mapper = new WeatherMapper();

	@Test
	void mapsAllFields() {
		var source = new VisualCrossingResponse("Madrid, España", "Europe/Madrid",
				List.of(new VisualCrossingDay(LocalDate.of(2026, 11, 9), 17.2, 8.1, 35.0, "Parcialmente nublado")));

		WeatherResponse result = mapper.toResponse("Madrid", source);

		assertThat(result.city()).isEqualTo("Madrid");
		assertThat(result.resolvedAddress()).isEqualTo("Madrid, España");
		assertThat(result.timezone()).isEqualTo("Europe/Madrid");
		assertThat(result.days()).containsExactly(
				new DailyWeather(LocalDate.of(2026, 11, 9), 17.2, 8.1, 35.0, "Parcialmente nublado"));
	}

	@Test
	void nullDaysBecomesEmptyList() {
		WeatherResponse result = mapper.toResponse("Madrid", new VisualCrossingResponse("Madrid", "Europe/Madrid", null));
		assertThat(result.days()).isEmpty();
	}
}
