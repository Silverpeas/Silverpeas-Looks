package org.silverpeas.looks.aurora.service.weather;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.enterprise.inject.Default;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;

/**
 * Requester of the OpenWeatherMap API service.
 * @author mmoquillon
 */
@Named("OpenWeatherMap")
@Default
@Singleton
public class OpenWeatherMapRequester implements WeatherServiceRequester {

  private static final String API_URL = "http://api.openweathermap.org/data/2.5/forecast";
  private static final String SERVICE_NAME = "OpenWeatherMap";

  @Override
  public WeatherForecastData request(final String cityId) {
    try {
      final String language = User.getCurrentRequester().getUserPreferences().getLanguage();
      final String weatherData = ClientBuilder.newClient()
          .target(API_URL)
          .queryParam("appid", WeatherSettings.get().getAPIKey())
          .queryParam("id", cityId)
          .queryParam("units", "metric")
          .queryParam("lang", language)
          .request()
          .get(String.class);
      return new WeatherForecastData(SERVICE_NAME, weatherData, MediaType.APPLICATION_JSON_TYPE);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
      return new WeatherForecastData(SERVICE_NAME,"", MediaType.APPLICATION_JSON_TYPE);
    }
  }
}
  