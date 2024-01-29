package org.silverpeas.looks.aurora.service.weather;

import org.silverpeas.core.annotation.Bean;
import org.silverpeas.kernel.annotation.Technical;
import org.silverpeas.kernel.cache.model.Cache;
import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.kernel.bundle.ResourceLocator;

import javax.inject.Singleton;
import java.util.function.Supplier;

/**
 * Cache of weather forecast data coming from a weather service.
 * @author mmoquillon
 */
@Technical
@Bean
@Singleton
public class WeatherCache {

  private static final String CACHE_PREFIX = "WEATHER-";
  private static final String CACHE_SETTINGS = "org.silverpeas.weather.settings.weather";

  /**
   * Gets the weather forecast data from the cache or invokes the specified supplier if no such
   * data are in the cache or if the data is expired. If the supplier is invoked, the returned data
   * is then automatically cached.
   * @param cityId the unique identifier of a city.
   * @param supplier a supplier of {@link WeatherForecastData} instance.
   * @return a {@link WeatherForecastData} instance.
   */
  public final WeatherForecastData get(final String cityId,
      final Supplier<WeatherForecastData> supplier) {
    final int timeToLive = getCacheTimeToLive();
    final WeatherForecastData data;
    if (timeToLive <= 0) {
      data = supplier.get();
    } else {
      final Cache cache = CacheAccessorProvider.getApplicationCacheAccessor().getCache();
      data =
          cache.computeIfAbsent(cacheKey(cityId), WeatherForecastData.class, timeToLive, supplier);
    }
    return data;
  }

  private String cacheKey(final String cityId) {
    return CACHE_PREFIX + cityId;
  }

  private int getCacheTimeToLive() {
    return
        ResourceLocator.getSettingBundle(CACHE_SETTINGS).getInteger("weather.cache.timeToLive", 3) *
            3600;
  }
}
  