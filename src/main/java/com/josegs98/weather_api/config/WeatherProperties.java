package com.josegs98.weather_api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("weather")
public record WeatherProperties(int maxRangeDays) {
}
