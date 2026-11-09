package com.josegs98.weather_api.controller;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.josegs98.weather_api.dto.WeatherResponse;
import com.josegs98.weather_api.service.WeatherService;

@RestController
@RequestMapping("/api/v1/weather")
@RequiredArgsConstructor
@Validated
@Tag(name = "Weather", description = "Consultas meteorológicas sobre Visual Crossing")
public class WeatherController {

	private final WeatherService weatherService;

	@GetMapping("/{city}/today")
	@Operation(summary = "Tiempo de hoy en una ciudad")
	@ApiResponse(responseCode = "200", description = "OK")
	@ApiResponse(responseCode = "404", description = "Ciudad no encontrada")
	@ApiResponse(responseCode = "502", description = "Error del servicio externo")
	@ApiResponse(responseCode = "503", description = "Límite de consultas excedido")
	public WeatherResponse getToday(
			@Parameter(description = "Nombre de la ciudad", example = "Madrid") @PathVariable @NotBlank String city) {
		return weatherService.getToday(city);
	}

	@GetMapping("/{city}")
	@Operation(summary = "Tiempo en una ciudad entre dos fechas",
			description = "Rango inclusivo, desde hoy en adelante y de como máximo 7 días")
	@ApiResponse(responseCode = "200", description = "OK")
	@ApiResponse(responseCode = "400", description = "Fechas inválidas")
	@ApiResponse(responseCode = "404", description = "Ciudad no encontrada")
	@ApiResponse(responseCode = "502", description = "Error del servicio externo")
	@ApiResponse(responseCode = "503", description = "Límite de consultas excedido")
	public WeatherResponse getRange(
			@Parameter(description = "Nombre de la ciudad", example = "Madrid") @PathVariable @NotBlank String city,
			@Parameter(description = "Fecha de entrada (yyyy-MM-dd)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@Parameter(description = "Fecha de salida (yyyy-MM-dd)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
		return weatherService.getRange(city, from, to);
	}
}
