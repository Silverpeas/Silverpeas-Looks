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

class HomepageJspCompatibilityTest {
  @TempDir
  Path temporary;

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
  void ephemerisUsesTheCommonRendererWhileWeatherRemainsSeparate() throws Exception {
    String main = Files.readString(Path.of("src/main/webapp/look/jsp/Main.jsp"));
    String widgets = Files.readString(Path.of("src/main/webapp/WEB-INF/tags/silverpeas/look/displayHomepageWidgets.tag"));
    String weather = Files.readString(Path.of("src/main/webapp/WEB-INF/tags/silverpeas/look/displayWeather.tag"));
    assertTrue(main.contains("weatherOnly=\"${true}\" showEphemeris=\"${false}\""));
    assertTrue(main.contains("showWeather=\"${showWeather and settings.displayEphemeris}\""));
    assertTrue(widgets.contains("widget.type == 'ephemeris'"));
    assertTrue(widgets.contains("<viewTags:displayEphemeris id=\"homepage-widget-${widget.id}\""));
    assertTrue(weather.contains("${showEphemeris or (weatherOnly and showWeather)}"));
    assertTrue(weather.contains("<c:if test=\"${showEphemeris}\"><div id=\"ephemeride\""));
  }

  @Test
  void homepageScriptletCompilesWithTheLegacyJspSourceLevel() throws Exception {
    String jsp = Files.readString(Path.of("src/main/webapp/look/jsp/Main.jsp"));
    String imports = Pattern.compile("<%@ page import=\"([^\"]+)\" %>")
        .matcher(jsp).results().map(match -> "import " + match.group(1) + ";")
        .collect(Collectors.joining("\n"));
    String scriptlets = Pattern.compile("<%(?![@!=-])([\\s\\S]*?)%>")
        .matcher(jsp).results().map(match -> match.group(1))
        .collect(Collectors.joining("\n"));
    Path source = temporary.resolve("HomepageScriptlet.java");
    Files.writeString(source, imports + "\nclass HomepageScriptlet {\n" +
        "void render(jakarta.servlet.http.HttpSession session, " +
        "jakarta.servlet.http.HttpServletRequest request) throws Exception {\n" +
        scriptlets + "\n}\n}");
    var compiler = ToolProvider.getSystemJavaCompiler();
    var diagnostics = new DiagnosticCollector<JavaFileObject>();
    try (var files = compiler.getStandardFileManager(diagnostics, null, null)) {
      // Keep JDK APIs available, but enforce the source syntax used by the JSP compiler.
      var options = List.of("-source", "8", "-target", "8", "-proc:none",
          "-classpath", System.getProperty("java.class.path"), "-d", temporary.toString());
      boolean compiled = compiler.getTask(null, files, diagnostics, options, null,
          files.getJavaFileObjects(source)).call();
      assertTrue(compiled, () -> diagnostics.getDiagnostics().toString());
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
    for (String view : List.of("manage.jsp", "contentEditor.jsp", "newsEditor.jsp", "publicationsEditor.jsp", "ephemerisEditor.jsp", "eventsEditor.jsp", "rssEditor.jsp", "faqEditor.jsp", "usersEditor.jsp", "subscriptionsEditor.jsp")) {
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

}
