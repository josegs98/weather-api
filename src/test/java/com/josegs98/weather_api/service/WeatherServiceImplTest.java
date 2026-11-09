package com.josegs98.weather_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import com.josegs98.weather_api.service.impl.WeatherServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.josegs98.weather_api.client.VisualCrossingClient;
import com.josegs98.weather_api.client.dto.VisualCrossingDay;
import com.josegs98.weather_api.client.dto.VisualCrossingResponse;
import com.josegs98.weather_api.config.WeatherProperties;
import com.josegs98.weather_api.dto.WeatherResponse;
import com.josegs98.weather_api.exception.InvalidDateRangeException;
import com.josegs98.weather_api.mapper.WeatherMapper;

@ExtendWith(MockitoExtension.class)
class WeatherServiceImplTest {

	private static final LocalDate TODAY = LocalDate.of(2026, 11, 9);

	@Mock
	private VisualCrossingClient client;

	private WeatherServiceImpl service;

	@BeforeEach
	void setUp() {
		Clock clock = Clock.fixed(TODAY.atStartOfDay(ZoneId.of("UTC")).toInstant(), ZoneId.of("UTC"));
		service = new WeatherServiceImpl(client, new WeatherMapper(), new WeatherProperties(7), clock);
	}

	@Test
	void getTodayDelegatesAndMaps() {
		when(client.getToday("Madrid")).thenReturn(response(TODAY));

		WeatherResponse result = service.getToday("Madrid");

		assertThat(result.city()).isEqualTo("Madrid");
		assertThat(result.days()).hasSize(1);
		assertThat(result.days().getFirst().date()).isEqualTo(TODAY);
	}

	@Test
	void getRangeWithValidSevenDayRange() {
		LocalDate to = TODAY.plusDays(6);
		when(client.getRange("Madrid", TODAY, to)).thenReturn(response(TODAY));

		assertThat(service.getRange("Madrid", TODAY, to).city()).isEqualTo("Madrid");
	}

	@Test
	void rejectsFromAfterTo() {
		assertThatThrownBy(() -> service.getRange("Madrid", TODAY.plusDays(2), TODAY.plusDays(1)))
				.isInstanceOf(InvalidDateRangeException.class).hasMessageContaining("posterior");
		verifyNoInteractions(client);
	}

	@Test
	void rejectsFromInThePast() {
		assertThatThrownBy(() -> service.getRange("Madrid", TODAY.minusDays(1), TODAY))
				.isInstanceOf(InvalidDateRangeException.class).hasMessageContaining("anterior a hoy");
		verifyNoInteractions(client);
	}

	@Test
	void rejectsRangeLongerThanMax() {
		assertThatThrownBy(() -> service.getRange("Madrid", TODAY, TODAY.plusDays(7)))
				.isInstanceOf(InvalidDateRangeException.class).hasMessageContaining("7 días");
		verifyNoInteractions(client);
	}

	private VisualCrossingResponse response(LocalDate date) {
		return new VisualCrossingResponse("Madrid, España", "Europe/Madrid",
				List.of(new VisualCrossingDay(date, 17.2, 8.1, 35.0, "Despejado")));
	}
}
