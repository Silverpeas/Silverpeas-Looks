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
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygContentTransformer;
import org.silverpeas.core.util.JSONCodec;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.kernel.logging.SilverLogger;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.UUID;

/** Opt-in prototype endpoint: no existing homepage is changed. */
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
    store = new HomepageContentStore(Path.of(dataHome, "homepages"));
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
      HomepagePage page = new HomepagePage(request.getParameter("kind"), request.getParameter("owner"));
      String action = request.getParameter("action");
      boolean edit = write || !"asset".equals(action) && !"view".equals(action);
      if (!isAllowed(controller, page, edit)) {
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
        return;
      }
      String csrf = csrfToken(session);
      if (write && !validToken(csrf, request.getHeader("X-Aurora-CSRF"))) {
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
        return;
      }
      String widget = request.getParameter("widget");
      String language = request.getParameter("language");
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

  boolean isAllowed(MainSessionController controller, HomepagePage page, boolean edit) {
    var user = controller.getCurrentUserDetail();
    if (user == null || !user.isActivatedState() && !user.isAnonymous()) {
      return false;
    }
    if (user.isAccessAdmin()) {
      return true;
    }
    if ("skins".equals(page.kind())) {
      return !edit && page.owner().equalsIgnoreCase(controller.getFavoriteLook());
    }
    var organization = OrganizationController.get();
    if (edit) {
      return Arrays.asList(organization.getUserManageableSpaceIds(user.getId())).contains(page.owner());
    }
    return organization.isSpaceAvailable(page.owner(), user.getId());
  }

  private void write(HttpServletRequest request, HttpServletResponse response, HomepagePage page,
      String widget, String language, String action) throws IOException, ServletException {
    if ("upload".equals(action)) {
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
      String sanitized = WysiwygContentTransformer.on(html).applySanitizeDirective().transform();
      store.save(page, widget, language, title, sanitized, request.getParameter("revision"));
      contentResponse(response, store.read(page, widget, language), "");
    } else {
      throw new IllegalArgumentException("Unknown action");
    }
  }

  private void read(HttpServletRequest request, HttpServletResponse response, HomepagePage page,
      String widget, String language, String action, String csrf) throws IOException, ServletException {
    if ("asset".equals(action)) {
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
    } else if ("editor".equals(action)) {
      request.getRequestDispatcher("/WEB-INF/homepage/contentEditor.jsp").forward(request, response);
    } else {
      throw new IllegalArgumentException("Unknown action");
    }
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
        .put("html", content.html()).put("revision", content.revision()).put("csrf", csrf)));
  }

  private void json(HttpServletResponse response, String json) throws IOException {
    response.setContentType("application/json;charset=UTF-8");
    response.getWriter().write(json);
  }
}
