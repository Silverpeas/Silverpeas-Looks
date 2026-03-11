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

import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.template.SilverpeasTemplates;
import org.silverpeas.kernel.util.StringUtil;

import javax.servlet.http.HttpServletRequest;

class MatomoScriptBuilder {
    public static String build(HttpServletRequest request, TrackableContent content, String userId, String matomoUrl, String siteId) {
        SilverpeasTemplate template = SilverpeasTemplates.createSilverpeasTemplateOnCore("statistics");
        template.setAttribute("matomoUrl", matomoUrl);
        template.setAttribute("siteId", siteId);

        if (content.isContent()) {
            template.setAttribute("space", "");
            template.setAttribute("component", "");
            template.setAttribute("userId", userId);
            template.setAttribute("content", content.getContentType() + "_" + content.getContentId());
            template.setAttribute(MatomoInjectionFilter.TITLE, content.getContentName());
            template.setAttribute(MatomoInjectionFilter.VIRTUAL_PAGE, content.getPermalink());

            String script = template.applyFileTemplate("matomo");
            script = script.replace(MatomoInjectionFilter.START_TAG_SCRIPT, MatomoInjectionFilter.START_TAG_SCRIPT + "if (!document.body.dataset.matomoExecuted) {document.body.dataset.matomoExecuted = 'true';");
            script = script.replace(MatomoInjectionFilter.END_TAG_SCRIPT, "}" + MatomoInjectionFilter.END_TAG_SCRIPT);
            return script;
        } else {
            String spaceId = request.getParameter("SpaceId");
            String componentId = request.getParameter("ComponentId");
            String spaceName = "";
            if (StringUtil.isDefined(spaceId)) {
                try {
                    spaceName = Administration.get().getSpaceInstLightById(spaceId).getName();
                    template.setAttribute("space", spaceId);
                    template.setAttribute(MatomoInjectionFilter.TITLE, spaceName);
                } catch (Exception e) {
                    // empty name
                }
            }

            String componentName = "";
            if (StringUtil.isDefined(componentId)) {
                try {
                    componentName = Administration.get().getComponentInstLight(componentId).getName();
                    template.setAttribute("component", componentId);
                    template.setAttribute(MatomoInjectionFilter.TITLE, componentName);
                } catch (Exception e) {
                    // empty name
                }
            }
            template.setAttribute("userId", userId);
            template.setAttribute("content", "");

            if (StringUtil.isDefined(componentId)) {
                template.setAttribute(MatomoInjectionFilter.VIRTUAL_PAGE, "/silverpeas/Component/" + componentId);
            } else if (StringUtil.isDefined(spaceId)) {
                template.setAttribute(MatomoInjectionFilter.VIRTUAL_PAGE, "/silverpeas/Space/" + spaceId);
            }
            return template.applyFileTemplate("matomo");
        }
    }

}
