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
package org.silverpeas.looks.aurora.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.silverpeas.core.web.look.LookHelper;
import org.silverpeas.looks.aurora.LookAuroraHelper;

import java.io.IOException;

/** Renders the latest publications for the authenticated reader. */
public class LatestPublicationsServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    response.setHeader("Cache-Control", "no-store");
    var session = request.getSession(false);
    if (session == null || !(LookHelper.getLookHelper(session) instanceof LookAuroraHelper helper)) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }
    String spaceId = request.getParameter("spaceId");
    String limit = request.getParameter("limit");
    String includeSubspaces = request.getParameter("includeSubspaces");
    if (spaceId == null || !spaceId.matches("[1-9][0-9]*") || limit == null ||
        !limit.matches("[1-9][0-9]*") ||
        !("true".equals(includeSubspaces) || "false".equals(includeSubspaces))) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    final int count;
    try {
      count = Integer.parseInt(limit);
      // The helper requests twice this count from the publication service.
      if (count > Integer.MAX_VALUE / 2) {
        throw new NumberFormatException();
      }
    } catch (NumberFormatException e) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    request.setAttribute("publications", helper.getLatestPublications(spaceId, count,
        Boolean.parseBoolean(includeSubspaces)));
    response.setContentType("text/html;charset=UTF-8");
    request.getRequestDispatcher("/WEB-INF/jsp/latestPublications.jsp").forward(request, response);
  }
}
