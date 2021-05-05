package org.silverpeas.looks.aurora;

import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.URLUtil;

/**
 * @author Nicolas Eysseric
 */
public class LookSettings {

  SettingBundle settings;

  public LookSettings(SettingBundle settings) {
    this.settings = settings;
  }

  public String getHelpURL() {
    return settings.getString("helpURL", "");
  }

  public boolean isDisplayMenuSubElements() {
    return settings.getBoolean("banner.subElements", true);
  }

  public boolean isDisplayMenuAppsFirst() {
    return settings.getBoolean("banner.menu.apps.first", false);
  }

  public boolean isDisplayMenuAppIcons() {
    return settings.getBoolean("banner.menu.apps.icons", false);
  }

  public String getLogo() {
    return settings.getString("logo", "icons/1px.gif");
  }

  public boolean isDisplayConnectedUsers() {
    return settings.getBoolean("displayConnectedUsers", true);
  }

  public boolean isDisplayEphemeris() {
    return settings.getBoolean("home.ephemeris", true);
  }

  public boolean isDisplayBookmarksAreaWhenEmpty() {
    return settings.getBoolean("home.bookmarks.empty.show", true);
  }

  public String getNoBookmarksFragmentURL() {
    return settings.getString("home.bookmarks.empty.fragment", "");
  }

  public boolean isDisplaySearchOnHome() {
    return settings.getBoolean("home.search", true);
  }

  public String getNewsImageSize() {
    return settings.getString("home.news.width", "1095") + "x" +
        settings.getString("home.news.height", "");
  }

  public boolean isDisplayNewsWithCarrousel() {
    return "carrousel".equalsIgnoreCase(settings.getString("home.news.displayer", "carrousel"));
  }

  public String getEventsAppURL() {
    return URLUtil.getSimpleURL(URLUtil.URL_COMPONENT, settings.getString("home.events.appId", ""));
  }

  public String getDefaultHomepageURL() {
    return settings.getString("defaultHomepage", "/dt");
  }

  public String getPersonalHomepageURL() {
    return settings.getString("persoHomepage", "/dt");
  }

  public boolean isDisplayAppIcons() {
    return settings.getBoolean("displayComponentIcons", true);
  }

  public boolean isDisplayPDCInNavigationFrame() {
    return settings.getBoolean("displayPDCInNav", false);
  }

  public boolean isDisplayContextualPDC() {
    return settings.getBoolean("displayContextualPDC", true);
  }

  public boolean isDisplayPDCDedicatedFrame() {
    return settings.getBoolean("displayPDCFrame", false);
  }

  public boolean isLabelsInsideSelectOnTaxonomy() {
    return settings.getBoolean("home.search.taxonomy.labelsInside", false);
  }

  public boolean isDisplayMegaMenu() {
    return settings.getBoolean("banner.menu.mega", false);
  }

  public String getDateFormat() {
    return settings.getString("home.ephemeris.date.format", "dd MMMMM yyyy");
  }

  public String getSecondaryNewsImageSize() {
    return settings.getString("home.news.secondary.width", "1095") + "x" +
        settings.getString("home.news.secondary.height", "");
  }

  public boolean isDisplaySecondaryNewsWithCarrousel() {
    return "carrousel".equalsIgnoreCase(settings.getString("home.news.secondary.displayer", "carrousel"));
  }

  public String getExtraJavascriptForHome() {
    return settings.getString("javascript.homepage", "");
  }

  public String getExtraJavascriptForBanner() {
    return settings.getString("javascript.banner", "");
  }

  public String getExtraJavascriptForSpaceHomepage() {
    return settings.getString("javascript.space.homepage", "");
  }

}