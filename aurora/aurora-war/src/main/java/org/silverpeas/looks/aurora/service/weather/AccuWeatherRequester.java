package org.silverpeas.looks.aurora.service.weather;

import org.silverpeas.core.admin.user.model.User;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;

/**
 * Requester of the AccuWeather API service.
 * @author mmoquillon
 */
@Singleton
@Named("AccuWeather")
public class AccuWeatherRequester implements WeatherServiceRequester {

  private static final String API_URL =
      "http://dataservice.accuweather.com/forecasts/v1/daily/5day";
  private static final String SERVICE_NAME = "AccuWeather";

  @Override
  public WeatherForecastData request(final String cityId) {
    final String language = User.getCurrentRequester().getUserPreferences().getLanguage();
    final String weatherData = ClientBuilder.newClient()
        .target(API_URL)
        .path(cityId)
        .queryParam("apikey", WeatherSettings.get().getAPIKey())
        .queryParam("language", language)
        .queryParam("metric", true)
        .request()
        .get(String.class);
    return new WeatherForecastData(SERVICE_NAME, weatherData, MediaType.APPLICATION_JSON_TYPE);
  }
}
  