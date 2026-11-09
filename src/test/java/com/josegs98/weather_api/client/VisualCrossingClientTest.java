package com.josegs98.weather_api.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;

import com.josegs98.weather_api.client.dto.VisualCrossingResponse;
import com.josegs98.weather_api.config.RestClientConfig;
import com.josegs98.weather_api.exception.CityNotFoundException;
import com.josegs98.weather_api.exception.ExternalApiException;
import com.josegs98.weather_api.exception.ExternalApiRateLimitException;

@RestClientTest(VisualCrossingClient.class)
@Import(RestClientConfig.class)
@TestPropertySource(properties = {
		"visualcrossing.base-url=https://vc.test/timeline",
		"visualcrossing.api-key=TEST_KEY",
		"visualcrossing.unit-group=metric",
		"visualcrossing.lang=es",
		"weather.max-range-days=7" })
class VisualCrossingClientTest {

	private static final String JSON = """
			{
			  "resolvedAddress": "Madrid, Comunidad de Madrid, España",
			  "timezone": "Europe/Madrid",
			  "days": [
			    { "datetime": "2026-11-09", "tempmax": 17.2, "tempmin": 8.1, "precipprob": 35.0,
			      "conditions": "Parcialmente nublado", "humidity": 70 }
			  ]
			}
			""";

	@Autowired
	private VisualCrossingClient client;

	@Autowired
	private MockRestServiceServer server;

	@Test
	void getTodayBuildsUriAndDeserializes() {
		server.expect(requestTo(startsWith("https://vc.test/timeline/Madrid/today?")))
				.andExpect(method(HttpMethod.GET))
				.andExpect(queryParam("key", "TEST_KEY"))
				.andExpect(queryParam("unitGroup", "metric"))
				.andExpect(queryParam("lang", "es"))
				.andExpect(queryParam("include", "days"))
				.andExpect(queryParam("elements", "datetime,tempmax,tempmin,precipprob,conditions"))
				.andRespond(withSuccess(JSON, MediaType.APPLICATION_JSON));

		VisualCrossingResponse response = client.getToday("Madrid");

		assertThat(response.timezone()).isEqualTo("Europe/Madrid");
		assertThat(response.days()).hasSize(1);
		var day = response.days().getFirst();
		assertThat(day.datetime()).isEqualTo(LocalDate.of(2026, 11, 9));
		assertThat(day.tempmax()).isEqualTo(17.2);
		assertThat(day.tempmin()).isEqualTo(8.1);
		assertThat(day.precipprob()).isEqualTo(35.0);
		assertThat(day.conditions()).isEqualTo("Parcialmente nublado");
		server.verify();
	}

	@Test
	void getRangeBuildsDatePath() {
		server.expect(requestTo(startsWith("https://vc.test/timeline/Madrid/2026-11-09/2026-11-12?")))
				.andRespond(withSuccess(JSON, MediaType.APPLICATION_JSON));

		client.getRange("Madrid", LocalDate.of(2026, 11, 9), LocalDate.of(2026, 11, 12));
		server.verify();
	}

	@Test
	void cityWithSpacesIsEncoded() {
		server.expect(requestTo(startsWith("https://vc.test/timeline/San%20Sebasti%C3%A1n/today?")))
				.andRespond(withSuccess(JSON, MediaType.APPLICATION_JSON));

		client.getToday("San Sebastián");
		server.verify();
	}

	@Test
	void invalidLocationMapsToCityNotFound() {
		server.expect(requestTo(startsWith("https://vc.test/timeline/xxqqzz/today")))
				.andRespond(withStatus(HttpStatus.BAD_REQUEST)
						.body("Bad API Request:Invalid location parameter value."));

		assertThatThrownBy(() -> client.getToday("xxqqzz")).isInstanceOf(CityNotFoundException.class);
	}

	@Test
	void unauthorizedMapsToExternalApiException() {
		server.expect(requestTo(startsWith("https://vc.test/timeline/Madrid/today")))
				.andRespond(withStatus(HttpStatus.UNAUTHORIZED).body("No account found with API key"));

		assertThatThrownBy(() -> client.getToday("Madrid")).isInstanceOf(ExternalApiException.class);
	}

	@Test
	void tooManyRequestsMapsToRateLimit() {
		server.expect(requestTo(startsWith("https://vc.test/timeline/Madrid/today")))
				.andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

		assertThatThrownBy(() -> client.getToday("Madrid")).isInstanceOf(ExternalApiRateLimitException.class);
	}

	@Test
	void serverErrorMapsToExternalApiException() {
		server.expect(requestTo(startsWith("https://vc.test/timeline/Madrid/today")))
				.andRespond(withServerError());

		assertThatThrownBy(() -> client.getToday("Madrid")).isInstanceOf(ExternalApiException.class);
	}
}
