package com.josegs98.weather_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(InvalidDateRangeException.class)
	public ProblemDetail handleInvalidRange(InvalidDateRangeException ex) {
		return problem(HttpStatus.BAD_REQUEST, "Rango de fechas inválido", ex.getMessage());
	}

	@ExceptionHandler(CityNotFoundException.class)
	public ProblemDetail handleCityNotFound(CityNotFoundException ex) {
		return problem(HttpStatus.NOT_FOUND, "Ciudad no encontrada", ex.getMessage());
	}

	@ExceptionHandler(ExternalApiRateLimitException.class)
	public ProblemDetail handleRateLimit(ExternalApiRateLimitException ex) {
		return problem(HttpStatus.SERVICE_UNAVAILABLE, "Servicio no disponible", ex.getMessage());
	}

	@ExceptionHandler(ExternalApiException.class)
	public ProblemDetail handleExternal(ExternalApiException ex) {
		return problem(HttpStatus.BAD_GATEWAY, "Error del servicio externo", ex.getMessage());
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
		return problem(HttpStatus.BAD_REQUEST, "Parámetro inválido",
				"El parámetro '" + ex.getName() + "' tiene un formato inválido (se espera yyyy-MM-dd)");
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ProblemDetail handleMissingParam(MissingServletRequestParameterException ex) {
		return problem(HttpStatus.BAD_REQUEST, "Parámetro obligatorio ausente",
				"Falta el parámetro '" + ex.getParameterName() + "'");
	}

	@ExceptionHandler(HandlerMethodValidationException.class)
	public ProblemDetail handleValidation(HandlerMethodValidationException ex) {
		return problem(HttpStatus.BAD_REQUEST, "Petición inválida", "Los parámetros de la petición no son válidos");
	}

	private ProblemDetail problem(HttpStatus status, String title, String detail) {
		ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
		pd.setTitle(title);
		return pd;
	}
}
