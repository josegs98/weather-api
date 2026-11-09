package com.josegs98.weather_api.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({VisualCrossingProperties.class, WeatherProperties.class})
public class RestClientConfig {
}
