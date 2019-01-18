package org.silverpeas.looks.aurora.servlets;

import org.silverpeas.looks.aurora.service.weather.WeatherCache;
import org.silverpeas.looks.aurora.service.weather.WeatherForecastData;
import org.silverpeas.looks.aurora.service.weather.WeatherServiceRequester;
import org.silverpeas.looks.aurora.service.weather.WeatherSettings;

import javax.inject.Inject;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import java.io.IOException;
import java.io.Writer;

/**
 * Adapter of the real weather service API used by Silverpeas to fetch weather forecast for one or
 * more cities in the world.
 */
public class WeatherServiceAdapter extends HttpServlet {

  private static final long serialVersionUID = 1L;
  @Inject
  private WeatherCache cache;

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res) {
    try {
      final String cityId = req.getParameter("city");
      final WeatherForecastData data = getWeatherForecastData(cityId);
      res.setHeader("X-WeatherService", data.getWeatherService());
      res.setContentType(data.getMediaType().toString());
      res.setHeader("charset", "UTF-8");
      Writer writer = res.getWriter();
      writer.write(data.getData());
    } catch (WebApplicationException e) {
      res.setStatus(e.getResponse().getStatus());
    } catch (IOException e) {
      res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  private WeatherForecastData getWeatherForecastData(final String cityId) {
    return cache.get(cityId, () -> {
      final WeatherSettings weatherSettings = WeatherSettings.get();
      final String weatherService = weatherSettings.getService();
      final WeatherServiceRequester requester =
          WeatherServiceRequester.getForService(weatherService);
      return requester.request(cityId);
    });
  }

}
