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
package org.silverpeas.looks.aurora.matomo;

import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.web.look.LookHelper;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Servlet {@link Filter} responsible for injecting the Matomo tracking script
 * into generated JSP pages.
 * <p>
 * The script is dynamically built using a Silverpeas template and enriched with
 * contextual information such as the current user, space and component identifiers.
 * </p>
 * <p>
 * The injection is performed just before the closing {@code </body>} tag of the HTML response.
 * </p>
 */
public class MatomoInjectionFilter implements Filter {


    // HTML closing tag used as injection point.
    public static final String START_TAG_SCRIPT = "<script>";
    public static final String END_TAG_SCRIPT = "</script>";
    public static final String VIRTUAL_PAGE = "virtualPage";
    public static final String TITLE = "title";

    // Configuration settings related to Matomo integration.
    private SettingBundle settings;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
        this.settings =
                ResourceLocator.getSettingBundle("org.silverpeas.statistics.settings.matomo");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // If Matomo tracking is disabled, continue without wrapping the response.
        if (!settings.getBoolean("matomo.enable")) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest httpReq = (HttpServletRequest) request;
        String path = httpReq.getRequestURI();
        String bodyPartJSP = settings.getString("bodypart.jsp");

        // Only process JSP pages and ignore specific technical pages.
        TrackableContent content = TrackableContentProvider.get(httpReq);
        boolean isBodyPart = path.contains(bodyPartJSP);
        boolean isTrackableContent = content.isContent();

        if (!isBodyPart && !isTrackableContent) {
            chain.doFilter(request, response);
            return;
        }

        // Wrap the response to capture the generated HTML content.
        ContentResponseWrapper responseWrapper = new ContentResponseWrapper((HttpServletResponse) response);
        chain.doFilter(request, responseWrapper);

        // Inject the Matomo script just before the closing </body> tag if present.
        String originalContent = responseWrapper.toString();
        if (originalContent.contains(END_TAG_SCRIPT)) {
            String matomoScript = MatomoScriptBuilder.build((HttpServletRequest) request, content,
                    getCurrentUserId((HttpServletRequest) request), settings.getString("matomo.url"),
                    settings.getString("matomo.siteId"));
            originalContent = originalContent.replace(END_TAG_SCRIPT, END_TAG_SCRIPT + matomoScript);
        }

        // Write the modified content back to the original response output stream.
        byte[] bytes = originalContent.getBytes(response.getCharacterEncoding());
        response.setContentLength(bytes.length);
        response.getOutputStream().write(bytes);
    }

    public String getCurrentUserId(HttpServletRequest request) {
        String userId = "";
        LookHelper helper = getLookHelper(request);
        if (helper != null) userId = helper.getUserId();
        return userId;
    }
    private LookHelper getLookHelper(HttpServletRequest request) {
        if (request == null) return null;
        HttpSession session = request.getSession(false);
        if (session == null) return null;
        Object attr = session.getAttribute("Silverpeas_LookHelper");
        if (attr == null) return  null;
        return (LookHelper) attr;
    }

}
