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

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import org.silverpeas.core.web.look.LookHelper;
import org.silverpeas.looks.aurora.LookAuroraHelper;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class LatestPublicationsServletTest {
  private final LatestPublicationsServlet servlet = new LatestPublicationsServlet();
  private HttpServletRequest request;
  private HttpServletResponse response;
  private LookAuroraHelper helper;

  @BeforeEach
  void setup() {
    request = mock(HttpServletRequest.class);
    response = mock(HttpServletResponse.class);
    helper = mock(LookAuroraHelper.class);
    var session = mock(HttpSession.class);
    when(request.getSession(false)).thenReturn(session);
    when(session.getAttribute(LookHelper.SESSION_ATT)).thenReturn(helper);
    when(request.getParameter("spaceId")).thenReturn("123");
    when(request.getParameter("limit")).thenReturn("5");
    when(request.getParameter("includeSubspaces")).thenReturn("false");
  }

  @Test
  void requiresAnExistingSession() throws Exception {
    when(request.getSession(false)).thenReturn(null);
    servlet.doGet(request, response);
    verify(response).sendError(401);
    verifyNoInteractions(helper);
  }

  @Test
  void rejectsInvalidParametersWithoutQueryingPublications() throws Exception {
    for (String limit : List.of("0", "-1", "1.5", "2147483647", "invalid")) {
      when(request.getParameter("limit")).thenReturn(limit);
      servlet.doGet(request, response);
    }
    verify(response, times(5)).sendError(400);
    verifyNoInteractions(helper);
  }

  @Test
  void forwardsTheAuthorizedListAndTheSubspaceOption() throws Exception {
    var dispatcher = mock(RequestDispatcher.class);
    when(request.getRequestDispatcher("/WEB-INF/jsp/latestPublications.jsp")).thenReturn(dispatcher);
    when(helper.getLatestPublications("123", 5, false)).thenReturn(List.of());
    servlet.doGet(request, response);
    verify(helper).getLatestPublications("123", 5, false);
    verify(request).setAttribute("publications", List.of());
    verify(dispatcher).forward(request, response);
    verify(response).setHeader("Cache-Control", "no-store");
    when(request.getParameter("includeSubspaces")).thenReturn("true");
    servlet.doGet(request, response);
    verify(helper).getLatestPublications("123", 5, true);
  }
}
