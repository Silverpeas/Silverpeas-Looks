package org.silverpeas.looks.aurora.service.weather;

import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.kernel.bundle.SettingBundle;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.kernel.logging.SilverLogger;

import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Settings of a weather module dedicated to work with a remote weather service. By default
 * those settings are defined in the properties/org/silverpeas/weather/settings/weather.properties
 * file but they can be defined in another properties file. In that case, the path of this file
 * must be specified with {@link #setSettingsFile(String)} method. Only the cache time-to-live
 * defined in the default properties file is taken into account.
 * @author mmoquillon
 */
@SessionScoped
public class WeatherSettings implements Serializable {

  private static final String SETTINGS_BUNDLES_PATH = "org.silverpeas.weather.settings.";
  public static final String DEFAULT_SETTINGS = "weather";
  private String settingsPath = SETTINGS_BUNDLES_PATH + DEFAULT_SETTINGS;

  /**
   * Gets an instance of the {@link WeatherSettings} bundle.
   * @return a {@link WeatherSettings} instance.
   */
  public static WeatherSettings get() {
    return ServiceProvider.getService(WeatherSettings.class);
  }

  /**
   * Sets the name of the properties file in which are defined the weather settings to be
   * taken in charge by this {@link WeatherSettings} instance. The properties file has to be
   * located into the <code>properties/org/silverpeas/weather/settings/</code> directory of the
   * Silverpeas installation.
   * @param settingsFile the name of the properties file without its extension. It must be located
   * into the <code>properties/org/silverpeas/weather/settings/</code> directory to be found and
   * loaded by this object.
   */
  public void setSettingsFile(final String settingsFile) {
    int idx = settingsFile.indexOf(".properties");
    this.settingsPath = SETTINGS_BUNDLES_PATH + (idx > 0 ? settingsFile.substring(0, idx) : settingsFile);
  }

  /**
   * Gets the list of cities for which the weather forecast have to be requested.
   * @return a list of cities. Can be empty.
   */
  public List<City> getCities() {
    final SettingBundle settings = getSettingBundle();
    final List<City> cities = new ArrayList<>();

    String[] cityIds = StringUtil.split(settings.getString("weather.cities.ids", ""), ",");
    String[] cityLabels = StringUtil.split(settings.getString("weather.cities.names", ""), ",");

    for (int i = 0; i < cityIds.length; i++) {
      try {
        cities.add(new City(cityIds[i], cityLabels[i]));
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e);
      }
    }
    return cities;
  }

  /**
   * Gets the API key to use to request the remote weather service used by Silverpeas. The API key
   * is used by the remote weather service to identify the requester and for weather requesting
   * authorization.
   * @return an API key.
   */
  public String getAPIKey() {
    return getSettingBundle().getString("weather.apiKey", "");
  }


  /**
   * Gets the weather service to request for weather forecast. From this service name, the
   * matching requester will be used.
   * @return the name of the service.
   */
  public String getService() {
    return getSettingBundle().getString("weather.service", "OpenWeatherMap");
  }

  /**
   * Gets the client key to use to request the remote weather service used by Silverpeas. Only some
   * weather service requires a client key for authentication.
   * @return a client key.
   */
  public String getClientKey() {
    return getSettingBundle().getString("weather.clientKey", "");
  }

  /**
   * Gets the client secret to use to request the remote weather service used by Silverpeas. Only
   * some weather service requires a client secret for authentication. A client
   * secret is always associated with a client key.
   * @return a client secret.
   */
  public String getClientSecret() {
    return getSettingBundle().getString("weather.clientSecret", "");
  }

  private SettingBundle getSettingBundle() {
    return ResourceLocator.getSettingBundle(settingsPath);
  }
}
  