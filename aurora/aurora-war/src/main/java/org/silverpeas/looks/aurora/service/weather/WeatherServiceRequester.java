package org.silverpeas.looks.aurora.service.weather;

import org.silverpeas.core.util.ServiceProvider;

import javax.enterprise.inject.Default;
import javax.enterprise.util.AnnotationLiteral;

/**
 * Requester of a remote weather service to fetch weather forecast data over several days.
 * @author mmoquillon
 */
@FunctionalInterface
public interface WeatherServiceRequester {

  /**
   * Gets the requester relative to the specified weather service.
   * @param serviceName a name of the weather service
   * @return the requester for the specified weather service. If the service isn't supported, then
   * returns the default weather service used by Silverpeas.
   */
  static WeatherServiceRequester getForService(final String serviceName) {
    try {
      return ServiceProvider.getService(serviceName);
    } catch (Exception e) {
      return ServiceProvider.getService(WeatherServiceRequester.class,
          new AnnotationLiteral<Default>() {
          });
    }
  }

  /**
   * Request weather forecast data of the specified city. If the requesting fails, then a
   * {@link javax.ws.rs.WebApplicationException} is thrown.
   * @param cityId a unique identifier of a city. Such identifiers are specific to the weather
   * service.
   * @return a JSON or an XML representation of the weather forecast data.
   */
  WeatherForecastData request(final String cityId);
}
