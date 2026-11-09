package com.josegs98.weather_api.exception;

public class CityNotFoundException extends RuntimeException {

	public CityNotFoundException(String city) {
		super("No se encontró la ciudad: " + city);
	}
}
