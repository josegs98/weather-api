package com.josegs98.weather_api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("weather-cache")
public record WeatherCacheProperties(Duration forecastTodayTtl, Duration forecastRangeTtl) {
    public WeatherCacheProperties {
        validateTtl("weather-cache.forecast-today-ttl", forecastTodayTtl);
        validateTtl("weather-cache.forecast-range-ttl", forecastRangeTtl);
    }

    private static void validateTtl(String property, Duration ttl) {
        if (ttl == null) {
            throw new IllegalArgumentException(property + " no está definida");
        }

        if (!ttl.isPositive()) {
            throw new IllegalArgumentException(property
                    + " debe ser mayor que cero (valor: " + ttl + ")");
        }
    }
}
