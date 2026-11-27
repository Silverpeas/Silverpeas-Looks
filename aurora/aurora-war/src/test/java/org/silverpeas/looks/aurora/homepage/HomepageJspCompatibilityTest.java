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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class HomepageJspCompatibilityTest {
  @TempDir
  Path temporary;

  @Test
  void spaceAdministratorsAreScopedAndRenderSilverpeasProfiles() throws Exception {
    String catalogue = Files.readString(Path.of("src/main/webapp/WEB-INF/homepage/manage.jsp"));
    int start = catalogue.indexOf("<c:if test=\"${param.kind == 'spaces'}\">", catalogue.indexOf("homepage-catalog-grid"));
    int end = catalogue.indexOf("</c:if>", start);
    assertTrue(catalogue.substring(start, end).contains("${addSpaceAdminsUrl}"));
    String widgets = Files.readString(Path.of("src/main/webapp/WEB-INF/tags/silverpeas/look/displayHomepageWidgets.tag"));
    assertTrue(widgets.contains("widget.type == 'spaceAdmins' and homepageKind == 'spaces'"));
    assertTrue(widgets.contains("<view:username userId=\"${admin.id}\"/>"));
    assertTrue(widgets.contains("<c:out value=\"${widget.title}\"/>"));
  }

  @Test
  void spaceNewsAndDeleteConfirmationHaveScopedLayoutRules() throws Exception {
    Path web = Path.of("src/main/webapp");
    String news = Files.readString(web.resolve("WEB-INF/tags/silverpeas/look/displayNews.tag"));
    assertTrue(news.contains("secteur-container homepage-news"));
    String css = Files.readString(web.resolve("look/jsp/css/homepage-space.css"));
    assertTrue(css.contains(".homepage-space-columns .homepage-news .caption {\n  position: static;"));
    assertTrue(css.contains(".homepage-space-columns .homepage-news .list-news > li > a"));
    assertTrue(css.contains(".homepage-space-columns .homepage-news .rslides > li {\n  height: auto;"));
    String management = Files.readString(web.resolve("look/jsp/css/homepage-content.css"));
    assertTrue(management.contains(".homepage-delete-dialog-actions > button {"));
    assertTrue(management.contains("height: 42px;"));
  }

  @Test
  void imageIsAvailableInBothCataloguesAndUsesScopedAssetsAndEscapedAttributes() throws Exception {
    String catalogue = Files.readString(Path.of("src/main/webapp/WEB-INF/homepage/manage.jsp"));
    assertTrue(catalogue.contains("${addImageUrl}"));
    assertTrue(catalogue.contains("widget.type == 'image' ? 'imageEditor'"));
    String widgets = Files.readString(Path.of("src/main/webapp/WEB-INF/tags/silverpeas/look/displayHomepageWidgets.tag"));
    assertTrue(widgets.contains("widget.type == 'image'"));
    assertTrue(widgets.contains("${widget.imageConfiguration.fileName}"));
    assertTrue(widgets.contains("<c:out value='${widget.imageConfiguration.alt}'/>"));
    assertTrue(widgets.contains("<c:out value='${widget.imageConfiguration.url}'/>"));
    assertTrue(widgets.contains("rel=\"noopener noreferrer\""));
    for (String name : List.of("Main.jsp", "spaceHomePage.jsp")) {
      assertTrue(Files.readString(Path.of("src/main/webapp/look/jsp", name)).contains("/look/jsp/css/homepage-image.css"));
    }
    String editor = Files.readString(Path.of("src/main/webapp/WEB-INF/homepage/imageEditor.jsp"));
    assertTrue(editor.contains("type=\"file\""));
    assertTrue(editor.contains("class=\"sp_button\""));
    assertTrue(editor.contains("class=\"simple-button\""));
    assertFalse(editor.contains("<script>"));
  }

  @Test
  void navigationCatalogueAndRenderingAreRestrictedToSpacesAndEscapeContent() throws Exception {
    String manage = Files.readString(Path.of("src/main/webapp/WEB-INF/homepage/manage.jsp"));
    assertTrue(manage.contains("<c:if test=\"${param.kind == 'spaces'}\">"));
    assertTrue(manage.contains("${addNavigationUrl}"));
    String tag = Files.readString(Path.of("src/main/webapp/WEB-INF/tags/silverpeas/look/displayHomepageWidgets.tag"));
    assertTrue(tag.contains("widget.type == 'navigation' and homepageKind == 'spaces'"));
    assertTrue(tag.contains("<c:out value=\"${item.label}\"/>"));
    assertTrue(tag.contains("<c:out value=\"${item.description}\"/>"));
    assertTrue(tag.contains("/Component/${item.id}"));
    assertTrue(tag.contains("homepage-navigation-${group}"));
    assertTrue(tag.contains("item.space == (group == 'spaces')"));
    assertTrue(tag.contains("not item.space and not empty item.icon"));
    assertTrue(tag.contains("<c:out value='${item.icon}'/>"));
    assertTrue(tag.contains("/Space/${item.id}"));
    String editor = Files.readString(Path.of("src/main/webapp/WEB-INF/homepage/navigationEditor.jsp"));
    assertFalse(editor.contains("<script>"));
    assertTrue(editor.contains("id=\"news-apps\""));
    assertTrue(editor.contains("id=\"news-subspaces\""));
  }

  @Test
  void spaceWidgetsReuseSkinStylesWithoutTheGlobalHomepageLayout() throws Exception {
    String page = Files.readString(Path.of("src/main/webapp/look/jsp/spaceHomePage.jsp"));
    assertTrue(page.contains("class=\"main-home-page homepage-space-columns\""));
    assertTrue(page.contains("/look/jsp/css/homepage-main.css?version="));
    assertTrue(page.indexOf("homepage-main.css") < page.indexOf("homepage-space.css"));
    assertFalse(page.contains("principal-main-container"));
    String css = Files.readString(Path.of("src/main/webapp/look/jsp/css/homepage-space.css"));
    assertTrue(css.contains(".homepage-publications .last-publication-list li"));
    assertTrue(css.contains("var(--bgColor-de-fond-clair-bloc-home, #fff)"));
    assertTrue(css.contains("repeat(auto-fit, minmax(min(100%, 14rem), 1fr))"));
  }

  @Test
  void spacesUseTheOperationMenuAndKeepWidgetRequestsAndMoreLinksScoped() throws Exception {
    String page = Files.readString(Path.of("src/main/webapp/look/jsp/spaceHomePage.jsp"));
    assertTrue(page.contains("<view:operation action=\"${manageHomepageUrl}\""));
    assertFalse(page.contains("homepage-edit-button"));
    assertFalse(page.contains("spaceHomepageProxy"));
    assertTrue(page.contains("zone=\"main\""));
    assertTrue(page.contains("zone=\"right\""));
    String widgets = Files.readString(Path.of("src/main/webapp/WEB-INF/tags/silverpeas/look/displayHomepageWidgets.tag"));
    assertTrue(widgets.contains("empty homepageKind ? 'skins' : homepageKind"));
    assertTrue(widgets.contains("spaceId=\"${homepageKind == 'spaces' ? homepageOwner : ''}\""));
    String search = Files.readString(Path.of("src/main/webapp/WEB-INF/homepage/searchWidget.jsp"));
    assertTrue(search.contains("extraSearchFieldSpace"));
    assertTrue(search.contains("data-space-id="));
    for (String tag : List.of("displayNews", "displayPublications")) {
      String view = Files.readString(Path.of("src/main/webapp/WEB-INF/tags/silverpeas/look/" + tag + ".tag"));
      assertTrue(view.contains("<c:param name=\"SpaceId\" value=\"${spaceId}\"/>"));
    }
  }

  @Test
  void skinsKeepToolsAndWeatherButDoNotConfigureHomepageWidgets() throws Exception {
    Path settings = Path.of("../aurora-configuration/src/main/config/properties/org/silverpeas/util/viewGenerator/settings");
    for (String skin : List.of("Aurora", "Sobre", "prima", "waves")) {
      var properties = new java.util.Properties();
      try (var reader = Files.newBufferedReader(settings.resolve(skin + ".properties"))) {
        properties.load(reader);
      }
      assertTrue(properties.containsKey("Shortcut.tool.1.Url"), skin);
      assertTrue(properties.containsKey("home.weather.settings"), skin);
      assertTrue(properties.containsKey("space.homepage.news"), skin);
      for (String key : List.of("FondManager", "JavaScript.kmelia",
          "space.homepage.management.customtemplate.WAXX", "home.news.height",
          "home.news.taxonomy.position", "home.news.taxonomy.display",
          "home.publications.components.excluded", "home.publications.spaces.excluded")) {
        assertFalse(properties.containsKey(key), skin + ": redundant setting " + key);
      }
      for (String key : properties.stringPropertyNames()) {
        assertFalse(key.startsWith("Shortcut.home."), key);
      }
      for (String key : List.of("home.news", "home.events.appId", "home.weather", "home.ephemeris",
          "home.freezone.appId", "home.freezone.thin.appId", "home.search", "home.faq.appId",
          "home.users.nb", "home.rss.appId", "home.subscription.news")) {
        assertFalse(properties.containsKey(key), skin + ": " + key);
      }
    }
    String main = Files.readString(Path.of("src/main/webapp/look/jsp/Main.jsp"));
    assertFalse(main.contains("<viewTags:displayFreeZone"));
    String banner = Files.readString(Path.of("src/main/webapp/look/jsp/TopBar.jsp"));
    assertTrue(banner.contains("lookHelper.toolsShortcuts"));
  }

  @Test
  void managementProvidesEscapedDeletionControlsAndExternalScript() throws Exception {
    String view = Files.readString(Path.of("src/main/webapp/WEB-INF/homepage/manage.jsp"));
    assertTrue(view.indexOf("/look/jsp/js/homepage-management.js") >= 0);
    assertTrue(view.indexOf("/look/jsp/js/homepage-management.js") ==
        view.lastIndexOf("/look/jsp/js/homepage-management.js"));
    assertTrue(view.contains("${deleteButton}"));
    assertTrue(view.contains("<view:script src=\"/look/jsp/js/homepage-management.js"));
    assertTrue(view.contains("<svg "));
    assertTrue(view.contains("aria-label=\"Supprimer le widget\""));
    assertTrue(view.contains("<dialog "));
    for (String attribute : List.of("widget.id", "widget.revision", "homepageCsrf")) {
      assertTrue(view.contains("<c:out value='${" + attribute + "}'/>"));
    }
    assertFalse(view.contains("onclick="));
  }

  @Test
  void homepageUsesOneWidgetModelForBothColumnsAndNewsResources() throws Exception {
    String jsp = Files.readString(Path.of("src/main/webapp/look/jsp/Main.jsp"));
    assertTrue(jsp.contains("request.setAttribute(\"homepageWidgets\", homepageWidgets)"));
    assertTrue(jsp.contains("request.setAttribute(\"homepageNews\", homepageNews)"));
    for (String zone : List.of("main", "right")) {
      assertTrue(jsp.contains("widgets=\"${homepageWidgets}\" news=\"${homepageNews}\" publications=\"${homepagePublications}\" questions=\"${homepageQuestions}\" users=\"${homepageUsers}\" subscriptions=\"${homepageSubscriptions}\" zone=\"" + zone + "\""));
    }
    assertTrue(jsp.contains("homepageNews.values().stream()"));
    assertTrue(!jsp.contains("<viewTags:displayNews listOfNews="));
  }

  @Test
  void ephemerisAndWeatherUseSeparateManagedRenderers() throws Exception {
    String main = Files.readString(Path.of("src/main/webapp/look/jsp/Main.jsp"));
    String widgets = Files.readString(Path.of("src/main/webapp/WEB-INF/tags/silverpeas/look/displayHomepageWidgets.tag"));
    String weather = Files.readString(Path.of("src/main/webapp/WEB-INF/tags/silverpeas/look/displayWeather.tag"));
    assertFalse(main.contains("<viewTags:displayWeather "));
    assertTrue(widgets.contains("widget.type == 'weather'"));
    assertTrue(widgets.contains("widget.type == 'ephemeris'"));
    assertTrue(widgets.contains("<viewTags:displayEphemeris id=\"homepage-widget-${widget.id}\""));
    assertTrue(weather.contains("${showEphemeris or (weatherOnly and showWeather)}"));
    assertTrue(weather.contains("<c:if test=\"${showEphemeris}\"><div id=\"ephemeride\""));
  }

  @Test
  void homepageScriptletCompilesWithTheLegacyJspSourceLevel() throws Exception {
    for (String page : List.of("look/jsp/Main.jsp", "look/jsp/spaceHomePage.jsp", "WEB-INF/homepage/searchWidget.jsp")) {
      String jsp = Files.readString(Path.of("src/main/webapp", page));
      String imports = Pattern.compile("<%@ page import=\"([^\"]+)\" %>")
          .matcher(jsp).results().map(match -> "import " + match.group(1) + ";")
          .collect(Collectors.joining("\n"));
      String scriptlets = Pattern.compile("<%(?![@!=-])([\\s\\S]*?)%>")
          .matcher(jsp).results().map(match -> match.group(1))
          .collect(Collectors.joining("\n"));
      Path source = temporary.resolve("HomepageScriptlet.java");
      Files.writeString(source, imports + "\nclass HomepageScriptlet {\n" +
          "void render(jakarta.servlet.http.HttpSession session, " +
          "jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response, jakarta.servlet.jsp.JspWriter out) throws Exception {\n" +
          scriptlets + "\n}\n}");
      var compiler = ToolProvider.getSystemJavaCompiler();
      var diagnostics = new DiagnosticCollector<JavaFileObject>();
      try (var files = compiler.getStandardFileManager(diagnostics, null, null)) {
        var options = List.of("-source", "8", "-target", "8", "-proc:none",
            "-classpath", System.getProperty("java.class.path"), "-d", temporary.toString());
        boolean compiled = compiler.getTask(null, files, diagnostics, options, null,
            files.getJavaFileObjects(source)).call();
        assertTrue(compiled, () -> page + diagnostics.getDiagnostics().toString());
      }
    }
  }

  @Test
  void managementViewsUseSupportedSilverpeasTagsAndAttributes() throws Exception {
    var factory = DocumentBuilderFactory.newInstance();
    factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    var tags = new HashMap<String, Set<String>>();
    var required = new HashMap<String, Set<String>>();
    try (var stream = getClass().getResourceAsStream("/META-INF/viewGenerator.tld")) {
      var definitions = factory.newDocumentBuilder().parse(stream).getElementsByTagName("tag");
      for (int i = 0; i < definitions.getLength(); i++) {
        var definition = (org.w3c.dom.Element) definitions.item(i);
        String name = definition.getElementsByTagName("name").item(0).getTextContent().trim();
        var attributes = definition.getElementsByTagName("attribute");
        var allowed = new HashSet<String>();
        var mandatory = new HashSet<String>();
        for (int j = 0; j < attributes.getLength(); j++) {
          var attribute = (org.w3c.dom.Element) attributes.item(j);
          String attributeName = attribute.getElementsByTagName("name").item(0).getTextContent().trim();
          allowed.add(attributeName);
          var requirement = attribute.getElementsByTagName("required");
          if (requirement.getLength() > 0 &&
              "true".equals(requirement.item(0).getTextContent().trim())) {
            mandatory.add(attributeName);
          }
        }
        tags.put(name, allowed);
        required.put(name, mandatory);
      }
    }
    for (String view : List.of("manage.jsp", "contentEditor.jsp", "newsEditor.jsp", "publicationsEditor.jsp", "ephemerisEditor.jsp", "eventsEditor.jsp", "rssEditor.jsp", "faqEditor.jsp", "usersEditor.jsp", "subscriptionsEditor.jsp", "shortcutsEditor.jsp", "searchEditor.jsp", "weatherEditor.jsp", "bookmarksEditor.jsp", "bookmarksWidget.jsp")) {
      String jsp = Files.readString(Path.of("src/main/webapp/WEB-INF/homepage", view));
      var uses = Pattern.compile("<view:([\\w-]+)([^>]*?)/?>").matcher(jsp);
      while (uses.find()) {
        String name = uses.group(1);
        assertTrue(tags.containsKey(name), "Unknown Silverpeas tag: " + name);
        var used = Pattern.compile("([\\w-]+)=\"").matcher(uses.group(2)).results()
            .map(match -> match.group(1)).collect(Collectors.toSet());
        assertTrue(tags.get(name).containsAll(used), "Unsupported attributes on " + name);
        assertTrue(used.containsAll(required.get(name)), "Missing required attributes on " + name);
      }
    }
  }

  @Test
  void communityWidgetsUseUniqueIdsAndSharedStylesAcrossSkins() throws Exception {
    String main = Files.readString(Path.of("src/main/webapp/look/jsp/Main.jsp"));
    assertTrue(!main.contains("lookHelper.questions"));
    assertTrue(!main.contains("lookHelper.newUsersList"));
    for (String tag : List.of("displayFAQ.tag", "displayNewUsers.tag")) {
      String markup = Files.readString(Path.of("src/main/webapp/WEB-INF/tags/silverpeas/look", tag));
      assertTrue(markup.contains("<%@ attribute name=\"id\""));
      assertTrue(markup.contains("<c:out value=\"${empty title"));
    }
    Path base = Path.of("../aurora-configuration/src/main/config/data/web/weblib.war");
    for (String skin : List.of("Aurora/css/main.css", "skins/Sobre/css/main.css",
        "skins/prima/css/main.css", "skins/waves/css/stylesheet.css")) {
      String css = Files.readString(base.resolve(skin));
      assertTrue(css.contains(":is(#faq-home, .homepage-faq)"));
      assertTrue(!java.util.regex.Pattern.compile("#faq-home(?!, \\.homepage-faq)").matcher(css).find());
      assertTrue(!java.util.regex.Pattern.compile("#new-users-home(?!, \\.homepage-users)").matcher(css).find());
    }
  }

  @Test
  void subscriptionsRenderOnlyThroughTheCommonModelWithScopedIdsAndOptionalLink() throws Exception {
    String main = Files.readString(Path.of("src/main/webapp/look/jsp/Main.jsp"));
    String tag = Files.readString(Path.of("src/main/webapp/WEB-INF/tags/silverpeas/look/displayNewsSubscribed.tag"));
    String widgets = Files.readString(Path.of("src/main/webapp/WEB-INF/tags/silverpeas/look/displayHomepageWidgets.tag"));
    assertTrue(!main.contains("lookHelper.lastNewsSubscribed"));
    assertTrue(!main.contains("<viewTags:displayNewsSubscribed"));
    assertTrue(main.contains("HomepageWidgets.subscriptions(homepageWidgets, homepageHelper)"));
    assertTrue(widgets.contains("showMore=\"${widget.subscriptionsConfiguration.showMore}\""));
    assertTrue(tag.contains("id=\"${empty id ? 'myNews-subscription' : id}\""));
    assertTrue(tag.contains("<c:out value=\"${empty title ? labelTitle : title}\"/>"));
    assertTrue(tag.contains("${empty showMore or showMore}"));
    assertTrue(!tag.contains("id=\"myNews-link-page\""));
    assertTrue(tag.contains("<c:url var=\"allNewsUrl\" value=\"/look/jsp/listOfNewsBySubscription.jsp\"/>"));
  }


  @Test
  void homeShortcutsRenderThroughTheCommonModelWithEscapedLinksAndUniqueIds() throws Exception {
    String main = Files.readString(Path.of("src/main/webapp/look/jsp/Main.jsp"));
    String widgets = Files.readString(Path.of("src/main/webapp/WEB-INF/tags/silverpeas/look/displayHomepageWidgets.tag"));
    String tag = Files.readString(Path.of("src/main/webapp/WEB-INF/tags/silverpeas/look/displayShortcuts.tag"));
    assertFalse(main.contains("lookHelper.mainShortcuts"));
    assertFalse(main.contains("<viewTags:displayShortcuts"));
    assertTrue(widgets.contains("shortcuts=\"${widget.shortcutsConfiguration.entries}\""));
    assertTrue(tag.contains("id=\"<c:out"));
    assertTrue(tag.contains("<c:out value=\"${empty title ? labelShortcuts : title}\"/>"));
    assertTrue(tag.contains("href=\"<c:out value='${shortcut.url}'/>\""));
    assertTrue(tag.contains("rel=\"${shortcut.target == '_blank' ? 'noopener noreferrer' : ''}\""));
    assertTrue(tag.contains("<c:if test=\"${not empty shortcuts}\">"));
  }
  @Test
  void searchUsesOneManagedTypeAndIsolatesCoreGeneratedFieldIds() throws Exception {
    String main = Files.readString(Path.of("src/main/webapp/look/jsp/Main.jsp"));
    String widgets = Files.readString(Path.of("src/main/webapp/WEB-INF/tags/silverpeas/look/displayHomepageWidgets.tag"));
    String search = Files.readString(Path.of("src/main/webapp/WEB-INF/homepage/searchWidget.jsp"));
    assertFalse(main.contains("<viewTags:displayTaxonomy"));
    assertFalse(main.contains("<viewTags:displayFormSearch"));
    assertFalse(main.contains("lookHelper.mainSearchForm"));
    assertTrue(widgets.contains("widget.type == 'search'"));
    assertTrue(widgets.contains("value=\"renderSearch\""));
    assertTrue(widgets.contains("class=\"homepage-search-frame\""));
    assertTrue(search.contains("target=\"_parent\""));
    assertTrue(search.contains("name=\"AxisValueCouples\""));
    assertTrue(search.contains("name=\"xmlSearchSelectedForm\""));
    assertFalse(search.contains("javascript:"));
    assertFalse(search.contains("<script>"));
  }

  @Test
  void personalBookmarksUseManagedPlacementAndIsolateCoreWidgetIds() throws Exception {
    String main = Files.readString(Path.of("src/main/webapp/look/jsp/Main.jsp"));
    String widgets = Files.readString(Path.of("src/main/webapp/WEB-INF/tags/silverpeas/look/displayHomepageWidgets.tag"));
    String bookmarks = Files.readString(Path.of("src/main/webapp/WEB-INF/homepage/bookmarksWidget.jsp"));
    assertFalse(main.contains("<viewTags:displayUserBookmarks"));
    assertFalse(main.contains("lookHelper.bookmarks"));
    assertTrue(widgets.contains("value=\"renderBookmarks\""));
    assertTrue(widgets.contains("homepage-bookmarks-frame"));
    assertTrue(bookmarks.contains("data-links=\"<c:out"));
    assertTrue(bookmarks.contains("data-fragment=\"<c:out"));
    assertTrue(bookmarks.contains("target=\"_parent\""));
    assertFalse(bookmarks.contains("<script>"));
    assertFalse(bookmarks.contains("javascript:"));
    assertFalse(bookmarks.contains("style=\""));
  }

  @Test
  void weatherUsesScopedControlsAndEscapesCitiesWithoutInlineCode() throws Exception {
    String main = Files.readString(Path.of("src/main/webapp/look/jsp/Main.jsp"));
    String weather = Files.readString(Path.of("src/main/webapp/WEB-INF/tags/silverpeas/look/displayHomepageWeather.tag"));
    assertFalse(main.contains("<viewTags:displayWeather "));
    assertTrue(weather.contains("configuredWeatherCities"));
    assertFalse(weather.contains("widget.weatherConfiguration"));
    assertTrue(weather.contains("data-city=\"<c:out"));
    assertTrue(weather.contains("<c:out value=\"${city.name}\""));
    assertTrue(weather.contains("homepage-widget-${widget.id}"));
    assertTrue(weather.contains("configuredWeatherCities"));
    assertFalse(weather.contains("widget.weatherConfiguration"));
    assertFalse(weather.contains("onclick="));
    assertFalse(weather.contains("<script"));
  }
}
