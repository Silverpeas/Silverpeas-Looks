package org.silverpeas.looks.aurora.service.weather;

import jakarta.enterprise.inject.Default;
import jakarta.inject.Named;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.kernel.logging.SilverLogger;

/**
 * Requester of the OpenWeatherMap API service.
 *
 * @author mmoquillon
 */
@Default
@Service
@Named("OpenWeatherMap")
public class OpenWeatherMapRequester implements WeatherServiceRequester {

  private static final String API_URL = "http://api.openweathermap.org/data/2.5/forecast";
  private static final String SERVICE_NAME = "OpenWeatherMap";

  @Override
  public WeatherForecastData request(final String cityId) {
    try {
      final String language = User.getCurrentRequester().getUserPreferences().getLanguage();
      try (var client = ClientBuilder.newClient()) {
        final String weatherData = client
            .target(API_URL)
            .queryParam("appid", WeatherSettings.get().getAPIKey())
            .queryParam("id", cityId)
            .queryParam("units", "metric")
            .queryParam("lang", language)
            .request()
            .get(String.class);
        return new WeatherForecastData(SERVICE_NAME, weatherData, MediaType.APPLICATION_JSON_TYPE);
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
      return new WeatherForecastData(SERVICE_NAME, "", MediaType.APPLICATION_JSON_TYPE);
    }
  }
}
  