package com.josegs98.weather_api.exception;

public class ExternalApiRateLimitException extends RuntimeException {

	public ExternalApiRateLimitException(String message) {
		super(message);
	}
}
