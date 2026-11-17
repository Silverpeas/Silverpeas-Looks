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

  public record Widget(String id, Content content) {
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
      result.add(new Widget(id, read(page, id, language)));
    }
    result.sort(java.util.Comparator.comparingInt(Widget::getOrder).thenComparing(Widget::getId));
    return java.util.List.copyOf(result);
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
    String version = properties.containsKey(prefix + ".type")
        ? revision(title + layout, html) : "";
    return new Content(title, html, version, layout);
  }

  public synchronized void save(HomepagePage page, String widget, String language,
      String title, String html, String expectedRevision) throws IOException {
    saveConfigured(page, widget, language, title, html, expectedRevision,
        read(page, widget, language).layout());
  }

  public synchronized void saveConfigured(HomepagePage page, String widget, String language,
      String title, String html, String expectedRevision, Layout layout) throws IOException {
    Content previous = read(page, widget, language);
    if (!previous.revision().equals(expectedRevision)) {
      throw new ConcurrentModificationException("Content changed since it was loaded");
    }
    Properties properties = properties(page);
    String prefix = "widget." + widget;
    properties.setProperty("page.configuration.version", "1");
    String widgets = properties.getProperty("widgets", "");
    var ids = Arrays.stream(widgets.split(",")).filter(s -> !s.isBlank())
        .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
    ids.add(widget);
    properties.setProperty("widgets", String.join(",", ids));
    properties.setProperty(prefix + ".type", "html");
    properties.setProperty(prefix + ".title", title);
    properties.setProperty(prefix + ".zone", layout.zone());
    properties.setProperty(prefix + ".enabled", String.valueOf(layout.enabled()));
    properties.setProperty(prefix + ".order", String.valueOf(layout.order()));
    atomicWrite(contentPath(page, widget, language), html.getBytes(StandardCharsets.UTF_8));
    StringWriter writer = new StringWriter();
    properties.store(writer, "Aurora homepage prototype");
    atomicWrite(path(page.kind(), page.owner(), "page.properties"),
        writer.toString().getBytes(StandardCharsets.UTF_8));
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
