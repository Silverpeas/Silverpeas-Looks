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
        value instanceof java.util.List<?> widgets && widgets.size() == 5));
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

}
