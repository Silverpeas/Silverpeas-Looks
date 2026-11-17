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
      NewsConfiguration newsConfiguration, int legacyIndex) {
    public Widget(String id, Content content) {
      this(id, content, "html", null, 0);
    }
    public String getType() { return type; }
    public String getId() { return id; }
    public String getTitle() { return content.title(); }
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
            content.revision()) : null;
    return new Widget(widget, content, type, news,
        Integer.parseInt(settings.getProperty(prefix + ".legacyIndex", "0")));
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
    String newsVersion = java.util.List.of("source", "size", "importantOnly", "rendering", "legacyIndex")
        .stream().map(key -> properties.getProperty(prefix + "." + key, ""))
        .collect(Collectors.joining("\u0000"));
    String version = properties.containsKey(prefix + ".type")
        ? revision("news".equals(properties.getProperty(prefix + ".type"))
            ? title + layout + newsVersion : title + layout, html) : "";
    return new Content(title, html, version, layout);
  }

  public synchronized void save(HomepagePage page, String widget, String language,
      String title, String html, String expectedRevision) throws IOException {
    saveConfigured(page, widget, language, title, html, expectedRevision,
        read(page, widget, language).layout());
  }

  public synchronized void saveConfigured(HomepagePage page, String widget, String language,
      String title, String html, String expectedRevision, Layout layout) throws IOException {
    if (HomepageWidgets.legacyIndex(widget) != 0 || !"html".equals(properties(page).getProperty("widget." + widget + ".type", "html"))) {
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

  private void registerWidget(Properties properties, String widget) {
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
    atomicWrite(path(page.kind(), page.owner(), "page.properties"),
        writer.toString().getBytes(StandardCharsets.UTF_8));
  }

  public synchronized void saveNewsWidget(HomepagePage page, String widget, String language,
      String title, Layout layout, NewsConfiguration news, int legacyIndex) throws IOException {
    if (legacyIndex < 0 || legacyIndex > 3) {
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
    saveProperties(page, properties);
  }

  public record NewsConfiguration(String source, int size, boolean importantOnly,
      String rendering, boolean enabled, String revision) {
    public NewsConfiguration {
      if (source == null || !source.matches("(?:|\\*|delegated|taxonomy|quickinfo[0-9]+(?: quickinfo[0-9]+)*)")
          || (size != -1 && (size < 1 || size > 100)) || !("LIST".equals(rendering) || "CAROUSEL".equals(rendering))) {
        throw new IllegalArgumentException("Invalid news configuration");
      }
    }
  }

  public synchronized NewsConfiguration readNews(HomepagePage page, int index) throws IOException {
    Properties settings = new Properties();
    Path file = newsPath(page, index);
    if (!Files.exists(file)) {
      return null;
    }
    try (var reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
      settings.load(reader);
    }
    return new NewsConfiguration(settings.getProperty("source"),
        Integer.parseInt(settings.getProperty("size")),
        Boolean.parseBoolean(settings.getProperty("importantOnly")),
        settings.getProperty("rendering"), Boolean.parseBoolean(settings.getProperty("enabled")),
        revision("news", Files.readString(file, StandardCharsets.UTF_8)));
  }

  public synchronized void saveNews(HomepagePage page, int index, NewsConfiguration configuration)
      throws IOException {
    var previous = readNews(page, index);
    if (!(previous == null ? "" : previous.revision()).equals(configuration.revision())) {
      throw new ConcurrentModificationException("News configuration changed");
    }
    Properties settings = new Properties();
    settings.setProperty("source", configuration.source());
    settings.setProperty("size", String.valueOf(configuration.size()));
    settings.setProperty("importantOnly", String.valueOf(configuration.importantOnly()));
    settings.setProperty("rendering", configuration.rendering());
    settings.setProperty("enabled", String.valueOf(configuration.enabled()));
    StringWriter writer = new StringWriter();
    settings.store(writer, "Aurora homepage news");
    atomicWrite(newsPath(page, index), writer.toString().getBytes(StandardCharsets.UTF_8));
  }

  private Path newsPath(HomepagePage page, int index) throws IOException {
    if (!"skins".equals(page.kind()) || index < 1 || index > 3) {
      throw new IllegalArgumentException("Invalid news block");
    }
    return path(page.kind(), page.owner(), "news-" + index + ".properties");
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

  private Properties properties(HomepagePage page) throws IOException {
    Path file = path(page.kind(), page.owner(), "page.properties");
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
