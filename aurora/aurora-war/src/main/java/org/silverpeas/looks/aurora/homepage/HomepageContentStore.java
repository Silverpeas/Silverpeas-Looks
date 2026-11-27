/*
 * Copyright (C) 2000 - 2026 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.looks.aurora.homepage;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.HexFormat;
import java.util.Properties;
import java.util.stream.Collectors;

/** File storage outside the deployed application. One instance serializes writes in one JVM. */
public class HomepageContentStore {
  private static HomepageContentStore installationStore;
  private final Path root;

  public static synchronized HomepageContentStore forInstallation() {
    if (installationStore == null) {
      String dataHome = System.getProperty("SILVERPEAS_DATA_HOME");
      if (dataHome == null || dataHome.isBlank()) {
        throw new IllegalStateException("SILVERPEAS_DATA_HOME must be configured");
      }
      installationStore = new HomepageContentStore(Path.of(dataHome, "homepages"));
    }
    return installationStore;
  }

  public HomepageContentStore(Path root) {
    this.root = root.toAbsolutePath().normalize();
  }

  public record Layout(String zone, int order, boolean enabled) {
    public Layout {
      if ((!"main".equals(zone) && !"right".equals(zone)) || order < 0 || order > 10000) {
        throw new IllegalArgumentException("Invalid widget placement");
      }
    }
  }

  public record Content(String title, String html, String revision, Layout layout) { }

  public record Widget(String id, Content content, String type,
      NewsConfiguration newsConfiguration, int legacyIndex,
      PublicationsConfiguration publicationsConfiguration,
      EventsConfiguration eventsConfiguration, RssConfiguration rssConfiguration,
      FaqConfiguration faqConfiguration, UsersConfiguration usersConfiguration,
      SubscriptionsConfiguration subscriptionsConfiguration, ShortcutsConfiguration shortcutsConfiguration, SearchConfiguration searchConfiguration, BookmarksConfiguration bookmarksConfiguration) {
    public Widget(String id, Content content, String type, NewsConfiguration news, int legacyIndex,
        PublicationsConfiguration publications, EventsConfiguration events, RssConfiguration rss,
        FaqConfiguration faq, UsersConfiguration users, SubscriptionsConfiguration subscriptions,
        ShortcutsConfiguration shortcuts, SearchConfiguration search) {
      this(id, content, type, news, legacyIndex, publications, events, rss, faq, users, subscriptions, shortcuts, search, null);
    }
    public Widget(String id, Content content, String type, NewsConfiguration news, int legacyIndex,
        PublicationsConfiguration publications, EventsConfiguration events, RssConfiguration rss,
        FaqConfiguration faq, UsersConfiguration users, SubscriptionsConfiguration subscriptions,
        ShortcutsConfiguration shortcuts) {
      this(id, content, type, news, legacyIndex, publications, events, rss, faq, users, subscriptions, shortcuts, null);
    }
    public Widget(String id, Content content, String type, NewsConfiguration news, int legacyIndex,
        PublicationsConfiguration publications, EventsConfiguration events, RssConfiguration rss,
        FaqConfiguration faq, UsersConfiguration users, SubscriptionsConfiguration subscriptions) {
      this(id, content, type, news, legacyIndex, publications, events, rss, faq, users, subscriptions, null);
    }
    public Widget(String id, Content content, String type, NewsConfiguration news, int legacyIndex,
        PublicationsConfiguration publications, EventsConfiguration events, RssConfiguration rss,
        FaqConfiguration faq, UsersConfiguration users) {
      this(id, content, type, news, legacyIndex, publications, events, rss, faq, users, null);
    }
    public Widget(String id, Content content, String type, NewsConfiguration news, int legacyIndex,
        PublicationsConfiguration publications, EventsConfiguration events, RssConfiguration rss) {
      this(id, content, type, news, legacyIndex, publications, events, rss, null, null);
    }
    public Widget(String id, Content content, String type, NewsConfiguration news, int legacyIndex,
        PublicationsConfiguration publications) {
      this(id, content, type, news, legacyIndex, publications, null, null);
    }
    public Widget(String id, Content content, String type, NewsConfiguration news, int legacyIndex) {
      this(id, content, type, news, legacyIndex, null);
    }
    public Widget(String id, Content content) {
      this(id, content, "html", null, 0);
    }
    public NewsConfiguration getNewsConfiguration() { return newsConfiguration; }
    public PublicationsConfiguration getPublicationsConfiguration() { return publicationsConfiguration; }
    public EventsConfiguration getEventsConfiguration() { return eventsConfiguration; }
    public RssConfiguration getRssConfiguration() { return rssConfiguration; }
    public FaqConfiguration getFaqConfiguration() { return faqConfiguration; }
    public UsersConfiguration getUsersConfiguration() { return usersConfiguration; }
    public SubscriptionsConfiguration getSubscriptionsConfiguration() { return subscriptionsConfiguration; }
    public ShortcutsConfiguration getShortcutsConfiguration() { return shortcutsConfiguration; }
    public BookmarksConfiguration getBookmarksConfiguration() { return bookmarksConfiguration; }
    public SearchConfiguration getSearchConfiguration() { return searchConfiguration; }
    public String getType() { return type; }
    public String getId() { return id; }
    public String getTitle() { return content.title(); }
    public String getRevision() { return content.revision(); }
    public String getHtml() { return content.html(); }
    public String getZone() { return content.layout().zone(); }
    public int getOrder() { return content.layout().order(); }
    public boolean isEnabled() { return content.layout().enabled(); }
  }

  public synchronized java.util.List<Widget> widgets(HomepagePage page, String language)
      throws IOException {
    var ids = Arrays.stream(properties(page).getProperty("widgets", "").split(","))
        .filter(id -> !id.isBlank()).distinct().toList();
    var result = new java.util.ArrayList<Widget>();
    for (String id : ids) {
      result.add(readWidget(page, id, language));
    }
    result.sort(java.util.Comparator.comparingInt(Widget::getOrder).thenComparing(Widget::getId));
    return java.util.List.copyOf(result);
  }

  public synchronized Widget readWidget(HomepagePage page, String widget, String language)
      throws IOException {
    var settings = properties(page);
    String prefix = "widget." + widget;
    String type = settings.getProperty(prefix + ".type", "html");
    var content = read(page, widget, language);
    NewsConfiguration news = "news".equals(type)
        ? new NewsConfiguration(settings.getProperty(prefix + ".source", "*"),
            Integer.parseInt(settings.getProperty(prefix + ".size", "10")),
            Boolean.parseBoolean(settings.getProperty(prefix + ".importantOnly", "false")),
            settings.getProperty(prefix + ".rendering", "CAROUSEL"), content.layout().enabled(),
            content.revision(), Boolean.parseBoolean(settings.getProperty(prefix + ".showMoreNews", "true")),
            Boolean.parseBoolean(settings.getProperty(prefix + ".showAllNews", "true"))) : null;
    return new Widget(widget, content, type, news,
        Integer.parseInt(settings.getProperty(prefix + ".legacyIndex", "0")),
        "publications".equals(type) ? new PublicationsConfiguration(
            settings.getProperty(prefix + ".spaceId", ""),
            Integer.parseInt(settings.getProperty(prefix + ".size", "3")),
            Boolean.parseBoolean(settings.getProperty(prefix + ".showMore", "true"))) : null,
        "events".equals(type) ? new EventsConfiguration(settings.getProperty(prefix + ".source", ""),
            Integer.parseInt(settings.getProperty(prefix + ".maxDays", "2")),
            Boolean.parseBoolean(settings.getProperty(prefix + ".includeToday", "true")),
            Boolean.parseBoolean(settings.getProperty(prefix + ".personal", "false")),
            Boolean.parseBoolean(settings.getProperty(prefix + ".importantOnly", "true")),
            Boolean.parseBoolean(settings.getProperty(prefix + ".showMore", "true"))) : null,
        "rss".equals(type) ? new RssConfiguration(settings.getProperty(prefix + ".source", ""),
            settings.getProperty(prefix + ".rendering", "aggregate")) : null,
        "faq".equals(type) ? new FaqConfiguration(settings.getProperty(prefix + ".source", ""),
            Integer.parseInt(settings.getProperty(prefix + ".size", "1")),
            settings.getProperty(prefix + ".selection", "random")) : null,
        "users".equals(type) ? new UsersConfiguration(settings.getProperty(prefix + ".group", ""),
            Integer.parseInt(settings.getProperty(prefix + ".size", "5")),
            Boolean.parseBoolean(settings.getProperty(prefix + ".avatar", "true")),
            settings.getProperty(prefix + ".fields", "")) : null,
        "subscriptions".equals(type) ? new SubscriptionsConfiguration(
            Integer.parseInt(settings.getProperty(prefix + ".size", "4")),
            Boolean.parseBoolean(settings.getProperty(prefix + ".showMore", "true"))) : null,
        "shortcuts".equals(type) ? readShortcuts(settings, prefix) : null,
        "search".equals(type) ? readSearch(settings, prefix) : null,
        "bookmarks".equals(type) ? readBookmarks(settings, prefix) : null);
  }

  public synchronized Content read(HomepagePage page, String widget, String language)
      throws IOException {
    Path file = contentPath(page, widget, language);
    Properties properties = properties(page);
    String title = properties.getProperty("widget." + widget + ".title", "");
    String html = Files.exists(file) ? Files.readString(file, StandardCharsets.UTF_8) : "";
    String prefix = "widget." + widget;
    var layout = new Layout(properties.getProperty(prefix + ".zone", "main"),
        Integer.parseInt(properties.getProperty(prefix + ".order", "10")),
        Boolean.parseBoolean(properties.getProperty(prefix + ".enabled", "true")));
    String configurationVersion = java.util.List.of("source", "size", "importantOnly", "rendering", "legacyIndex")
        .stream().map(key -> properties.getProperty(prefix + "." + key, ""))
        .collect(Collectors.joining("\u0000"));
    // Preserve revisions of news widgets saved before button settings were introduced.
    if (properties.containsKey(prefix + ".showMoreNews")) {
      configurationVersion += "\u0000" + properties.getProperty(prefix + ".showMoreNews")
          + "\u0000" + properties.getProperty(prefix + ".showAllNews", "true");
    }
    if ("publications".equals(properties.getProperty(prefix + ".type"))) {
      configurationVersion = java.util.List.of("spaceId", "size", "showMore").stream()
          .map(key -> properties.getProperty(prefix + "." + key, ""))
          .collect(Collectors.joining("\u0000"));
    }
    if (java.util.List.of("events", "rss").contains(properties.getProperty(prefix + ".type", ""))) {
      configurationVersion = java.util.List.of("source", "maxDays", "includeToday", "personal",
          "importantOnly", "showMore", "rendering").stream()
          .map(key -> properties.getProperty(prefix + "." + key, ""))
          .collect(Collectors.joining("\u0000"));
    }
    if (java.util.List.of("faq", "users").contains(properties.getProperty(prefix + ".type", ""))) {
      configurationVersion = java.util.List.of("source", "size", "selection", "group", "avatar", "fields")
          .stream().map(key -> properties.getProperty(prefix + "." + key, ""))
          .collect(Collectors.joining("\u0000"));
    }
    if ("subscriptions".equals(properties.getProperty(prefix + ".type"))) {
      configurationVersion = properties.getProperty(prefix + ".size", "4") + "\u0000"
          + properties.getProperty(prefix + ".showMore", "true");
    }
    if ("shortcuts".equals(properties.getProperty(prefix + ".type"))) {
      configurationVersion = readShortcuts(properties, prefix).entries().stream()
          .map(entry -> String.join("\u0000", entry.label(), entry.url(), entry.iconUrl(), entry.target()))
          .collect(Collectors.joining("\u0000"));
    }
    if ("search".equals(properties.getProperty(prefix + ".type"))) {
      configurationVersion = readSearch(properties, prefix).toString();
    }
    if ("bookmarks".equals(properties.getProperty(prefix + ".type"))) {
      configurationVersion = readBookmarks(properties, prefix).toString();
    }
    String version = properties.containsKey(prefix + ".type")
        ? revision(!"html".equals(properties.getProperty(prefix + ".type"))
            ? title + layout + configurationVersion : title + layout, html) : "";
    return new Content(title, html, version, layout);
  }

  public synchronized void save(HomepagePage page, String widget, String language,
      String title, String html, String expectedRevision) throws IOException {
    saveConfigured(page, widget, language, title, html, expectedRevision,
        read(page, widget, language).layout());
  }

  public synchronized void saveConfigured(HomepagePage page, String widget, String language,
      String title, String html, String expectedRevision, Layout layout) throws IOException {
    if ("weather-legacy".equals(widget) || "bookmarks-legacy".equals(widget) || "search-legacy".equals(widget) || "shortcuts-legacy".equals(widget) || "subscriptions-legacy".equals(widget) || "faq-legacy".equals(widget) || "users-legacy".equals(widget) || "events-legacy".equals(widget) || "rss-legacy".equals(widget) || "ephemeris-legacy".equals(widget) || "publications-legacy".equals(widget) || HomepageWidgets.legacyIndex(widget) != 0 || !"html".equals(properties(page).getProperty("widget." + widget + ".type", "html"))) {
      throw new IllegalArgumentException("Widget type cannot be changed");
    }
    Content previous = read(page, widget, language);
    if (!previous.revision().equals(expectedRevision)) {
      throw new ConcurrentModificationException("Content changed since it was loaded");
    }
    Properties properties = properties(page);
    String prefix = "widget." + widget;
    registerWidget(properties, widget);
    properties.setProperty(prefix + ".type", "html");
    configureLayout(properties, prefix, title, layout);
    atomicWrite(contentPath(page, widget, language), html.getBytes(StandardCharsets.UTF_8));
    saveProperties(page, properties);
  }

  public record LayoutUpdate(String widget, String revision, int order, boolean enabled) { }

  public synchronized void updateLayouts(HomepagePage page, String language,
      java.util.List<LayoutUpdate> updates) throws IOException {
    var deleted = deletedWidgets(page);
    var current = widgets(page, language).stream()
        .filter(widget -> !deleted.contains(widget.getId())).toList();
    var ids = updates.stream().map(LayoutUpdate::widget).collect(Collectors.toSet());
    if (ids.size() != updates.size() || !ids.equals(current.stream()
        .map(Widget::getId).collect(Collectors.toSet()))) {
      throw new ConcurrentModificationException("Widget list changed");
    }
    var settings = properties(page);
    for (var update : updates) {
      var content = read(page, update.widget(), language);
      if (!content.revision().equals(update.revision())) {
        throw new ConcurrentModificationException("Widget changed");
      }
      var layout = new Layout(content.layout().zone(), update.order(), update.enabled());
      configureLayout(settings, "widget." + update.widget(), content.title(), layout);
    }
    saveProperties(page, settings);
  }

  public synchronized void deleteWidget(HomepagePage page, String widget, String language,
      String expectedRevision) throws IOException {
    HomepagePage.validateIdentifier(widget);
    if (!read(page, widget, language).revision().equals(expectedRevision)) {
      throw new ConcurrentModificationException("Widget changed");
    }
    var settings = properties(page);
    settings.setProperty("widget." + widget + ".deleted", "true");
    saveProperties(page, settings);
  }

  public synchronized java.util.Set<String> deletedWidgets(HomepagePage page) throws IOException {
    var settings = properties(page);
    return settings.stringPropertyNames().stream()
        .filter(key -> key.startsWith("widget.") && key.endsWith(".deleted"))
        .filter(key -> Boolean.parseBoolean(settings.getProperty(key)))
        .map(key -> key.substring(7, key.length() - 8)).collect(Collectors.toSet());
  }

  private void registerWidget(Properties properties, String widget) {
    if (Boolean.parseBoolean(properties.getProperty("widget." + widget + ".deleted"))) {
      throw new ConcurrentModificationException("Widget deleted");
    }
    properties.setProperty("page.configuration.version", "1");
    String widgets = properties.getProperty("widgets", "");
    var ids = Arrays.stream(widgets.split(",")).filter(s -> !s.isBlank())
        .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
    ids.add(widget);
    properties.setProperty("widgets", String.join(",", ids));
  }

  private void configureLayout(Properties properties, String prefix, String title, Layout layout) {
    if (title == null || title.length() > 200) {
      throw new IllegalArgumentException("Invalid title");
    }
    properties.setProperty(prefix + ".title", title);
    properties.setProperty(prefix + ".zone", layout.zone());
    properties.setProperty(prefix + ".enabled", String.valueOf(layout.enabled()));
    properties.setProperty(prefix + ".order", String.valueOf(layout.order()));
  }

  private void saveProperties(HomepagePage page, Properties properties) throws IOException {
    StringWriter writer = new StringWriter();
    properties.store(writer, "Aurora homepage");
    atomicWrite(propertiesPath(page),
        writer.toString().getBytes(StandardCharsets.UTF_8));
  }

  public synchronized void saveNewsWidget(HomepagePage page, String widget, String language,
      String title, Layout layout, NewsConfiguration news, int legacyIndex) throws IOException {
    if ("weather-legacy".equals(widget) || "bookmarks-legacy".equals(widget) || "search-legacy".equals(widget) || "shortcuts-legacy".equals(widget) || "subscriptions-legacy".equals(widget) || "faq-legacy".equals(widget) || "users-legacy".equals(widget) || "events-legacy".equals(widget) || "rss-legacy".equals(widget) || "ephemeris-legacy".equals(widget) || "publications-legacy".equals(widget) || legacyIndex < 0 || legacyIndex > 3) {
      throw new IllegalArgumentException("Invalid legacy news reference");
    }
    var properties = properties(page);
    String prefix = "widget." + widget;
    if (!"news".equals(properties.getProperty(prefix + ".type", "news"))) {
      throw new IllegalArgumentException("Widget type cannot be changed");
    }
    if (!read(page, widget, language).revision().equals(news.revision())) {
      throw new ConcurrentModificationException("Widget changed since it was loaded");
    }
    registerWidget(properties, widget);
    configureLayout(properties, prefix, title, layout);
    properties.setProperty(prefix + ".type", "news");
    properties.setProperty(prefix + ".source", news.source());
    properties.setProperty(prefix + ".size", String.valueOf(news.size()));
    properties.setProperty(prefix + ".importantOnly", String.valueOf(news.importantOnly()));
    properties.setProperty(prefix + ".rendering", news.rendering());
    properties.setProperty(prefix + ".legacyIndex", String.valueOf(legacyIndex));
    properties.setProperty(prefix + ".showMoreNews", String.valueOf(news.showMoreNews()));
    properties.setProperty(prefix + ".showAllNews", String.valueOf(news.showAllNews()));
    saveProperties(page, properties);
  }

  public record NewsConfiguration(String source, int size, boolean importantOnly,
      String rendering, boolean enabled, String revision, boolean showMoreNews, boolean showAllNews) {
    public NewsConfiguration(String source, int size, boolean importantOnly, String rendering,
        boolean enabled, String revision) {
      this(source, size, importantOnly, rendering, enabled, revision, true, true);
    }
    public boolean isShowMoreNews() { return showMoreNews; }
    public boolean isShowAllNews() { return showAllNews; }
    public NewsConfiguration {
      if (source == null || !source.matches("(?:|\\*|delegated|taxonomy|quickinfo[0-9]+(?: quickinfo[0-9]+)*)")
          || (size != -1 && (size < 1 || size > 100)) || !("LIST".equals(rendering) || "CAROUSEL".equals(rendering))) {
        throw new IllegalArgumentException("Invalid news configuration");
      }
    }
  }

  public synchronized void saveEphemerisWidget(HomepagePage page, String widget, String language,
      String title, Layout layout, String expectedRevision) throws IOException {
    var properties = properties(page);
    String prefix = "widget." + widget;
    if ("weather-legacy".equals(widget) || "bookmarks-legacy".equals(widget) || "search-legacy".equals(widget) || "shortcuts-legacy".equals(widget) || "subscriptions-legacy".equals(widget) || "faq-legacy".equals(widget) || "users-legacy".equals(widget) || "events-legacy".equals(widget) || "rss-legacy".equals(widget) || "publications-legacy".equals(widget) || HomepageWidgets.legacyIndex(widget) != 0 ||
        !"ephemeris".equals(properties.getProperty(prefix + ".type", "ephemeris"))) {
      throw new IllegalArgumentException("Widget type cannot be changed");
    }
    if (!read(page, widget, language).revision().equals(expectedRevision)) {
      throw new ConcurrentModificationException("Widget changed since it was loaded");
    }
    registerWidget(properties, widget);
    configureLayout(properties, prefix, title, layout);
    properties.setProperty(prefix + ".type", "ephemeris");
    saveProperties(page, properties);
  }

  /** An ordered home shortcut, separate from the toolbar shortcuts. */
  public record HomeShortcut(String label, String url, String iconUrl, String target) {
    public HomeShortcut {
      label = label == null ? "" : label.trim();
      url = url == null ? "" : url.trim();
      iconUrl = iconUrl == null ? "" : iconUrl.trim();
      target = target == null ? "" : target.trim();
      if (label.length() > 200 || label.indexOf('\u0000') >= 0 || url.isEmpty() ||
          !safeShortcutUrl(url, false) || !safeShortcutUrl(iconUrl, true) ||
          !target.matches("(?:|_self|_blank|_parent|_top|[A-Za-z][A-Za-z0-9_-]{0,63})")) {
        throw new IllegalArgumentException("Invalid home shortcut");
      }
    }
    public String getAltText() { return label; }
    public String getUrl() { return url; }
    public String getIconURL() { return iconUrl; }
    public String getTarget() { return target; }
  }

  private static boolean safeShortcutUrl(String url, boolean image) {
    if (url.length() > 2048 || url.chars().anyMatch(c -> Character.isISOControl(c) || c == '\\')) {
      return false;
    }
    // Relative links and anchors are valid; executable and data schemes are not.
    int colon = url.indexOf(':');
    int boundary = java.util.stream.IntStream.of(url.indexOf('/'), url.indexOf('?'), url.indexOf('#'))
        .filter(i -> i >= 0).min().orElse(Integer.MAX_VALUE);
    return colon < 0 || colon > boundary ||
        url.matches(image ? "(?i)https?://.+" : "(?i)(?:https?://.+|mailto:.+)");
  }

  public record ShortcutsConfiguration(java.util.List<HomeShortcut> entries) {
    public ShortcutsConfiguration {
      if (entries == null || entries.size() > 100 || entries.stream().anyMatch(java.util.Objects::isNull)) {
        throw new IllegalArgumentException("Invalid home shortcuts configuration");
      }
      entries = java.util.List.copyOf(entries);
    }
    public java.util.List<HomeShortcut> getEntries() { return entries; }
  }

  private ShortcutsConfiguration readShortcuts(Properties properties, String prefix) {
    int count = Integer.parseInt(properties.getProperty(prefix + ".count", "0"));
    if (count < 0 || count > 100) {
      throw new IllegalArgumentException("Invalid home shortcut count");
    }
    return new ShortcutsConfiguration(java.util.stream.IntStream.range(0, count).mapToObj(index -> {
      String key = prefix + ".entry." + index;
      return new HomeShortcut(properties.getProperty(key + ".label", ""),
          properties.getProperty(key + ".url", ""), properties.getProperty(key + ".iconUrl", ""),
          properties.getProperty(key + ".target", ""));
    }).toList());
  }

  public synchronized void saveShortcutsWidget(HomepagePage page, String widget, String language,
      String title, Layout layout, ShortcutsConfiguration configuration, String expectedRevision)
      throws IOException {
    HomepagePage.validateIdentifier(widget);
    var properties = properties(page);
    String prefix = "widget." + widget;
    if (configuration == null || java.util.List.of("weather-legacy", "bookmarks-legacy", "search-legacy", "subscriptions-legacy", "faq-legacy", "users-legacy",
        "events-legacy", "rss-legacy", "ephemeris-legacy", "publications-legacy").contains(widget) ||
        HomepageWidgets.legacyIndex(widget) != 0 ||
        !"shortcuts".equals(properties.getProperty(prefix + ".type", "shortcuts"))) {
      throw new IllegalArgumentException("Invalid home shortcuts widget");
    }
    if (!read(page, widget, language).revision().equals(expectedRevision)) {
      throw new ConcurrentModificationException("Widget changed since it was loaded");
    }
    registerWidget(properties, widget);
    configureLayout(properties, prefix, title, layout);
    properties.setProperty(prefix + ".type", "shortcuts");
    properties.keySet().removeIf(key -> key.toString().startsWith(prefix + ".entry."));
    properties.setProperty(prefix + ".count", String.valueOf(configuration.entries().size()));
    for (int index = 0; index < configuration.entries().size(); index++) {
      var entry = configuration.entries().get(index);
      String key = prefix + ".entry." + index;
      properties.setProperty(key + ".label", entry.label());
      properties.setProperty(key + ".url", entry.url());
      properties.setProperty(key + ".iconUrl", entry.iconUrl());
      properties.setProperty(key + ".target", entry.target());
    }
    saveProperties(page, properties);
  }

  public record SearchConfiguration(String mode, String template, boolean labelsInside,
      boolean period, boolean space) {
    public SearchConfiguration {
      if (!java.util.List.of("taxonomy", "advanced").contains(mode == null ? "" : mode) ||
          template == null || template.length() > 200 ||
          !template.matches("(?:|[A-Za-z0-9_-][A-Za-z0-9_.-]*\\.xml)") ||
          template.contains("..") || "advanced".equals(mode) && template.isEmpty()) {
        throw new IllegalArgumentException("Invalid search configuration");
      }
    }
    public String getMode() { return mode; }
    public String getTemplate() { return template; }
    public boolean isLabelsInside() { return labelsInside; }
    public boolean isPeriod() { return period; }
    public boolean isSpace() { return space; }
  }

  private SearchConfiguration readSearch(Properties properties, String prefix) {
    return new SearchConfiguration(properties.getProperty(prefix + ".mode", "taxonomy"),
        properties.getProperty(prefix + ".template", ""),
        Boolean.parseBoolean(properties.getProperty(prefix + ".labelsInside", "false")),
        Boolean.parseBoolean(properties.getProperty(prefix + ".period", "false")),
        Boolean.parseBoolean(properties.getProperty(prefix + ".space", "false")));
  }

  public synchronized void saveSearchWidget(HomepagePage page, String widget, String language,
      String title, Layout layout, SearchConfiguration configuration, String expectedRevision)
      throws IOException {
    HomepagePage.validateIdentifier(widget);
    var properties = properties(page);
    String prefix = "widget." + widget;
    if (configuration == null || java.util.List.of("weather-legacy", "bookmarks-legacy", "shortcuts-legacy", "subscriptions-legacy",
        "faq-legacy", "users-legacy", "events-legacy", "rss-legacy", "ephemeris-legacy",
        "publications-legacy").contains(widget) || HomepageWidgets.legacyIndex(widget) != 0 ||
        !"search".equals(properties.getProperty(prefix + ".type", "search"))) {
      throw new IllegalArgumentException("Invalid search widget");
    }
    if (!read(page, widget, language).revision().equals(expectedRevision)) {
      throw new ConcurrentModificationException("Widget changed since it was loaded");
    }
    registerWidget(properties, widget);
    configureLayout(properties, prefix, title, layout);
    properties.setProperty(prefix + ".type", "search");
    properties.setProperty(prefix + ".mode", configuration.mode());
    properties.setProperty(prefix + ".template", configuration.template());
    properties.setProperty(prefix + ".labelsInside", String.valueOf(configuration.labelsInside()));
    properties.setProperty(prefix + ".period", String.valueOf(configuration.period()));
    properties.setProperty(prefix + ".space", String.valueOf(configuration.space()));
    saveProperties(page, properties);
  }

  public synchronized void saveWeatherWidget(HomepagePage page, String widget, String language,
      String title, Layout layout, String expectedRevision)
      throws IOException {
    HomepagePage.validateIdentifier(widget);
    var properties = properties(page);
    String prefix = "widget." + widget;
    if ((widget.endsWith("-legacy") && !"weather-legacy".equals(widget)) ||
        HomepageWidgets.legacyIndex(widget) != 0 ||
        !"weather".equals(properties.getProperty(prefix + ".type", "weather"))) {
      throw new IllegalArgumentException("Invalid weather widget");
    }
    if (!read(page, widget, language).revision().equals(expectedRevision)) {
      throw new ConcurrentModificationException("Widget changed since it was loaded");
    }
    registerWidget(properties, widget);
    configureLayout(properties, prefix, title, layout);
    properties.setProperty(prefix + ".type", "weather");
    properties.remove(prefix + ".cities");
    saveProperties(page, properties);
  }

  public record BookmarksConfiguration(boolean showWhenEmpty, String emptyFragment) {
    public BookmarksConfiguration {
      if (emptyFragment == null || !safeShortcutUrl(emptyFragment, true)) {
        throw new IllegalArgumentException("Invalid bookmarks fragment URL");
      }
    }
    public boolean isShowWhenEmpty() { return showWhenEmpty; }
    public String getEmptyFragment() { return emptyFragment; }
  }

  private BookmarksConfiguration readBookmarks(Properties properties, String prefix) {
    return new BookmarksConfiguration(
        Boolean.parseBoolean(properties.getProperty(prefix + ".showWhenEmpty", "true")),
        properties.getProperty(prefix + ".emptyFragment", ""));
  }

  public synchronized void saveBookmarksWidget(HomepagePage page, String widget, String language,
      String title, Layout layout, BookmarksConfiguration configuration, String expectedRevision)
      throws IOException {
    HomepagePage.validateIdentifier(widget);
    var properties = properties(page);
    String prefix = "widget." + widget;
    if (configuration == null || (widget.endsWith("-legacy") && !"bookmarks-legacy".equals(widget)) ||
        HomepageWidgets.legacyIndex(widget) != 0 ||
        !"bookmarks".equals(properties.getProperty(prefix + ".type", "bookmarks"))) {
      throw new IllegalArgumentException("Invalid bookmarks widget");
    }
    if (!read(page, widget, language).revision().equals(expectedRevision)) {
      throw new ConcurrentModificationException("Widget changed since it was loaded");
    }
    registerWidget(properties, widget);
    configureLayout(properties, prefix, title, layout);
    properties.setProperty(prefix + ".type", "bookmarks");
    properties.setProperty(prefix + ".showWhenEmpty", String.valueOf(configuration.showWhenEmpty()));
    properties.setProperty(prefix + ".emptyFragment", configuration.emptyFragment());
    saveProperties(page, properties);
  }

  public record SubscriptionsConfiguration(int size, boolean showMore) {
    public SubscriptionsConfiguration {
      if (size < 1 || size > 100) {
        throw new IllegalArgumentException("Invalid subscriptions configuration");
      }
    }
    public boolean isShowMore() { return showMore; }
  }

  public synchronized void saveSubscriptionsWidget(HomepagePage page, String widget, String language,
      String title, Layout layout, SubscriptionsConfiguration configuration, String expectedRevision)
      throws IOException {
    HomepagePage.validateIdentifier(widget);
    if (configuration == null || "weather-legacy".equals(widget) || "bookmarks-legacy".equals(widget) || "search-legacy".equals(widget) || "shortcuts-legacy".equals(widget) || "faq-legacy".equals(widget) || "users-legacy".equals(widget) ||
        "events-legacy".equals(widget) || "rss-legacy".equals(widget) ||
        "ephemeris-legacy".equals(widget) || "publications-legacy".equals(widget) ||
        HomepageWidgets.legacyIndex(widget) != 0) {
      throw new IllegalArgumentException("Invalid subscriptions widget");
    }
    var properties = properties(page);
    String prefix = "widget." + widget;
    if (!"subscriptions".equals(properties.getProperty(prefix + ".type", "subscriptions"))) {
      throw new IllegalArgumentException("Widget type cannot be changed");
    }
    if (!read(page, widget, language).revision().equals(expectedRevision)) {
      throw new ConcurrentModificationException("Widget changed since it was loaded");
    }
    registerWidget(properties, widget);
    configureLayout(properties, prefix, title, layout);
    properties.setProperty(prefix + ".type", "subscriptions");
    properties.setProperty(prefix + ".size", String.valueOf(configuration.size()));
    properties.setProperty(prefix + ".showMore", String.valueOf(configuration.showMore()));
    saveProperties(page, properties);
  }

  public record FaqConfiguration(String source, int size, String selection) {
    public FaqConfiguration {
      if (source == null || !source.matches("(?:|questionReply[0-9]+)") || size < 1 || size > 100 || selection == null ||
          !java.util.List.of("random", "first").contains(selection)) {
        throw new IllegalArgumentException("Invalid FAQ configuration");
      }
    }
  }

  public record UsersConfiguration(String group, int size, boolean avatar, String fields) {
    public UsersConfiguration {
      if (group == null || !group.matches("(?:|-1|[0-9]+)") || size < 0 || size > 100 || fields == null ||
          !fields.matches("(?:|[A-Za-z][A-Za-z0-9_-]*(?: [A-Za-z][A-Za-z0-9_-]*)*)") || fields.length() > 500) {
        throw new IllegalArgumentException("Invalid new users configuration");
      }
    }
  }

  public synchronized void saveCommunityWidget(HomepagePage page, String widget, String language,
      String title, Layout layout, String type, FaqConfiguration faq, UsersConfiguration users,
      String expectedRevision) throws IOException {
    if (!java.util.List.of("faq", "users").contains(type) ||
        ("faq".equals(type) ? faq == null : users == null)) {
      throw new IllegalArgumentException("Invalid community widget");
    }
    HomepagePage.validateIdentifier(widget);
    var properties = properties(page);
    String prefix = "widget." + widget;
    String reservedType = switch (widget) {
      case "weather-legacy" -> "weather";
      case "bookmarks-legacy" -> "bookmarks";
      case "search-legacy" -> "search";
      case "shortcuts-legacy" -> "shortcuts";
      case "subscriptions-legacy" -> "subscriptions";
      case "faq-legacy" -> "faq";
      case "users-legacy" -> "users";
      case "events-legacy" -> "events";
      case "rss-legacy" -> "rss";
      case "ephemeris-legacy" -> "ephemeris";
      case "publications-legacy" -> "publications";
      default -> HomepageWidgets.legacyIndex(widget) != 0 ? "news" : type;
    };
    if (!type.equals(reservedType) || !type.equals(properties.getProperty(prefix + ".type", type))) {
      throw new IllegalArgumentException("Widget type cannot be changed");
    }
    if (!read(page, widget, language).revision().equals(expectedRevision)) {
      throw new ConcurrentModificationException("Widget changed since it was loaded");
    }
    registerWidget(properties, widget);
    configureLayout(properties, prefix, title, layout);
    properties.setProperty(prefix + ".type", type);
    if ("faq".equals(type)) {
      properties.setProperty(prefix + ".source", faq.source());
      properties.setProperty(prefix + ".size", String.valueOf(faq.size()));
      properties.setProperty(prefix + ".selection", faq.selection());
    } else {
      properties.setProperty(prefix + ".group", users.group());
      properties.setProperty(prefix + ".size", String.valueOf(users.size()));
      properties.setProperty(prefix + ".avatar", String.valueOf(users.avatar()));
      properties.setProperty(prefix + ".fields", users.fields());
    }
    saveProperties(page, properties);
  }

  public record EventsConfiguration(String source, int maxDays, boolean includeToday,
      boolean personal, boolean importantOnly, boolean showMore) {
    public EventsConfiguration {
      if (source == null || !source.matches("(?:|\\*|almanach[0-9]+(?: almanach[0-9]+)*)") ||
          maxDays < 1 || maxDays > 100) {
        throw new IllegalArgumentException("Invalid events configuration");
      }
    }
    public boolean isShowMore() { return showMore; }
  }

  public record RssConfiguration(String source, String rendering) {
    public RssConfiguration {
      if (source == null || !source.matches("(?:|rssAgregator[0-9]+)") ||
          !java.util.List.of("aggregate", "tabs", "carrousel").contains(rendering)) {
        throw new IllegalArgumentException("Invalid RSS configuration");
      }
    }
  }

  public synchronized void saveFeedWidget(HomepagePage page, String widget, String language,
      String title, Layout layout, String type, EventsConfiguration events,
      RssConfiguration rss, String expectedRevision) throws IOException {
    if (!java.util.List.of("events", "rss").contains(type) ||
        ("events".equals(type) ? events == null : rss == null)) {
      throw new IllegalArgumentException("Invalid feed widget");
    }
    var properties = properties(page);
    String prefix = "widget." + widget;
    HomepagePage.validateIdentifier(widget);
    String reservedType = switch (widget) {
      case "weather-legacy" -> "weather";
      case "bookmarks-legacy" -> "bookmarks";
      case "search-legacy" -> "search";
      case "shortcuts-legacy" -> "shortcuts";
      case "subscriptions-legacy" -> "subscriptions";
      case "faq-legacy" -> "faq";
      case "users-legacy" -> "users";
      case "events-legacy" -> "events";
      case "rss-legacy" -> "rss";
      case "ephemeris-legacy" -> "ephemeris";
      case "publications-legacy" -> "publications";
      default -> HomepageWidgets.legacyIndex(widget) != 0 ? "news" : type;
    };
    if (!type.equals(reservedType) || !type.equals(properties.getProperty(prefix + ".type", type))) {
      throw new IllegalArgumentException("Widget type cannot be changed");
    }
    if (!read(page, widget, language).revision().equals(expectedRevision)) {
      throw new ConcurrentModificationException("Widget changed since it was loaded");
    }
    registerWidget(properties, widget);
    configureLayout(properties, prefix, title, layout);
    properties.setProperty(prefix + ".type", type);
    if ("events".equals(type)) {
      properties.setProperty(prefix + ".source", events.source());
      properties.setProperty(prefix + ".maxDays", String.valueOf(events.maxDays()));
      properties.setProperty(prefix + ".includeToday", String.valueOf(events.includeToday()));
      properties.setProperty(prefix + ".personal", String.valueOf(events.personal()));
      properties.setProperty(prefix + ".importantOnly", String.valueOf(events.importantOnly()));
      properties.setProperty(prefix + ".showMore", String.valueOf(events.showMore()));
    } else {
      properties.setProperty(prefix + ".source", rss.source());
      properties.setProperty(prefix + ".rendering", rss.rendering());
    }
    saveProperties(page, properties);
  }

  public record PublicationsConfiguration(String spaceId, int size, boolean showMore) {
    public PublicationsConfiguration {
      if (spaceId == null || !spaceId.matches("(?:|(?:WA)?[0-9]+)") || size < 1 || size > 100) {
        throw new IllegalArgumentException("Invalid publications configuration");
      }
    }
    public boolean isShowMore() { return showMore; }
  }

  public synchronized void savePublicationsWidget(HomepagePage page, String widget,
      String language, String title, Layout layout, PublicationsConfiguration configuration,
      String expectedRevision) throws IOException {
    var properties = properties(page);
    String prefix = "widget." + widget;
    if ("weather-legacy".equals(widget) || "bookmarks-legacy".equals(widget) || "search-legacy".equals(widget) || "shortcuts-legacy".equals(widget) || "subscriptions-legacy".equals(widget) || "faq-legacy".equals(widget) || "users-legacy".equals(widget) || "events-legacy".equals(widget) || "rss-legacy".equals(widget) || "ephemeris-legacy".equals(widget) || HomepageWidgets.legacyIndex(widget) != 0 ||
        !"publications".equals(properties.getProperty(prefix + ".type", "publications"))) {
      throw new IllegalArgumentException("Widget type cannot be changed");
    }
    if (!read(page, widget, language).revision().equals(expectedRevision)) {
      throw new ConcurrentModificationException("Widget changed since it was loaded");
    }
    registerWidget(properties, widget);
    configureLayout(properties, prefix, title, layout);
    properties.setProperty(prefix + ".type", "publications");
    properties.setProperty(prefix + ".spaceId", configuration.spaceId());
    properties.setProperty(prefix + ".size", String.valueOf(configuration.size()));
    properties.setProperty(prefix + ".showMore", String.valueOf(configuration.showMore()));
    saveProperties(page, properties);
  }

  public synchronized void saveAsset(HomepagePage page, String widget, String name, byte[] bytes)
      throws IOException {
    HomepagePage.validateIdentifier(widget);
    validateAssetName(name);
    atomicWrite(path(page.kind(), page.owner(), "widgets", widget, "assets", name), bytes);
  }

  public synchronized byte[] readAsset(HomepagePage page, String widget, String name)
      throws IOException {
    HomepagePage.validateIdentifier(widget);
    validateAssetName(name);
    return Files.readAllBytes(path(page.kind(), page.owner(), "widgets", widget, "assets", name));
  }

  private void validateAssetName(String name) {
    if (name == null || !name.matches("[a-f0-9-]{36}\\.png")) {
      throw new IllegalArgumentException("Invalid image identifier");
    }
  }

  private Path propertiesPath(HomepagePage page) throws IOException {
    return "spaces".equals(page.kind()) ? path("spaces", page.owner() + ".properties")
        : path(page.kind(), page.owner(), "page.properties");
  }

  private Properties properties(HomepagePage page) throws IOException {
    Path file = propertiesPath(page);
    Properties properties = new Properties();
    if (Files.exists(file)) {
      try (var reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
        properties.load(reader);
      }
    }
    return properties;
  }

  private Path contentPath(HomepagePage page, String widget, String language) throws IOException {
    HomepagePage.validateIdentifier(widget);
    if (language == null || !language.matches("[a-z]{2,3}(-[A-Za-z]{2,8})?")) {
      throw new IllegalArgumentException("Invalid language");
    }
    return path(page.kind(), page.owner(), "widgets", widget, "content-" + language + ".html");
  }

  private Path path(String... segments) throws IOException {
    Path file = root.resolve(Path.of("", segments)).normalize();
    if (!file.startsWith(root)) {
      throw new IOException("Path outside homepage storage");
    }
    // Reject links in the entire path, including parents of the configured root.
    Path checked = file.getRoot();
    for (Path segment : file) {
      checked = checked.resolve(segment);
      if (Files.isSymbolicLink(checked)) {
        throw new IOException("Symbolic links are not supported in homepage storage");
      }
    }
    return file;
  }

  private void atomicWrite(Path target, byte[] bytes) throws IOException {
    Files.createDirectories(target.getParent());
    Path temporary = Files.createTempFile(target.getParent(), ".homepage-", ".tmp");
    try {
      Files.write(temporary, bytes);
      Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE,
          StandardCopyOption.REPLACE_EXISTING);
    } finally {
      Files.deleteIfExists(temporary);
    }
  }

  private String revision(String title, String html) {
    if (title.isEmpty() && html.isEmpty()) {
      return "";
    }
    try {
      return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
          .digest((title + "\u0000" + html).getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }
}
