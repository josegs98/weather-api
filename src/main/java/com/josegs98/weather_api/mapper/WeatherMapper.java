package com.josegs98.weather_api.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.josegs98.weather_api.client.dto.VisualCrossingDay;
import com.josegs98.weather_api.client.dto.VisualCrossingResponse;
import com.josegs98.weather_api.dto.DailyWeather;
import com.josegs98.weather_api.dto.WeatherResponse;

@Component
public class WeatherMapper {

	public WeatherResponse toResponse(String city, VisualCrossingResponse vcResponse) {
		List<DailyWeather> days = new ArrayList<>();
		if(Objects.nonNull(vcResponse.days())){
			days = vcResponse.days().stream().map(this::toDaily).toList();
		}
		return new WeatherResponse(city, vcResponse.resolvedAddress(), vcResponse.timezone(), days);
	}

	private DailyWeather toDaily(VisualCrossingDay day) {
		return new DailyWeather(day.datetime(), day.tempmax(), day.tempmin(), day.precipprob(), day.conditions());
	}
}
