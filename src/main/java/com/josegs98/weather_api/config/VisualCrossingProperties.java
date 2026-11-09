package com.josegs98.weather_api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("visualcrossing")
public record VisualCrossingProperties(String baseUrl, String apiKey, String unitGroup, String lang) {
}
