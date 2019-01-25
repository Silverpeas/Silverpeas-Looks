package org.silverpeas.looks.aurora.service.weather;

import javax.ws.rs.core.MediaType;

/**
 * Weather forecast on several days as returned by a remote weather service.
 * @author mmoquillon
 */
public class WeatherForecastData {

  private final String service;
  private final String data;
  private final MediaType mediaType;

  /**
   * Constructs a new {@link WeatherForecastData} instance wrapping the specified data returned by
   * a weather service and expressed in the specified media type (either in JSON or in XML).
   * @param service the name of the weather service used.
   * @param data the data returned by a weather service.
   * @param mediaType the format of the data (usually in JSON or in XML).
   */
  WeatherForecastData(final String service, final String data, final MediaType mediaType) {
    this.service = service;
    this.data = data;
    this.mediaType = mediaType;
  }

  /**
   * Gets the name of the weather service from which this data is.
   * @return the name of the weather service that returned this data.
   */
  public String getWeatherService() {
    return service;
  }

  /**
   * Gets the data of the weather forecast itself.
   * @return a textual representation of the data.
   */
  public String getData() {
    return data;
  }

  /**
   * Gets the media type in which is formatted the weather forecast data.
   * @return the media type of the data.
   */
  public MediaType getMediaType() {
    return mediaType;
  }
}
  