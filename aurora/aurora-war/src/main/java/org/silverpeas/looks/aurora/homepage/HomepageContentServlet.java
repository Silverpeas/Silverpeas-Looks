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

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.util.JSONCodec;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;
import java.net.URLEncoder;
import org.silverpeas.kernel.logging.SilverLogger;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.UUID;

/** Management and protected delivery of homepage HTML widgets. */
@WebServlet("/AuroraHomepageContent")
@MultipartConfig(maxFileSize = 5242880, maxRequestSize = 6291456)
public class HomepageContentServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private static final String TOKEN = "AuroraHomepageContent.csrf";
  private transient HomepageContentStore store;

  public HomepageContentServlet() {
    // Required by the servlet container.
  }

  HomepageContentServlet(HomepageContentStore store) {
    this.store = store;
  }

  @Override
  public void init() throws ServletException {
    String dataHome = System.getProperty("SILVERPEAS_DATA_HOME");
    if (dataHome == null || dataHome.isBlank()) {
      throw new ServletException("SILVERPEAS_DATA_HOME must be configured");
    }
    store = HomepageContentStore.forInstallation();
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    handle(request, response, false);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    handle(request, response, true);
  }

  private void handle(HttpServletRequest request, HttpServletResponse response, boolean write)
      throws IOException {
    response.setHeader("Cache-Control", "no-store");
    response.setHeader("X-Content-Type-Options", "nosniff");
    request.setCharacterEncoding(StandardCharsets.UTF_8.name());
    try {
      HttpSession session = request.getSession(false);
      MainSessionController controller = session == null ? null : MainSessionController.getInstance(session);
      if (controller == null) {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        return;
      }
      String action = request.getParameter("action");
      String activeSkin = "";
      if ("skins".equals(request.getParameter("kind"))) {
        var factory = (GraphicElementFactory) session.getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);
        if (factory == null) {
          response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
          return;
        }
        activeSkin = skinId(factory);
        if (("editor".equals(action) || "manage".equals(action) || "newsEditor".equals(action) || "publicationsEditor".equals(action) || "ephemerisEditor".equals(action) || "eventsEditor".equals(action) || "rssEditor".equals(action) || "faqEditor".equals(action) || "usersEditor".equals(action) || "subscriptionsEditor".equals(action) || "weatherEditor".equals(action) || "bookmarksEditor".equals(action) || "searchEditor".equals(action) || "shortcutsEditor".equals(action)) && !activeSkin.equals(request.getParameter("owner"))) {
          if (!controller.getCurrentUserDetail().isAccessAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
          }
          response.sendRedirect(request.getContextPath() + "/AuroraHomepageContent?action=" + action + "&kind=skins&owner="
              + activeSkin + "&widget=" + encode(request.getParameter("widget"))
              + "&language=" + encode(request.getParameter("language"))
              + ("newsEditor".equals(action) ? "&index=" + encode(request.getParameter("index")) : ""));
          return;
        }
      }
      HomepagePage page = new HomepagePage(request.getParameter("kind"), request.getParameter("owner"));
      boolean edit = write || !"asset".equals(action) && !"view".equals(action) && !"renderFeed".equals(action) && !"renderSearch".equals(action) && !"renderBookmarks".equals(action);
      if (!isAllowed(controller, page, edit, activeSkin)) {
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
        return;
      }
      if ("spaces".equals(page.kind()) && !spaceAction(action)) {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }
      request.setAttribute("homepageKind", page.kind());
      request.setAttribute("homepageOwner", page.owner());
      String csrf = csrfToken(session);
      if (write && !validToken(csrf, request.getHeader("X-Aurora-CSRF"))) {
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
        return;
      }
      String widget = request.getParameter("widget");
      String language = request.getParameter("language");
      if (language == null || language.isBlank()) {
        language = controller.getFavoriteLanguage();
      }
      if (!"asset".equals(action) &&
          (language == null || !language.matches("[a-z]{2,3}(-[A-Za-z]{2,8})?"))) {
        throw new IllegalArgumentException("Invalid language");
      }
      request.setAttribute("homepageLanguage", language);
      if (write) {
        write(request, response, page, widget, language, action);
      } else {
        read(request, response, page, widget, language, action, csrf);
      }
    } catch (IllegalArgumentException e) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    } catch (ConcurrentModificationException e) {
      response.sendError(HttpServletResponse.SC_CONFLICT);
    } catch (NoSuchFileException e) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
    } catch (IllegalStateException e) {
      response.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
    } catch (ServletException e) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    } catch (IOException e) {
      SilverLogger.getLogger(this).error(e);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  static boolean spaceAction(String action) {
    return java.util.Set.of("manage", "editor", "load", "save", "upload", "asset", "view",
        "deleteWidget", "newsEditor", "loadNews", "saveNews", "publicationsEditor",
        "loadPublications", "savePublications", "eventsEditor", "loadEvents", "saveEvents",
        "shortcutsEditor", "loadShortcuts", "saveShortcuts", "searchEditor", "loadSearch",
        "saveSearch", "renderFeed", "renderSearch").contains(action == null ? "" : action);
  }

  boolean isAllowed(MainSessionController controller, HomepagePage page, boolean edit, String activeSkin) {
    var user = controller.getCurrentUserDetail();
    if (user == null || !user.isActivatedState() && !user.isAnonymous()) {
      return false;
    }
    if (user.isAccessAdmin()) {
      return true;
    }
    if ("skins".equals(page.kind())) {
      return !edit && page.owner().equals(activeSkin);
    }
    var organization = OrganizationController.get();
    if (edit) {
      return Arrays.stream(organization.getUserManageableSpaceIds(user.getId()))
          .map(id -> new HomepagePage("spaces", id).owner()).anyMatch(page.owner()::equals);
    }
    return organization.isSpaceAvailable(page.owner(), user.getId());
  }

  public static String skinId(GraphicElementFactory factory) {
    String bundle = factory.getFavoriteLookSettings().getBaseBundleName();
    String id = bundle.substring(bundle.lastIndexOf('.') + 1);
    HomepagePage.validateIdentifier(id);
    return id;
  }

  private String encode(String value) {
    return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
  }

  private void write(HttpServletRequest request, HttpServletResponse response, HomepagePage page,
      String widget, String language, String action) throws IOException, ServletException {
    if ("deleteWidget".equals(action)) {
      store.deleteWidget(page, widget, language, request.getParameter("revision"));
      json(response, "{}");
    } else if ("saveNews".equals(action)) {
      if (widget != null && !widget.isBlank()) {
        store.saveNewsWidget(page, widget, language, request.getParameter("title"),
            new HomepageContentStore.Layout(request.getParameter("zone"),
                Integer.parseInt(request.getParameter("order")),
                Boolean.parseBoolean(request.getParameter("enabled"))),
            new HomepageContentStore.NewsConfiguration(
                "spaces".equals(page.kind()) ? "*" : request.getParameter("source"),
                Integer.parseInt(request.getParameter("size")),
                Boolean.parseBoolean(request.getParameter("importantOnly")),
                request.getParameter("rendering"), Boolean.parseBoolean(request.getParameter("enabled")),
                request.getParameter("revision"), option(request, "showMoreNews"),
                option(request, "showAllNews")), 0);
        newsWidgetResponse(request, response, page, widget, language, "");
      } else {
        throw new IllegalArgumentException("Missing news widget");
      }
    } else if ("saveWeather".equals(action)) {
      store.saveWeatherWidget(page, widget, language, request.getParameter("title"),
          new HomepageContentStore.Layout(request.getParameter("zone"),
              Integer.parseInt(request.getParameter("order")), option(request, "enabled")),
          request.getParameter("revision"));
      weatherResponse(request, response, page, widget, language, "");
    } else if ("saveBookmarks".equals(action)) {
      store.saveBookmarksWidget(page, widget, language, request.getParameter("title"),
          new HomepageContentStore.Layout(request.getParameter("zone"),
              Integer.parseInt(request.getParameter("order")), option(request, "enabled")),
          new HomepageContentStore.BookmarksConfiguration(option(request, "showWhenEmpty"),
              request.getParameter("emptyFragment")), request.getParameter("revision"));
      bookmarksResponse(request, response, page, widget, language, "");
    } else if ("saveSearch".equals(action)) {
      store.saveSearchWidget(page, widget, language, request.getParameter("title"),
          new HomepageContentStore.Layout(request.getParameter("zone"),
              Integer.parseInt(request.getParameter("order")), option(request, "enabled")),
          new HomepageContentStore.SearchConfiguration(request.getParameter("mode"),
              request.getParameter("template"), option(request, "labelsInside"),
              option(request, "period"), option(request, "space")), request.getParameter("revision"));
      searchResponse(request, response, page, widget, language, "");
    } else if ("saveShortcuts".equals(action)) {
      int count = Integer.parseInt(request.getParameter("count"));
      if (count < 0 || count > 100) {
        throw new IllegalArgumentException("Invalid shortcut count");
      }
      var entries = java.util.stream.IntStream.range(0, count).mapToObj(index -> {
        String prefix = "entry." + index;
        return new HomepageContentStore.HomeShortcut(request.getParameter(prefix + ".label"),
            request.getParameter(prefix + ".url"), request.getParameter(prefix + ".iconUrl"),
            request.getParameter(prefix + ".target"));
      }).toList();
      store.saveShortcutsWidget(page, widget, language, request.getParameter("title"),
          new HomepageContentStore.Layout(request.getParameter("zone"),
              Integer.parseInt(request.getParameter("order")),
              Boolean.parseBoolean(request.getParameter("enabled"))),
          new HomepageContentStore.ShortcutsConfiguration(entries), request.getParameter("revision"));
      shortcutsResponse(request, response, page, widget, language, "");
    } else if ("saveSubscriptions".equals(action)) {
      store.saveSubscriptionsWidget(page, widget, language, request.getParameter("title"),
          new HomepageContentStore.Layout(request.getParameter("zone"),
              Integer.parseInt(request.getParameter("order")),
              Boolean.parseBoolean(request.getParameter("enabled"))),
          new HomepageContentStore.SubscriptionsConfiguration(Integer.parseInt(request.getParameter("size")),
              option(request, "showMore")), request.getParameter("revision"));
      subscriptionsResponse(request, response, page, widget, language, "");
    } else if ("saveFaq".equals(action) || "saveUsers".equals(action)) {
      boolean faq = "saveFaq".equals(action);
      store.saveCommunityWidget(page, widget, language, request.getParameter("title"),
          new HomepageContentStore.Layout(request.getParameter("zone"),
              Integer.parseInt(request.getParameter("order")),
              Boolean.parseBoolean(request.getParameter("enabled"))), faq ? "faq" : "users",
          faq ? new HomepageContentStore.FaqConfiguration(request.getParameter("source"),
              Integer.parseInt(request.getParameter("size")), request.getParameter("selection")) : null,
          faq ? null : new HomepageContentStore.UsersConfiguration(request.getParameter("group"),
              Integer.parseInt(request.getParameter("size")),
              Boolean.parseBoolean(request.getParameter("avatar")), request.getParameter("fields")),
          request.getParameter("revision"));
      communityResponse(request, response, page, widget, language, "", faq ? "faq" : "users");
    } else if ("saveEvents".equals(action) || "saveRss".equals(action)) {
      boolean events = "saveEvents".equals(action);
      store.saveFeedWidget(page, widget, language, request.getParameter("title"),
          new HomepageContentStore.Layout(request.getParameter("zone"),
              Integer.parseInt(request.getParameter("order")),
              Boolean.parseBoolean(request.getParameter("enabled"))), events ? "events" : "rss",
          events ? new HomepageContentStore.EventsConfiguration(
              "spaces".equals(page.kind()) ? "" : request.getParameter("source"),
              Integer.parseInt(request.getParameter("maxDays")), option(request, "includeToday"),
              !"spaces".equals(page.kind()) && Boolean.parseBoolean(request.getParameter("personal")),
              Boolean.parseBoolean(request.getParameter("importantOnly")), option(request, "showMore")) : null,
          events ? null : new HomepageContentStore.RssConfiguration(request.getParameter("source"),
              request.getParameter("rendering")), request.getParameter("revision"));
      feedResponse(request, response, page, widget, language, "", events ? "events" : "rss");
    } else if ("saveEphemeris".equals(action)) {
      store.saveEphemerisWidget(page, widget, language, request.getParameter("title"),
          new HomepageContentStore.Layout(request.getParameter("zone"),
              Integer.parseInt(request.getParameter("order")),
              Boolean.parseBoolean(request.getParameter("enabled"))), request.getParameter("revision"));
      ephemerisResponse(request, response, page, widget, language, "");
    } else if ("savePublications".equals(action)) {
      store.savePublicationsWidget(page, widget, language, request.getParameter("title"),
          new HomepageContentStore.Layout(request.getParameter("zone"),
              Integer.parseInt(request.getParameter("order")),
              Boolean.parseBoolean(request.getParameter("enabled"))),
          new HomepageContentStore.PublicationsConfiguration(
              "spaces".equals(page.kind()) ? page.owner() : request.getParameter("spaceId"),
              Integer.parseInt(request.getParameter("size")), option(request, "showMore")),
          request.getParameter("revision"));
      publicationsResponse(request, response, page, widget, language, "");
    } else if ("upload".equals(action)) {
      String name = UUID.randomUUID() + ".png";
      store.saveAsset(page, widget, name, image(request));
      String url = request.getContextPath() + "/AuroraHomepageContent?kind=" + page.kind()
          + "&owner=" + page.owner() + "&widget=" + widget + "&action=asset&name=" + name;
      json(response, JSONCodec.encodeObject(o -> o.put("url", url)));
    } else if ("save".equals(action)) {
      String html = request.getParameter("html");
      String title = request.getParameter("title");
      if (html == null || html.length() > 500000 || title == null || title.length() > 200) {
        throw new IllegalArgumentException("Content too large or missing");
      }
      String sanitized = HomepageContentSanitizer.sanitize(html);
      var previous = store.read(page, widget, language);
      var layout = request.getParameter("zone") == null ? previous.layout()
          : new HomepageContentStore.Layout(request.getParameter("zone"),
              Integer.parseInt(request.getParameter("order")),
              Boolean.parseBoolean(request.getParameter("enabled")));
      store.saveConfigured(page, widget, language, title, sanitized,
          request.getParameter("revision"), layout);
      contentResponse(response, store.read(page, widget, language), "");
    } else {
      throw new IllegalArgumentException("Unknown action");
    }
  }

  private void read(HttpServletRequest request, HttpServletResponse response, HomepagePage page,
      String widget, String language, String action, String csrf) throws IOException, ServletException {
    if ("sources".equals(action)) {
      var organization = OrganizationController.get();
      String userId = MainSessionController.getInstance(request.getSession(false)).getCurrentUserDetail().getId();
      String type = request.getParameter("sourceType");
      if (!Arrays.asList("quickinfo", "rssAgregator", "spaces").contains(type)) {
        throw new IllegalArgumentException("Unknown source type");
      }
      String[] ids = "spaces".equals(type) ? organization.getAllSpaceIds(userId)
          : organization.getComponentIdsForUser(userId, type);
      json(response, JSONCodec.encodeObject(o -> o.putJSONArray("sources", array -> {
        Arrays.stream(ids).forEach(id -> {
          if ("spaces".equals(type)) {
            var space = organization.getSpaceInstLightById(id);
            if (space != null) {
              array.addJSONObject(item -> item.put("id", id).put("label", space.getName()));
            }
          } else {
            var app = organization.getComponentInstLight(id);
            if (app != null && !app.isHidden()) {
              var space = organization.getSpaceInstLightById(app.getSpaceId());
              String label = app.getLabel() + (space == null ? "" : " — " + space.getName());
              array.addJSONObject(item -> item.put("id", id).put("label", label));
            }
          }
        });
        return array;
      })));
    } else if ("renderBookmarks".equals(action)) {
      var helper = (org.silverpeas.looks.aurora.LookAuroraHelper)
          request.getSession(false).getAttribute("Silverpeas_LookHelper");
      var model = HomepageWidgets.all(store, page, language, helper).stream()
          .filter(candidate -> candidate.id().equals(widget) && "bookmarks".equals(candidate.type()) &&
              candidate.isEnabled()).findFirst().orElse(null);
      if (model == null) {
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        return;
      }
      var bookmarks = helper.getBookmarks();
      request.setAttribute("bookmarksWidget", model);
      request.setAttribute("bookmarks", bookmarks);
      request.setAttribute("bookmarksJson", JSONCodec.encode(bookmarks.stream()
          .map(link -> org.silverpeas.core.webapi.mylinks.MyLinkEntity.fromLinkDetail(link, null)).toList()));
      request.setAttribute("showBookmarks", !bookmarks.isEmpty() ||
          model.bookmarksConfiguration().showWhenEmpty() &&
              !MainSessionController.getInstance(request.getSession(false)).getCurrentUserDetail().isAnonymous());
      request.getRequestDispatcher("/WEB-INF/homepage/bookmarksWidget.jsp").forward(request, response);
    } else if ("renderSearch".equals(action)) {
      var helper = (org.silverpeas.looks.aurora.LookAuroraHelper)
          request.getSession(false).getAttribute("Silverpeas_LookHelper");
      var model = HomepageWidgets.all(store, page, language, helper).stream()
          .filter(candidate -> candidate.id().equals(widget) && "search".equals(candidate.type()) &&
              candidate.isEnabled()).findFirst().orElse(null);
      if (model == null) {
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        return;
      }
      request.setAttribute("searchWidget", model);
      request.setAttribute("searchSpaceId", "spaces".equals(page.kind()) ? page.owner() : "");
      if ("advanced".equals(model.searchConfiguration().mode())) {
        request.setAttribute("searchForm", helper.getConfiguredSearchForm(model.searchConfiguration().template()));
      }
      request.getRequestDispatcher("/WEB-INF/homepage/searchWidget.jsp").forward(request, response);
    } else if ("renderFeed".equals(action)) {
      var helper = (org.silverpeas.looks.aurora.LookAuroraHelper)
          request.getSession(false).getAttribute("Silverpeas_LookHelper");
      var model = HomepageWidgets.all(store, page, language, helper).stream()
          .filter(candidate -> candidate.id().equals(widget)).findFirst().orElse(null);
      if (model == null || !model.isEnabled() ||
          !java.util.List.of("events", "rss").contains(model.type())) {
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        return;
      }
      request.setAttribute("feedWidget", model);
      if ("events".equals(model.type())) {
        var events = HomepageWidgets.events(page, model.eventsConfiguration(), helper);
        if (events == null || events.getNextEventsDates().isEmpty()) {
          response.setStatus(HttpServletResponse.SC_NO_CONTENT);
          return;
        }
        request.setAttribute("feedEvents", events);
      } else {
        var feeds = helper.getConfiguredRSSFeeds(model.rssConfiguration());
        if (feeds == null || feeds.getAggregatedItems().isEmpty()) {
          response.setStatus(HttpServletResponse.SC_NO_CONTENT);
          return;
        }
        request.setAttribute("feedRss", feeds);
      }
      request.getRequestDispatcher("/WEB-INF/homepage/feedWidget.jsp").forward(request, response);
    } else if ("loadWeather".equals(action)) {
      weatherResponse(request, response, page, widget, language, csrf);
    } else if ("weatherEditor".equals(action)) {
      request.getRequestDispatcher("/WEB-INF/homepage/weatherEditor.jsp").forward(request, response);
    } else if ("loadBookmarks".equals(action)) {
      bookmarksResponse(request, response, page, widget, language, csrf);
    } else if ("bookmarksEditor".equals(action)) {
      request.getRequestDispatcher("/WEB-INF/homepage/bookmarksEditor.jsp").forward(request, response);
    } else if ("loadSearch".equals(action)) {
      searchResponse(request, response, page, widget, language, csrf);
    } else if ("searchEditor".equals(action)) {
      request.getRequestDispatcher("/WEB-INF/homepage/searchEditor.jsp").forward(request, response);
    } else if ("loadShortcuts".equals(action)) {
      shortcutsResponse(request, response, page, widget, language, csrf);
    } else if ("shortcutsEditor".equals(action)) {
      request.getRequestDispatcher("/WEB-INF/homepage/shortcutsEditor.jsp").forward(request, response);
    } else if ("loadSubscriptions".equals(action)) {
      subscriptionsResponse(request, response, page, widget, language, csrf);
    } else if ("subscriptionsEditor".equals(action)) {
      request.getRequestDispatcher("/WEB-INF/homepage/subscriptionsEditor.jsp").forward(request, response);
    } else if ("loadFaq".equals(action) || "loadUsers".equals(action)) {
      communityResponse(request, response, page, widget, language, csrf,
          "loadFaq".equals(action) ? "faq" : "users");
    } else if ("faqEditor".equals(action) || "usersEditor".equals(action)) {
      request.getRequestDispatcher("/WEB-INF/homepage/" + action + ".jsp").forward(request, response);
    } else if ("loadEvents".equals(action) || "loadRss".equals(action)) {
      feedResponse(request, response, page, widget, language, csrf,
          "loadEvents".equals(action) ? "events" : "rss");
    } else if ("eventsEditor".equals(action) || "rssEditor".equals(action)) {
      request.getRequestDispatcher("/WEB-INF/homepage/" +
          ("eventsEditor".equals(action) ? "eventsEditor.jsp" : "rssEditor.jsp")).forward(request, response);
    } else if ("loadEphemeris".equals(action)) {
      ephemerisResponse(request, response, page, widget, language, csrf);
    } else if ("ephemerisEditor".equals(action)) {
      request.getRequestDispatcher("/WEB-INF/homepage/ephemerisEditor.jsp").forward(request, response);
    } else if ("loadPublications".equals(action)) {
      publicationsResponse(request, response, page, widget, language, csrf);
    } else if ("publicationsEditor".equals(action)) {
      request.getRequestDispatcher("/WEB-INF/homepage/publicationsEditor.jsp").forward(request, response);
    } else if ("loadNews".equals(action) || "newsEditor".equals(action)) {
      if (widget == null || widget.isBlank()) {
        throw new IllegalArgumentException("Missing news widget");
      }
      if ("loadNews".equals(action)) {
        newsWidgetResponse(request, response, page, widget, language, csrf);
      } else {
        request.getRequestDispatcher("/WEB-INF/homepage/newsEditor.jsp").forward(request, response);
      }
    } else if ("asset".equals(action)) {
      byte[] bytes = store.readAsset(page, widget, request.getParameter("name"));
      response.setContentType("image/png");
      response.setContentLength(bytes.length);
      response.getOutputStream().write(bytes);
    } else if ("view".equals(action)) {
      response.setContentType("text/html;charset=UTF-8");
      response.setHeader("Content-Security-Policy", "sandbox allow-same-origin; default-src 'none'; img-src 'self'; style-src 'unsafe-inline'; frame-ancestors 'self'");
      response.getWriter().write(store.read(page, widget, language).html());
    } else if ("load".equals(action)) {
      contentResponse(response, store.read(page, widget, language), csrf);
    } else if ("manage".equals(action)) {
      request.setAttribute("homepageCsrf", csrf);
      request.setAttribute("homepageWidgets", HomepageWidgets.all(store, page, language,
          (org.silverpeas.looks.aurora.LookAuroraHelper)
              request.getSession(false).getAttribute("Silverpeas_LookHelper")));
      request.setAttribute("newWidgetId", "html-" + UUID.randomUUID());
      request.setAttribute("newNewsWidgetId", "news-" + UUID.randomUUID());
      request.setAttribute("newPublicationsWidgetId", "publications-" + UUID.randomUUID());
      request.setAttribute("newEventsWidgetId", "events-" + UUID.randomUUID());
      request.setAttribute("newWeatherWidgetId", "weather-" + UUID.randomUUID());
      request.setAttribute("newBookmarksWidgetId", "bookmarks-" + UUID.randomUUID());
      request.setAttribute("newSearchWidgetId", "search-" + UUID.randomUUID());
      request.setAttribute("newShortcutsWidgetId", "shortcuts-" + UUID.randomUUID());
      request.setAttribute("newSubscriptionsWidgetId", "subscriptions-" + UUID.randomUUID());
      request.setAttribute("newFaqWidgetId", "faq-" + UUID.randomUUID());
      request.setAttribute("newUsersWidgetId", "users-" + UUID.randomUUID());
      request.setAttribute("newRssWidgetId", "rss-" + UUID.randomUUID());
      request.setAttribute("newEphemerisWidgetId", "ephemeris-" + UUID.randomUUID());
      request.getRequestDispatcher("/WEB-INF/homepage/manage.jsp").forward(request, response);
    } else if ("editor".equals(action)) {
      request.getRequestDispatcher("/WEB-INF/homepage/contentEditor.jsp").forward(request, response);
    } else {
      throw new IllegalArgumentException("Unknown action");
    }
  }

  private void weatherResponse(HttpServletRequest request, HttpServletResponse response,
      HomepagePage page, String widget, String language, String csrf) throws IOException {
    var model = HomepageWidgets.weatherWidget(store, page, widget, language,
        (org.silverpeas.looks.aurora.LookAuroraHelper)
            request.getSession(false).getAttribute("Silverpeas_LookHelper"));
    var content = model.content();
    json(response, JSONCodec.encodeObject(o -> o
        .put("configured", !content.revision().isEmpty()).put("title", content.title())
        .put("zone", content.layout().zone()).put("order", content.layout().order())
        .put("enabled", content.layout().enabled()).put("revision", content.revision()).put("csrf", csrf)));
  }

  private void bookmarksResponse(HttpServletRequest request, HttpServletResponse response,
      HomepagePage page, String widget, String language, String csrf) throws IOException {
    var model = HomepageWidgets.bookmarksWidget(store, page, widget, language,
        (org.silverpeas.looks.aurora.LookAuroraHelper)
            request.getSession(false).getAttribute("Silverpeas_LookHelper"));
    var content = model.content();
    json(response, JSONCodec.encodeObject(o -> o
        .put("configured", !content.revision().isEmpty()).put("title", content.title())
        .put("zone", content.layout().zone()).put("order", content.layout().order())
        .put("enabled", content.layout().enabled()).put("revision", content.revision()).put("csrf", csrf)
        .put("showWhenEmpty", model.bookmarksConfiguration().showWhenEmpty())
        .put("emptyFragment", model.bookmarksConfiguration().emptyFragment())));
  }

  private void searchResponse(HttpServletRequest request, HttpServletResponse response,
      HomepagePage page, String widget, String language, String csrf) throws IOException {
    var model = HomepageWidgets.searchWidget(store, page, widget, language,
        (org.silverpeas.looks.aurora.LookAuroraHelper)
            request.getSession(false).getAttribute("Silverpeas_LookHelper"));
    var content = model.content();
    var configuration = model.searchConfiguration();
    json(response, JSONCodec.encodeObject(o -> o
        .put("configured", !content.revision().isEmpty()).put("title", content.title())
        .put("zone", content.layout().zone()).put("order", content.layout().order())
        .put("enabled", content.layout().enabled()).put("revision", content.revision()).put("csrf", csrf)
        .put("mode", configuration.mode()).put("template", configuration.template())
        .put("labelsInside", configuration.labelsInside()).put("period", configuration.period())
        .put("space", configuration.space())));
  }

  private void shortcutsResponse(HttpServletRequest request, HttpServletResponse response,
      HomepagePage page, String widget, String language, String csrf) throws IOException {
    var model = HomepageWidgets.shortcutsWidget(store, page, widget, language,
        (org.silverpeas.looks.aurora.LookAuroraHelper)
            request.getSession(false).getAttribute("Silverpeas_LookHelper"));
    var content = model.content();
    json(response, JSONCodec.encodeObject(o -> o
        .put("configured", !content.revision().isEmpty()).put("title", content.title())
        .put("zone", content.layout().zone()).put("order", content.layout().order())
        .put("enabled", content.layout().enabled()).put("revision", content.revision()).put("csrf", csrf)
        .putJSONArray("entries", array -> {
          model.shortcutsConfiguration().entries().forEach(entry -> array.addJSONObject(link -> link
              .put("label", entry.label()).put("url", entry.url())
              .put("iconUrl", entry.iconUrl()).put("target", entry.target())));
          return array;
        })));
  }

  private void subscriptionsResponse(HttpServletRequest request, HttpServletResponse response,
      HomepagePage page, String widget, String language, String csrf) throws IOException {
    var model = HomepageWidgets.subscriptionsWidget(store, page, widget, language,
        (org.silverpeas.looks.aurora.LookAuroraHelper)
            request.getSession(false).getAttribute("Silverpeas_LookHelper"));
    var content = model.content();
    json(response, JSONCodec.encodeObject(o -> o
        .put("configured", !content.revision().isEmpty()).put("title", content.title())
        .put("zone", content.layout().zone()).put("order", content.layout().order())
        .put("enabled", content.layout().enabled()).put("revision", content.revision()).put("csrf", csrf)
        .put("size", model.subscriptionsConfiguration().size())
        .put("showMore", model.subscriptionsConfiguration().showMore())));
  }

  private void communityResponse(HttpServletRequest request, HttpServletResponse response,
      HomepagePage page, String widget, String language, String csrf, String type) throws IOException {
    var model = HomepageWidgets.communityWidget(store, page, widget, language,
        (org.silverpeas.looks.aurora.LookAuroraHelper)
            request.getSession(false).getAttribute("Silverpeas_LookHelper"), type);
    var content = model.content();
    json(response, JSONCodec.encodeObject(o -> {
      o.put("configured", !content.revision().isEmpty()).put("title", content.title())
          .put("zone", content.layout().zone()).put("order", content.layout().order())
          .put("enabled", content.layout().enabled()).put("revision", content.revision()).put("csrf", csrf);
      if ("faq".equals(type)) {
        var configuration = model.faqConfiguration();
        o.put("source", configuration.source()).put("size", configuration.size())
            .put("selection", configuration.selection());
      } else {
        var configuration = model.usersConfiguration();
        o.put("group", configuration.group()).put("size", configuration.size())
            .put("avatar", configuration.avatar()).put("fields", configuration.fields());
      }
      return o;
    }));
  }

  private void feedResponse(HttpServletRequest request, HttpServletResponse response,
      HomepagePage page, String widget, String language, String csrf, String type) throws IOException {
    var model = HomepageWidgets.feedWidget(store, page, widget, language,
        (org.silverpeas.looks.aurora.LookAuroraHelper)
            request.getSession(false).getAttribute("Silverpeas_LookHelper"), type);
    var content = model.content();
    json(response, JSONCodec.encodeObject(o -> {
      o.put("configured", !content.revision().isEmpty()).put("title", content.title())
          .put("zone", content.layout().zone()).put("order", content.layout().order())
          .put("enabled", content.layout().enabled()).put("revision", content.revision()).put("csrf", csrf);
      if ("events".equals(type)) {
        var configuration = model.eventsConfiguration();
        o.put("source", configuration.source()).put("maxDays", configuration.maxDays())
            .put("includeToday", configuration.includeToday()).put("personal", configuration.personal())
            .put("importantOnly", configuration.importantOnly()).put("showMore", configuration.showMore());
      } else {
        o.put("source", model.rssConfiguration().source()).put("rendering", model.rssConfiguration().rendering());
      }
      return o;
    }));
  }

  private void ephemerisResponse(HttpServletRequest request, HttpServletResponse response,
      HomepagePage page, String widget, String language, String csrf) throws IOException {
    var model = HomepageWidgets.ephemerisWidget(store, page, widget, language,
        (org.silverpeas.looks.aurora.LookAuroraHelper)
            request.getSession(false).getAttribute("Silverpeas_LookHelper"));
    var content = model.content();
    json(response, JSONCodec.encodeObject(o -> o.put("configured", !content.revision().isEmpty())
        .put("title", content.title()).put("zone", content.layout().zone())
        .put("order", content.layout().order()).put("enabled", content.layout().enabled())
        .put("revision", content.revision()).put("csrf", csrf)));
  }

  private boolean option(HttpServletRequest request, String name) {
    return request.getParameter(name) == null || Boolean.parseBoolean(request.getParameter(name));
  }

  private void publicationsResponse(HttpServletRequest request, HttpServletResponse response,
      HomepagePage page, String widget, String language, String csrf) throws IOException {
    var model = HomepageWidgets.publicationsWidget(store, page, widget, language,
        (org.silverpeas.looks.aurora.LookAuroraHelper)
            request.getSession(false).getAttribute("Silverpeas_LookHelper"));
    var configuration = model.publicationsConfiguration();
    var content = model.content();
    json(response, JSONCodec.encodeObject(o -> o.put("configured", !content.revision().isEmpty())
        .put("title", content.title()).put("zone", content.layout().zone())
        .put("order", content.layout().order()).put("enabled", content.layout().enabled())
        .put("spaceId", configuration.spaceId()).put("size", configuration.size())
        .put("showMore", configuration.showMore()).put("revision", content.revision()).put("csrf", csrf)));
  }

  private void newsWidgetResponse(HttpServletRequest request, HttpServletResponse response,
      HomepagePage page, String widget, String language, String csrf) throws IOException {
    var helper = (org.silverpeas.looks.aurora.LookAuroraHelper)
        request.getSession(false).getAttribute("Silverpeas_LookHelper");
    var model = HomepageWidgets.newsWidget(store, page, widget, language, helper);
    var news = model.newsConfiguration();
    var content = model.content();
    json(response, JSONCodec.encodeObject(o -> o.put("configured", !content.revision().isEmpty())
        .put("title", content.title()).put("zone", content.layout().zone())
        .put("order", content.layout().order()).put("enabled", content.layout().enabled())
        .put("source", news.source()).put("size", news.size()).put("importantOnly", news.importantOnly())
        .put("showMoreNews", news.showMoreNews()).put("showAllNews", news.showAllNews())
        .put("rendering", news.rendering()).put("revision", content.revision()).put("csrf", csrf)));
  }

  private byte[] image(HttpServletRequest request) throws IOException, ServletException {
    var part = request.getPart("image");
    if (part == null || part.getSize() == 0 || part.getSize() > 5242880) {
      throw new IllegalArgumentException("Missing image");
    }
    try (var stream = part.getInputStream();
        var input = ImageIO.createImageInputStream(stream)) {
      var readers = ImageIO.getImageReaders(input);
      if (!readers.hasNext()) {
        throw new IllegalArgumentException("Unsupported image");
      }
      var reader = readers.next();
      try {
        reader.setInput(input);
        if ((long) reader.getWidth(0) * reader.getHeight(0) > 16000000) {
          throw new IllegalArgumentException("Image dimensions too large");
        }
        var output = new ByteArrayOutputStream();
        ImageIO.write(reader.read(0), "png", output);
        return output.toByteArray();
      } finally {
        reader.dispose();
      }
    } finally {
      part.delete();
    }
  }

  private String csrfToken(HttpSession session) {
    synchronized (session) {
      String token = (String) session.getAttribute(TOKEN);
      if (token == null) {
        token = UUID.randomUUID().toString();
        session.setAttribute(TOKEN, token);
      }
      return token;
    }
  }

  private boolean validToken(String expected, String actual) {
    return actual != null && MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
        actual.getBytes(StandardCharsets.UTF_8));
  }

  private void contentResponse(HttpServletResponse response, HomepageContentStore.Content content,
      String csrf) throws IOException {
    json(response, JSONCodec.encodeObject(o -> o.put("title", content.title())
        .put("html", content.html()).put("revision", content.revision()).put("csrf", csrf)
        .put("zone", content.layout().zone()).put("order", content.layout().order())
        .put("enabled", content.layout().enabled())));
  }

  private void json(HttpServletResponse response, String json) throws IOException {
    response.setContentType("application/json;charset=UTF-8");
    response.getWriter().write(json);
  }
}
