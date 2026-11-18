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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;
import org.silverpeas.kernel.bundle.SettingBundle;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class HomepageContentServletTest {
  @TempDir
  Path root;
  private HomepageContentStore store;
  private HomepageContentServlet servlet;
  private HttpServletRequest request;
  private HttpServletResponse response;
  private HttpSession session;
  private MainSessionController controller;
  private UserDetail user;
  private StringWriter output;

  @BeforeEach
  void prepare() throws Exception {
    store = new HomepageContentStore(root);
    servlet = new HomepageContentServlet(store);
    request = mock(HttpServletRequest.class);
    response = mock(HttpServletResponse.class);
    session = mock(HttpSession.class);
    controller = mock(MainSessionController.class);
    user = mock(UserDetail.class);
    output = new StringWriter();
    when(response.getWriter()).thenReturn(new PrintWriter(output));
    when(request.getSession(false)).thenReturn(session);
    when(session.getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT)).thenReturn(controller);
    when(session.getAttribute("AuroraHomepageContent.csrf")).thenReturn("test-token");
    when(controller.getCurrentUserDetail()).thenReturn(user);
    when(user.isActivatedState()).thenReturn(true);
    when(user.getId()).thenReturn("42");
    when(controller.getFavoriteLook()).thenReturn("Initial");
    var factory = mock(GraphicElementFactory.class);
    var skin = mock(SettingBundle.class);
    when(factory.getFavoriteLookSettings()).thenReturn(skin);
    when(skin.getBaseBundleName()).thenReturn("org.silverpeas.util.viewGenerator.settings.waves");
    when(session.getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT)).thenReturn(factory);
    when(request.getParameter("kind")).thenReturn("skins");
    when(request.getParameter("owner")).thenReturn("waves");
    when(request.getParameter("widget")).thenReturn("welcome");
    when(request.getParameter("language")).thenReturn("fr");
    var helper = mock(org.silverpeas.looks.aurora.LookAuroraHelper.class);
    var settings = mock(org.silverpeas.looks.aurora.LookSettings.class);
    when(settings.isDisplayEphemeris()).thenReturn(true);
    when(helper.getLookSettings()).thenReturn(settings);
    when(settings.getNoBookmarksFragmentURL()).thenReturn("");
    when(helper.getDefaultSearchConfiguration()).thenReturn(
        new HomepageContentStore.SearchConfiguration("taxonomy", "", false, false, false));
    when(helper.getDefaultSubscriptionsConfiguration()).thenReturn(
        new HomepageContentStore.SubscriptionsConfiguration(4, true));
    when(settings.isDisplayLastSubscribedNews()).thenReturn(true);
    when(helper.getDefaultFaqConfiguration()).thenReturn(
        new HomepageContentStore.FaqConfiguration("questionReply12", 2, "random"));
    when(helper.getDefaultUsersConfiguration()).thenReturn(
        new HomepageContentStore.UsersConfiguration("12", 5, true, "title phone"));
    when(helper.getDefaultEventsConfiguration()).thenReturn(
        new HomepageContentStore.EventsConfiguration("almanach12", 3, true, false, true, true));
    when(helper.getDefaultRssConfiguration()).thenReturn(
        new HomepageContentStore.RssConfiguration("rssAgregator12", "tabs"));
    when(helper.getDefaultPublicationsConfiguration()).thenReturn(
        new HomepageContentStore.PublicationsConfiguration("WA12", 3, true));
    when(helper.getDefaultNewsConfiguration(anyInt())).thenReturn(
        new HomepageContentStore.NewsConfiguration("", -1, false, "LIST", false, ""));
    when(session.getAttribute("Silverpeas_LookHelper")).thenReturn(helper);
  }

  private void prepareNewsSave() {
    when(request.getParameter("widget")).thenReturn(null);
    when(user.isAccessAdmin()).thenReturn(true);
    when(request.getParameter("action")).thenReturn("saveNews");
    when(request.getHeader("X-Aurora-CSRF")).thenReturn("test-token");
    when(request.getParameter("index")).thenReturn("1");
    when(request.getParameter("source")).thenReturn("quickinfo12");
    when(request.getParameter("size")).thenReturn("5");
    when(request.getParameter("rendering")).thenReturn("LIST");
    when(request.getParameter("enabled")).thenReturn("true");
    when(request.getParameter("importantOnly")).thenReturn("true");
    when(request.getParameter("revision")).thenReturn("");
  }

  @Test
  void ephemerisEnforcesRightsCsrfAndRevisionAndReturnsSavedPlacement() throws Exception {
    prepareNewsSave();
    when(request.getParameter("action")).thenReturn("saveEphemeris");
    when(request.getParameter("widget")).thenReturn("ephemeris-test");
    when(request.getParameter("title")).thenReturn("Aujourd’hui");
    when(request.getParameter("zone")).thenReturn("main");
    when(request.getParameter("order")).thenReturn("8");
    when(user.isAccessAdmin()).thenReturn(false);
    servlet.doPost(request, response);
    verify(response).sendError(403);
    when(user.isAccessAdmin()).thenReturn(true);
    when(request.getHeader("X-Aurora-CSRF")).thenReturn(null);
    clearInvocations(response);
    servlet.doPost(request, response);
    verify(response).sendError(403);
    when(request.getHeader("X-Aurora-CSRF")).thenReturn("test-token");
    clearInvocations(response);
    servlet.doPost(request, response);
    verify(response, never()).sendError(anyInt());
    var saved = store.readWidget(new HomepagePage("skins", "waves"), "ephemeris-test", "fr");
    assertEquals("ephemeris", saved.type());
    assertEquals("main", saved.getZone());
    assertTrue(output.toString().contains("\"order\":8"));
    servlet.doPost(request, response);
    verify(response).sendError(409);
  }

  @Test
  void publicationsEnforceRightsCsrfPersistSettingsAndRejectStaleRevision() throws Exception {
    prepareNewsSave();
    when(request.getParameter("action")).thenReturn("savePublications");
    when(request.getParameter("widget")).thenReturn("publications-test");
    when(request.getParameter("title")).thenReturn("Documents");
    when(request.getParameter("zone")).thenReturn("right");
    when(request.getParameter("order")).thenReturn("4");
    when(request.getParameter("spaceId")).thenReturn("WA12");
    when(request.getParameter("showMore")).thenReturn("false");
    when(user.isAccessAdmin()).thenReturn(false);
    servlet.doPost(request, response);
    verify(response).sendError(403);
    when(user.isAccessAdmin()).thenReturn(true);
    when(request.getHeader("X-Aurora-CSRF")).thenReturn(null);
    clearInvocations(response);
    servlet.doPost(request, response);
    verify(response).sendError(403);
    when(request.getHeader("X-Aurora-CSRF")).thenReturn("test-token");
    clearInvocations(response);
    servlet.doPost(request, response);
    verify(response, never()).sendError(anyInt());
    var saved = store.readWidget(new HomepagePage("skins", "waves"), "publications-test", "fr");
    assertEquals("right", saved.getZone());
    assertFalse(saved.publicationsConfiguration().showMore());
    assertTrue(output.toString().contains("\"spaceId\":\"WA12\""));
    servlet.doPost(request, response);
    verify(response).sendError(409);
  }

  @Test
  void dynamicNewsUsesTheSharedPlacementAndRevision() throws Exception {
    prepareNewsSave();
    when(request.getParameter("widget")).thenReturn("news-fourth");
    when(request.getParameter("title")).thenReturn("Vie des équipes");
    when(request.getParameter("zone")).thenReturn("right");
    when(request.getParameter("order")).thenReturn("7");
    when(request.getParameter("showMoreNews")).thenReturn("false");
    when(request.getParameter("showAllNews")).thenReturn("true");
    servlet.doPost(request, response);
    verify(response, never()).sendError(anyInt());
    var widget = store.readWidget(new HomepagePage("skins", "waves"), "news-fourth", "fr");
    assertFalse(widget.newsConfiguration().showMoreNews());
    assertTrue(widget.newsConfiguration().showAllNews());
    assertEquals("news", widget.type());
    assertEquals("Vie des équipes", widget.getTitle());
    assertEquals("right", widget.getZone());
    assertEquals(7, widget.getOrder());
    assertTrue(widget.isEnabled());
    assertEquals("quickinfo12", widget.newsConfiguration().source());
    assertTrue(output.toString().contains("\"zone\":\"right\""));
    servlet.doPost(request, response);
    verify(response).sendError(409);
  }

  @Test
  void newsSaveEnforcesAdministratorCsrfAndRevision() throws Exception {
    prepareNewsSave();
    when(user.isAccessAdmin()).thenReturn(false);
    servlet.doPost(request, response);
    verify(response).sendError(403);
    assertNull(store.readNews(new HomepagePage("skins", "waves"), 1));
    when(user.isAccessAdmin()).thenReturn(true);
    when(request.getHeader("X-Aurora-CSRF")).thenReturn(null);
    clearInvocations(response);
    servlet.doPost(request, response);
    verify(response).sendError(403);
    when(request.getHeader("X-Aurora-CSRF")).thenReturn("test-token");
    clearInvocations(response);
    servlet.doPost(request, response);
    verify(response, never()).sendError(anyInt());
    assertEquals("quickinfo12", store.readNews(new HomepagePage("skins", "waves"), 1).source());
    assertTrue(output.toString().contains("revision"));
    servlet.doPost(request, response);
    verify(response).sendError(409);
  }

  @Test
  void newsEditorRedirectKeepsBlockIndex() throws Exception {
    when(user.isAccessAdmin()).thenReturn(true);
    when(request.getContextPath()).thenReturn("/silverpeas");
    when(request.getParameter("action")).thenReturn("newsEditor");
    when(request.getParameter("owner")).thenReturn(null);
    when(request.getParameter("index")).thenReturn("3");
    servlet.doGet(request, response);
    verify(response).sendRedirect(contains("&index=3"));
  }

  @Test
  void newsLoadUsesThemeConfigurationUntilSaved() throws Exception {
    when(user.isAccessAdmin()).thenReturn(true);
    when(request.getParameter("action")).thenReturn("loadNews");
    when(request.getParameter("widget")).thenReturn(null);
    when(request.getParameter("index")).thenReturn("2");
    var helper = mock(org.silverpeas.looks.aurora.LookAuroraHelper.class);
    when(session.getAttribute("Silverpeas_LookHelper")).thenReturn(helper);
    when(helper.getDefaultNewsConfiguration(2)).thenReturn(
        new HomepageContentStore.NewsConfiguration("delegated", -1, false, "LIST", true, ""));
    servlet.doGet(request, response);
    assertTrue(output.toString().contains("delegated"));
    assertTrue(output.toString().contains("\"configured\":false"));
    verify(response, never()).sendError(anyInt());
  }

  @Test
  void invalidNewsSaveCannotChangeStorage() throws Exception {
    prepareNewsSave();
    when(request.getParameter("size")).thenReturn("0");
    servlet.doPost(request, response);
    verify(response).sendError(400);
    assertNull(store.readNews(new HomepagePage("skins", "waves"), 1));
  }

  @Test
  void editorRequiresAnAuthenticatedSession() throws Exception {
    when(request.getSession(false)).thenReturn(null);
    servlet.doGet(request, response);
    verify(response).sendError(401);
  }

  @Test
  void regularUserCannotLoadEditorContentOrWrite() throws Exception {
    when(request.getParameter("action")).thenReturn("load");
    servlet.doGet(request, response);
    verify(response).sendError(403);
    clearInvocations(response);
    servlet.doPost(request, response);
    verify(response).sendError(403);
  }

  @Test
  void skinReadAccessIsLimitedToTheCurrentSkin() {
    assertTrue(servlet.isAllowed(controller, new HomepagePage("skins", "waves"), false, "waves"));
    assertFalse(servlet.isAllowed(controller, new HomepagePage("skins", "other"), false, "waves"));
    assertFalse(servlet.isAllowed(controller, new HomepagePage("skins", "waves"), true, "waves"));
  }

  @Test
  void spaceImagesUseSpaceReadRightsAndEditingUsesManageableSpaces() {
    var organization = mock(OrganizationController.class);
    try (var mocked = mockStatic(OrganizationController.class)) {
      mocked.when(OrganizationController::get).thenReturn(organization);
      when(organization.isSpaceAvailable("WA123", "42")).thenReturn(true);
      when(organization.getUserManageableSpaceIds("42")).thenReturn(new String[]{"WA456"});
      assertTrue(servlet.isAllowed(controller, new HomepagePage("spaces", "WA123"), false, "waves"));
      assertFalse(servlet.isAllowed(controller, new HomepagePage("spaces", "WA123"), true, "waves"));
      assertFalse(servlet.isAllowed(controller, new HomepagePage("spaces", "WA456"), false, "waves"));
      assertTrue(servlet.isAllowed(controller, new HomepagePage("spaces", "WA456"), true, "waves"));
    }
  }

  @Test
  void administratorMustProvideCsrfTokenBeforeWriting() throws Exception {
    when(user.isAccessAdmin()).thenReturn(true);
    when(request.getParameter("action")).thenReturn("save");
    servlet.doPost(request, response);
    verify(response).sendError(403);
    assertEquals("", store.read(new HomepagePage("skins", "waves"), "welcome", "fr").html());
  }

  @Test
  void saveSanitizesContentAndRejectsStaleRevision() throws Exception {
    when(user.isAccessAdmin()).thenReturn(true);
    when(request.getHeader("X-Aurora-CSRF")).thenReturn("test-token");
    when(request.getParameter("action")).thenReturn("save");
    when(request.getParameter("revision")).thenReturn("");
    when(request.getParameter("title")).thenReturn("Bienvenue");
    when(request.getParameter("html")).thenReturn("<p>Bonjour</p><img src=\"/silverpeas/AuroraHomepageContent?action=asset&amp;name=image.png\"><script>alert(1)</script>");
    servlet.doPost(request, response);
    verify(response, never()).sendError(anyInt());
    var content = store.read(new HomepagePage("skins", "waves"), "welcome", "fr");
    assertTrue(content.html().contains("Bonjour"));
    assertFalse(content.html().contains("script"));
    assertTrue(content.html().contains("/silverpeas/AuroraHomepageContent"));
    servlet.doPost(request, response);
    verify(response).sendError(409);
  }

  @Test
  void previewIsSandboxed() throws Exception {
    when(request.getParameter("action")).thenReturn("view");
    servlet.doGet(request, response);
    verify(response).setHeader(eq("Content-Security-Policy"), eq("sandbox allow-same-origin; default-src 'none'; img-src 'self'; style-src 'unsafe-inline'; frame-ancestors 'self'"));
    verify(response).setHeader("X-Content-Type-Options", "nosniff");
  }

  @Test
  void guessedImageUrlDoesNotBypassPagePermissions() throws Exception {
    when(request.getParameter("action")).thenReturn("asset");
    when(request.getParameter("owner")).thenReturn("other");
    servlet.doGet(request, response);
    verify(response).sendError(403);
  }
  @Test
  void uploadedRasterImageIsReencodedAndStoredUnderItsWidget() throws Exception {
    when(user.isAccessAdmin()).thenReturn(true);
    when(request.getHeader("X-Aurora-CSRF")).thenReturn("test-token");
    when(request.getContextPath()).thenReturn("/silverpeas");
    when(request.getParameter("action")).thenReturn("upload");
    var bytes = new ByteArrayOutputStream();
    ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", bytes);
    Part part = mock(Part.class);
    when(part.getSize()).thenReturn((long) bytes.size());
    when(part.getInputStream()).thenReturn(new ByteArrayInputStream(bytes.toByteArray()));
    when(request.getPart("image")).thenReturn(part);
    servlet.doPost(request, response);
    verify(response, never()).sendError(anyInt());
    verify(part).delete();
    assertTrue(output.toString().contains("action=asset"));
    try (var images = Files.list(root.resolve("skins/waves/widgets/welcome/assets"))) {
      Path file = images.findFirst().orElseThrow();
      assertEquals(2, ImageIO.read(file.toFile()).getWidth());
      assertTrue(file.getFileName().toString().endsWith(".png"));
    }
  }

  @Test
  void svgUploadIsRejected() throws Exception {
    when(user.isAccessAdmin()).thenReturn(true);
    when(request.getHeader("X-Aurora-CSRF")).thenReturn("test-token");
    when(request.getParameter("action")).thenReturn("upload");
    Part part = mock(Part.class);
    byte[] bytes = "<svg xmlns='http://www.w3.org/2000/svg'><script/></svg>".getBytes();
    when(part.getSize()).thenReturn((long) bytes.length);
    when(part.getInputStream()).thenReturn(new ByteArrayInputStream(bytes));
    when(request.getPart("image")).thenReturn(part);
    servlet.doPost(request, response);
    verify(response).sendError(400);
    verify(part).delete();
    assertFalse(Files.exists(root.resolve("skins/waves/widgets/welcome/assets")));
  }

  @Test
  void defaultAliasResolvesToTheActualAuroraBundle() {
    var factory = mock(GraphicElementFactory.class);
    var settings = mock(SettingBundle.class);
    when(factory.getFavoriteLookSettings()).thenReturn(settings);
    when(settings.getBaseBundleName()).thenReturn("org.silverpeas.util.viewGenerator.settings.Aurora");
    assertEquals("Aurora", HomepageContentServlet.skinId(factory));
  }

  @Test
  void editorRedirectsToTheEffectiveSkinInsteadOfTrustingTheUrl() throws Exception {
    when(user.isAccessAdmin()).thenReturn(true);
    when(request.getContextPath()).thenReturn("/silverpeas");
    when(request.getParameter("action")).thenReturn("editor");
    when(request.getParameter("owner")).thenReturn("wrong-skin");
    servlet.doGet(request, response);
    verify(response).sendRedirect("/silverpeas/AuroraHomepageContent?action=editor&kind=skins&owner=waves&widget=welcome&language=fr");
  }

  @Test
  void managementRedirectsToEffectiveSkin() throws Exception {
    when(user.isAccessAdmin()).thenReturn(true);
    when(request.getContextPath()).thenReturn("/silverpeas");
    when(request.getParameter("action")).thenReturn("manage");
    when(request.getParameter("owner")).thenReturn(null);
    servlet.doGet(request, response);
    verify(response).sendRedirect("/silverpeas/AuroraHomepageContent?action=manage&kind=skins&owner=waves&widget=welcome&language=fr");
  }

  @Test
  void managementListsWidgetsAndUsesFavoriteLanguageWhenMissing() throws Exception {
    when(user.isAccessAdmin()).thenReturn(true);
    when(request.getParameter("action")).thenReturn("manage");
    when(request.getParameter("language")).thenReturn(null);
    when(controller.getFavoriteLanguage()).thenReturn("fr");
    var dispatcher = mock(jakarta.servlet.RequestDispatcher.class);
    when(request.getRequestDispatcher("/WEB-INF/homepage/manage.jsp")).thenReturn(dispatcher);
    servlet.doGet(request, response);
    verify(request).setAttribute(eq("homepageWidgets"), argThat(value ->
        value instanceof java.util.List<?> widgets && widgets.size() == 14));
    verify(request).setAttribute(eq("newShortcutsWidgetId"), argThat(v -> v.toString().startsWith("shortcuts-")));
    verify(request).setAttribute(eq("newNewsWidgetId"), argThat(v -> v.toString().startsWith("news-")));
    verify(request).setAttribute(eq("newWidgetId"), argThat(v -> v.toString().startsWith("html-")));
    verify(request).setAttribute("homepageLanguage", "fr");
    verify(dispatcher).forward(request, response);
  }

  @Test
  void saveIncludesPlacement() throws Exception {
    when(user.isAccessAdmin()).thenReturn(true);
    when(request.getHeader("X-Aurora-CSRF")).thenReturn("test-token");
    when(request.getParameter("action")).thenReturn("save");
    when(request.getParameter("revision")).thenReturn("");
    when(request.getParameter("title")).thenReturn("Zone");
    when(request.getParameter("html")).thenReturn("<p>Contenu</p>");
    when(request.getParameter("zone")).thenReturn("right");
    when(request.getParameter("order")).thenReturn("24");
    when(request.getParameter("enabled")).thenReturn("false");
    servlet.doPost(request, response);
    verify(response, never()).sendError(anyInt());
    assertEquals(new HomepageContentStore.Layout("right", 24, false),
        store.read(new HomepagePage("skins", "waves"), "welcome", "fr").layout());
  }

  @Test
  void regularUserCannotManageEvenWhenOwnerIsOmitted() throws Exception {
    when(request.getParameter("action")).thenReturn("manage");
    when(request.getParameter("owner")).thenReturn(null);
    servlet.doGet(request, response);
    verify(response).sendError(403);
  }

  @Test
  void eventsAndRssRequireAdminCsrfAndFreshRevision() throws Exception {
    for (String type : java.util.List.of("Events", "Rss")) {
      prepareNewsSave();
      when(request.getParameter("action")).thenReturn("save" + type);
      when(request.getParameter("widget")).thenReturn(type.toLowerCase() + "-test");
      when(request.getParameter("title")).thenReturn("Titre");
      when(request.getParameter("zone")).thenReturn("main");
      when(request.getParameter("order")).thenReturn("2");
      when(request.getParameter("source")).thenReturn(type.equals("Events") ? "almanach12" : "rssAgregator12");
      when(request.getParameter("maxDays")).thenReturn("4");
      when(request.getParameter("rendering")).thenReturn("tabs");
      when(request.getParameter("includeToday")).thenReturn("false");
      when(request.getParameter("personal")).thenReturn("true");
      when(request.getParameter("showMore")).thenReturn("false");
      when(user.isAccessAdmin()).thenReturn(false);
      clearInvocations(response);
      servlet.doPost(request, response);
      verify(response).sendError(403);
      when(user.isAccessAdmin()).thenReturn(true);
      when(request.getHeader("X-Aurora-CSRF")).thenReturn(null);
      clearInvocations(response);
      servlet.doPost(request, response);
      verify(response).sendError(403);
      when(request.getHeader("X-Aurora-CSRF")).thenReturn("test-token");
      clearInvocations(response);
      servlet.doPost(request, response);
      verify(response, never()).sendError(anyInt());
      var saved = store.readWidget(new HomepagePage("skins", "waves"), type.toLowerCase() + "-test", "fr");
      assertEquals(type.toLowerCase(), saved.type());
      if (type.equals("Events")) {
        assertTrue(saved.eventsConfiguration().personal());
        assertFalse(saved.eventsConfiguration().includeToday());
        assertFalse(saved.eventsConfiguration().showMore());
      } else {
        assertEquals("tabs", saved.rssConfiguration().rendering());
      }
      servlet.doPost(request, response);
      verify(response).sendError(409);
    }
  }

  @Test
  void feedRenderingAllowsReadersOnActiveSkinAndSkipsHiddenAndMissingWidgets() throws Exception {
    var helper = (org.silverpeas.looks.aurora.LookAuroraHelper) session.getAttribute("Silverpeas_LookHelper");
    var page = new HomepagePage("skins", "waves");
    var events = new HomepageContentStore.EventsConfiguration("*", 2, true, false, false, true);
    store.saveFeedWidget(page, "events-hidden", "fr", "", new HomepageContentStore.Layout("main", 2, false), "events", events, null, "");
    when(request.getParameter("action")).thenReturn("renderFeed");
    for (String id : java.util.List.of("events-hidden", "missing")) {
      when(request.getParameter("widget")).thenReturn(id);
      servlet.doGet(request, response);
      verify(helper, never()).getConfiguredNextEvents(any());
      verify(helper, never()).getConfiguredRSSFeeds(any());
    }
    verify(response, times(2)).setStatus(204);
    var dispatcher = mock(jakarta.servlet.RequestDispatcher.class);
    when(request.getRequestDispatcher("/WEB-INF/homepage/feedWidget.jsp")).thenReturn(dispatcher);
    var visibleEvents = mock(org.silverpeas.looks.aurora.NextEvents.class);
    when(visibleEvents.getNextEventsDates()).thenReturn(java.util.List.of(mock(org.silverpeas.looks.aurora.NextEventsDate.class)));
    when(helper.getConfiguredNextEvents(any())).thenReturn(visibleEvents);
    var visibleRss = new org.silverpeas.looks.aurora.RSSFeeds(java.util.List.of(),
        java.util.List.of(mock(org.silverpeas.components.rssaggregator.model.RSSItem.class)));
    when(helper.getConfiguredRSSFeeds(any())).thenReturn(visibleRss);
    when(request.getParameter("widget")).thenReturn("events-legacy");
    servlet.doGet(request, response);
    verify(helper).getConfiguredNextEvents(new HomepageContentStore.EventsConfiguration("almanach12", 3, true, false, true, true));
    verify(dispatcher).forward(request, response);
    when(request.getParameter("widget")).thenReturn("rss-legacy");
    servlet.doGet(request, response);
    verify(helper).getConfiguredRSSFeeds(new HomepageContentStore.RssConfiguration("rssAgregator12", "tabs"));
    when(helper.getConfiguredRSSFeeds(any())).thenReturn(null);
    clearInvocations(response, dispatcher);
    servlet.doGet(request, response);
    verify(response).setStatus(204);
    verifyNoInteractions(dispatcher);
    when(request.getParameter("owner")).thenReturn("Aurora");
    servlet.doGet(request, response);
    verify(response).sendError(403);
  }

  @Test
  void communitySettingsRequireAdminCsrfAndFreshRevision() throws Exception {
    for (String type : java.util.List.of("Faq", "Users")) {
      prepareNewsSave();
      when(request.getParameter("action")).thenReturn("save" + type);
      when(request.getParameter("widget")).thenReturn(type.toLowerCase() + "-test");
      when(request.getParameter("title")).thenReturn("Titre");
      when(request.getParameter("zone")).thenReturn("right");
      when(request.getParameter("order")).thenReturn("22");
      when(request.getParameter("source")).thenReturn("questionReply12");
      when(request.getParameter("size")).thenReturn("3");
      when(request.getParameter("selection")).thenReturn("first");
      when(request.getParameter("group")).thenReturn("42");
      when(request.getParameter("avatar")).thenReturn("false");
      when(request.getParameter("fields")).thenReturn("phone title");
      when(user.isAccessAdmin()).thenReturn(false);
      clearInvocations(response);
      servlet.doPost(request, response);
      verify(response).sendError(403);
      when(user.isAccessAdmin()).thenReturn(true);
      when(request.getHeader("X-Aurora-CSRF")).thenReturn(null);
      clearInvocations(response);
      servlet.doPost(request, response);
      verify(response).sendError(403);
      when(request.getHeader("X-Aurora-CSRF")).thenReturn("test-token");
      clearInvocations(response);
      servlet.doPost(request, response);
      verify(response, never()).sendError(anyInt());
      var saved = store.readWidget(new HomepagePage("skins", "waves"), type.toLowerCase() + "-test", "fr");
      assertEquals(type.toLowerCase(), saved.type());
      if (type.equals("Faq")) {
        assertEquals(new HomepageContentStore.FaqConfiguration("questionReply12", 3, "first"), saved.faqConfiguration());
      } else {
        assertEquals(new HomepageContentStore.UsersConfiguration("42", 3, false, "phone title"), saved.usersConfiguration());
      }
      servlet.doPost(request, response);
      verify(response).sendError(409);
      when(request.getParameter("action")).thenReturn("load" + type);
      clearInvocations(response);
      servlet.doGet(request, response);
      verify(response, never()).sendError(anyInt());
      when(user.isAccessAdmin()).thenReturn(false);
      when(request.getParameter("owner")).thenReturn("prima");
      servlet.doGet(request, response);
      verify(response).sendError(403);
      when(request.getParameter("owner")).thenReturn("waves");
    }
  }

  @Test
  void subscriptionsSaveRequiresAdminCsrfAndFreshRevisionAndLoadsTheSavedSettings() throws Exception {
    prepareNewsSave();
    when(request.getParameter("action")).thenReturn("saveSubscriptions");
    when(request.getParameter("widget")).thenReturn("subscriptions-test");
    when(request.getParameter("title")).thenReturn("Mes abonnements");
    when(request.getParameter("zone")).thenReturn("right");
    when(request.getParameter("order")).thenReturn("8");
    when(request.getParameter("size")).thenReturn("6");
    when(request.getParameter("showMore")).thenReturn("false");
    when(user.isAccessAdmin()).thenReturn(false);
    servlet.doPost(request, response);
    verify(response).sendError(403);
    when(user.isAccessAdmin()).thenReturn(true);
    when(request.getHeader("X-Aurora-CSRF")).thenReturn(null);
    clearInvocations(response);
    servlet.doPost(request, response);
    verify(response).sendError(403);
    when(request.getHeader("X-Aurora-CSRF")).thenReturn("test-token");
    clearInvocations(response);
    servlet.doPost(request, response);
    verify(response, never()).sendError(anyInt());
    var saved = store.readWidget(new HomepagePage("skins", "waves"), "subscriptions-test", "fr");
    assertEquals(new HomepageContentStore.SubscriptionsConfiguration(6, false), saved.subscriptionsConfiguration());
    servlet.doPost(request, response);
    verify(response).sendError(409);
    when(request.getParameter("action")).thenReturn("loadSubscriptions");
    clearInvocations(response);
    servlet.doGet(request, response);
    verify(response, never()).sendError(anyInt());
    assertTrue(output.toString().contains("\"size\":6"));
  }


  @Test
  void homeShortcutsSaveRequiresAdminCsrfAndFreshRevisionAndValidatesTheOrderedList() throws Exception {
    prepareNewsSave();
    when(request.getParameter("action")).thenReturn("saveShortcuts");
    when(request.getParameter("widget")).thenReturn("shortcuts-test");
    when(request.getParameter("title")).thenReturn("Mes liens");
    when(request.getParameter("zone")).thenReturn("right");
    when(request.getParameter("order")).thenReturn("8");
    when(request.getParameter("count")).thenReturn("1");
    when(request.getParameter("entry.0.label")).thenReturn("Documents & outils");
    when(request.getParameter("entry.0.url")).thenReturn("/silverpeas/Component/kmelia12");
    when(request.getParameter("entry.0.iconUrl")).thenReturn("/weblib/icon.png");
    when(request.getParameter("entry.0.target")).thenReturn("bottomFrame");
    when(user.isAccessAdmin()).thenReturn(false);
    servlet.doPost(request, response);
    verify(response).sendError(403);
    when(user.isAccessAdmin()).thenReturn(true);
    when(request.getHeader("X-Aurora-CSRF")).thenReturn(null);
    clearInvocations(response);
    servlet.doPost(request, response);
    verify(response).sendError(403);
    when(request.getHeader("X-Aurora-CSRF")).thenReturn("test-token");
    clearInvocations(response);
    servlet.doPost(request, response);
    verify(response, never()).sendError(anyInt());
    var page = new HomepagePage("skins", "waves");
    var saved = store.readWidget(page, "shortcuts-test", "fr");
    assertEquals("bottomFrame", saved.shortcutsConfiguration().entries().getFirst().target());
    assertEquals("right", saved.getZone());
    servlet.doPost(request, response);
    verify(response).sendError(409);
    when(request.getParameter("action")).thenReturn("loadShortcuts");
    clearInvocations(response);
    servlet.doGet(request, response);
    verify(response, never()).sendError(anyInt());
    assertTrue(output.toString().contains("\"entries\":[{\"label\":\"Documents & outils\""));
    when(request.getParameter("action")).thenReturn("saveShortcuts");
    when(request.getParameter("revision")).thenReturn(saved.content().revision());
    when(request.getParameter("entry.0.url")).thenReturn("javascript:alert(1)");
    clearInvocations(response);
    servlet.doPost(request, response);
    verify(response).sendError(400);
    assertEquals(saved.content().revision(), store.read(page, "shortcuts-test", "fr").revision());
    when(request.getParameter("count")).thenReturn("101");
    clearInvocations(response);
    servlet.doPost(request, response);
    verify(response).sendError(400);
  }
  @Test
  void searchSaveRequiresAdminCsrfAndFreshRevisionAndRejectsInvalidTemplates() throws Exception {
    prepareNewsSave();
    when(request.getParameter("action")).thenReturn("saveSearch");
    when(request.getParameter("widget")).thenReturn("search-test");
    when(request.getParameter("title")).thenReturn("Recherche");
    when(request.getParameter("zone")).thenReturn("right");
    when(request.getParameter("order")).thenReturn("30");
    when(request.getParameter("mode")).thenReturn("advanced");
    when(request.getParameter("template")).thenReturn("documents.xml");
    when(request.getParameter("labelsInside")).thenReturn("true");
    when(request.getParameter("period")).thenReturn("true");
    when(request.getParameter("space")).thenReturn("false");
    when(user.isAccessAdmin()).thenReturn(false);
    servlet.doPost(request, response);
    verify(response).sendError(403);
    when(user.isAccessAdmin()).thenReturn(true);
    when(request.getHeader("X-Aurora-CSRF")).thenReturn(null);
    clearInvocations(response);
    servlet.doPost(request, response);
    verify(response).sendError(403);
    when(request.getHeader("X-Aurora-CSRF")).thenReturn("test-token");
    clearInvocations(response);
    servlet.doPost(request, response);
    verify(response, never()).sendError(anyInt());
    var page = new HomepagePage("skins", "waves");
    var saved = store.readWidget(page, "search-test", "fr");
    assertEquals(new HomepageContentStore.SearchConfiguration("advanced", "documents.xml", true, true, false), saved.searchConfiguration());
    servlet.doPost(request, response);
    verify(response).sendError(409);
    when(request.getParameter("action")).thenReturn("loadSearch");
    servlet.doGet(request, response);
    assertTrue(output.toString().contains("\"mode\":\"advanced\""));
    when(request.getParameter("action")).thenReturn("saveSearch");
    when(request.getParameter("template")).thenReturn("../secret.xml");
    clearInvocations(response);
    servlet.doPost(request, response);
    verify(response).sendError(400);
    assertEquals(saved.content().revision(), store.read(page, "search-test", "fr").revision());
  }

  @Test
  void ordinaryReaderCanRenderOnlyVisibleSearchFromTheirActiveSkin() throws Exception {
    var page = new HomepagePage("skins", "waves");
    store.saveSearchWidget(page, "search-test", "fr", "Recherche",
        new HomepageContentStore.Layout("right", 30, true),
        new HomepageContentStore.SearchConfiguration("taxonomy", "", true, false, false), "");
    when(user.isAccessAdmin()).thenReturn(false);
    when(request.getParameter("action")).thenReturn("renderSearch");
    when(request.getParameter("widget")).thenReturn("search-test");
    var dispatcher = mock(jakarta.servlet.RequestDispatcher.class);
    when(request.getRequestDispatcher("/WEB-INF/homepage/searchWidget.jsp")).thenReturn(dispatcher);
    servlet.doGet(request, response);
    verify(dispatcher).forward(request, response);
    verify(response, never()).sendError(anyInt());
    when(request.getParameter("owner")).thenReturn("other");
    servlet.doGet(request, response);
    verify(response).sendError(403);
    when(request.getParameter("owner")).thenReturn("waves");
    when(request.getParameter("widget")).thenReturn("search-missing");
    servlet.doGet(request, response);
    verify(response).setStatus(204);
  }

  @Test
  void weatherSaveRequiresAdminCsrfAndRevisionAndIgnoresSubmittedCities() throws Exception {
    prepareNewsSave();
    when(request.getParameter("action")).thenReturn("saveWeather");
    when(request.getParameter("widget")).thenReturn("weather-test");
    when(request.getParameter("title")).thenReturn("Mes favoris");
    when(request.getParameter("zone")).thenReturn("right");
    when(request.getParameter("order")).thenReturn("40");
    when(request.getParameter("cities")).thenReturn("2988507|Paris");
    when(user.isAccessAdmin()).thenReturn(false);
    servlet.doPost(request, response);
    verify(response).sendError(403);
    when(user.isAccessAdmin()).thenReturn(true);
    when(request.getHeader("X-Aurora-CSRF")).thenReturn(null);
    clearInvocations(response);
    servlet.doPost(request, response);
    verify(response).sendError(403);
    when(request.getHeader("X-Aurora-CSRF")).thenReturn("test-token");
    clearInvocations(response);
    servlet.doPost(request, response);
    verify(response, never()).sendError(anyInt());
    var page = new HomepagePage("skins", "waves");
    var saved = store.readWidget(page, "weather-test", "fr");
    assertEquals("weather", saved.type());
    servlet.doPost(request, response);
    verify(response).sendError(409);
    when(request.getParameter("action")).thenReturn("loadWeather");
    servlet.doGet(request, response);
    when(request.getParameter("action")).thenReturn("saveWeather");
    when(request.getParameter("zone")).thenReturn("invalid");
    clearInvocations(response);
    servlet.doPost(request, response);
    verify(response).sendError(400);
    assertEquals(saved.content().revision(), store.read(page, "weather-test", "fr").revision());
  }

  @Test
  void bookmarksSaveRequiresAdminCsrfAndRevisionAndReturnsEmptyStateOptions() throws Exception {
    prepareNewsSave();
    when(request.getParameter("action")).thenReturn("saveBookmarks");
    when(request.getParameter("widget")).thenReturn("bookmarks-test");
    when(request.getParameter("title")).thenReturn("Mes favoris");
    when(request.getParameter("zone")).thenReturn("right");
    when(request.getParameter("order")).thenReturn("40");
    when(request.getParameter("showWhenEmpty")).thenReturn("true");
    when(request.getParameter("emptyFragment")).thenReturn("/silverpeas/empty.html");
    when(user.isAccessAdmin()).thenReturn(false);
    servlet.doPost(request, response);
    verify(response).sendError(403);
    when(user.isAccessAdmin()).thenReturn(true);
    when(request.getHeader("X-Aurora-CSRF")).thenReturn(null);
    clearInvocations(response);
    servlet.doPost(request, response);
    verify(response).sendError(403);
    when(request.getHeader("X-Aurora-CSRF")).thenReturn("test-token");
    clearInvocations(response);
    servlet.doPost(request, response);
    verify(response, never()).sendError(anyInt());
    var page = new HomepagePage("skins", "waves");
    var saved = store.readWidget(page, "bookmarks-test", "fr");
    assertEquals(new HomepageContentStore.BookmarksConfiguration(true, "/silverpeas/empty.html"), saved.bookmarksConfiguration());
    servlet.doPost(request, response);
    verify(response).sendError(409);
    when(request.getParameter("action")).thenReturn("loadBookmarks");
    servlet.doGet(request, response);
    assertTrue(output.toString().contains("\"showWhenEmpty\":true"));
    assertTrue(output.toString().contains("\"emptyFragment\":\"/silverpeas/empty.html\""));
    when(request.getParameter("action")).thenReturn("saveBookmarks");
    when(request.getParameter("emptyFragment")).thenReturn("javascript:alert(1)");
    clearInvocations(response);
    servlet.doPost(request, response);
    verify(response).sendError(400);
    assertEquals(saved.content().revision(), store.read(page, "bookmarks-test", "fr").revision());
  }

  @Test
  void bookmarksRenderUsesTheReadersPersonalLinksAndRespectsVisibilityAndSkin() throws Exception {
    var page = new HomepagePage("skins", "waves");
    var config = new HomepageContentStore.BookmarksConfiguration(false, "");
    store.saveBookmarksWidget(page, "bookmarks-test", "fr", "Mes favoris",
        new HomepageContentStore.Layout("right", 40, true), config, "");
    when(user.isAccessAdmin()).thenReturn(false);
    when(request.getParameter("action")).thenReturn("renderBookmarks");
    when(request.getParameter("widget")).thenReturn("bookmarks-test");
    var helper = (org.silverpeas.looks.aurora.LookAuroraHelper) session.getAttribute("Silverpeas_LookHelper");
    var links = java.util.List.<org.silverpeas.core.mylinks.model.LinkDetail>of();
    when(helper.getBookmarks()).thenReturn(links);
    var dispatcher = mock(jakarta.servlet.RequestDispatcher.class);
    when(request.getRequestDispatcher("/WEB-INF/homepage/bookmarksWidget.jsp")).thenReturn(dispatcher);
    servlet.doGet(request, response);
    verify(dispatcher).forward(request, response);
    verify(request).setAttribute("bookmarks", links);
    verify(request).setAttribute("showBookmarks", false);
    verify(response, never()).sendError(anyInt());
    var revision = store.read(page, "bookmarks-test", "fr").revision();
    store.saveBookmarksWidget(page, "bookmarks-test", "fr", "Mes favoris",
        new HomepageContentStore.Layout("right", 40, true), new HomepageContentStore.BookmarksConfiguration(true, ""), revision);
    clearInvocations(request);
    servlet.doGet(request, response);
    verify(request).setAttribute("showBookmarks", true);
    when(user.isAnonymous()).thenReturn(true);
    clearInvocations(request);
    servlet.doGet(request, response);
    verify(request).setAttribute("showBookmarks", false);
    var personalLink = new org.silverpeas.core.mylinks.model.LinkDetail(
        "My private link", "Description", "/Component/kmelia12", true, false);
    when(helper.getBookmarks()).thenReturn(java.util.List.of(personalLink));
    clearInvocations(request);
    servlet.doGet(request, response);
    verify(request).setAttribute("showBookmarks", true);
    verify(request).setAttribute(eq("bookmarksJson"), argThat(json ->
        json.toString().contains("My private link")));
    assertFalse(Files.readString(root.resolve("skins/waves/page.properties")).contains("My private link"));
    when(request.getParameter("owner")).thenReturn("other");
    clearInvocations(helper);
    servlet.doGet(request, response);
    verify(response).sendError(403);
    verify(helper, never()).getBookmarks();
    when(request.getParameter("owner")).thenReturn("waves");
    when(request.getParameter("widget")).thenReturn("bookmarks-missing");
    servlet.doGet(request, response);
    verify(response).setStatus(204);
    verify(helper, never()).getBookmarks();
    when(request.getParameter("widget")).thenReturn("bookmarks-test");
    revision = store.read(page, "bookmarks-test", "fr").revision();
    store.saveBookmarksWidget(page, "bookmarks-test", "fr", "Mes favoris",
        new HomepageContentStore.Layout("right", 40, false), config, revision);
    servlet.doGet(request, response);
    verify(helper, never()).getBookmarks();

  }

}
