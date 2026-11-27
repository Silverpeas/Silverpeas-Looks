<%--

    Copyright (C) 2000 - 2026 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "https://www.silverpeas.org/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="org.silverpeas.core.web.look.LookHelper" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.operationpanes.OperationPaneType" %>
<%@ page import="org.silverpeas.core.admin.service.OrganizationController" %>
<%@ page import="org.silverpeas.core.web.mvc.controller.MainSessionController" %>
<%@ page import="org.silverpeas.looks.aurora.LookAuroraHelper" %>
<%@ page import="org.silverpeas.looks.aurora.homepage.*" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib uri="silverpeas.tags.viewGenerator" prefix="view" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/look" prefix="viewTags" %>
<view:timeout/>
<%
  response.setHeader("Cache-Control", "no-store");
  MainSessionController controller = MainSessionController.getInstance(session);
  LookAuroraHelper helper = (LookAuroraHelper) LookHelper.getLookHelper(session);
  String pageOwner = request.getParameter("SpaceId");
  OrganizationController organization = OrganizationController.get();
  if (controller == null || pageOwner == null ||
      !organization.isSpaceAvailable(pageOwner, controller.getUserId())) {
    response.sendError(403);
    return;
  }
  org.silverpeas.core.admin.space.SpaceInstLight space = organization.getSpaceInstLightById(pageOwner);
  if (space == null) {
    response.sendError(404);
    return;
  }
  // Canonical Silverpeas identifier (WA...) also identifies the properties file.
  HomepagePage homepagePage = new HomepagePage("spaces", space.getId());
  HomepageContentStore store = HomepageContentStore.forInstallation();
  java.util.List<HomepageContentStore.Widget> widgets = HomepageWidgets.all(store, homepagePage, controller.getFavoriteLanguage(), helper);
  request.setAttribute("homepageKind", homepagePage.kind());
  request.setAttribute("homepageOwner", homepagePage.owner());
  request.setAttribute("homepageLanguage", controller.getFavoriteLanguage());
  request.setAttribute("homepageWidgets", widgets);
  request.setAttribute("homepageNews", HomepageWidgets.news(store, homepagePage, widgets, helper));
  request.setAttribute("homepageNavigation", HomepageWidgets.navigation(homepagePage, widgets, helper));
  request.setAttribute("homepagePublications", HomepageWidgets.publications(homepagePage, widgets, helper));
  request.setAttribute("homepageAdmin", controller.getCurrentUserDetail().isAccessAdmin() ||
      java.util.Arrays.stream(organization.getUserManageableSpaceIds(controller.getUserId()))
          .anyMatch(id -> new HomepagePage("spaces", id).owner().equals(homepagePage.owner())));
  GraphicElementFactory factory = (GraphicElementFactory) session.getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);
  factory.setSpaceIdForCurrentRequest(homepagePage.owner());
%>
<c:set var="lookHelper" value="${sessionScope['Silverpeas_LookHelper']}"/>
<view:setBundle bundle="${lookHelper.localizedBundle}"/>
<c:url value="/AuroraHomepageContent" var="manageHomepageUrl">
  <c:param name="action" value="manage"/>
  <c:param name="kind" value="spaces"/>
  <c:param name="owner" value="${homepageOwner}"/>
  <c:param name="language" value="${homepageLanguage}"/>
</c:url>
<view:sp-page>
  <view:sp-head-part>
    <view:link href="/look/jsp/css/aurora.css"/>
    <view:link href="/look/jsp/css/responsiveslides.css"/>
    <view:link href="/look/jsp/css/themes.css"/>
    <view:link href="/look/jsp/css/homepage-main.css?version=15592-navigation-groups-v3"/>
    <view:link href="/look/jsp/css/homepage-space.css?version=15592-navigation-groups-v3"/>
    <view:includePlugin name="pdc"/>
    <view:includePlugin name="lightslideshow"/>
    <view:script src="/look/jsp/js/responsiveslides.min.js"/>
    <view:script src="/look/jsp/js/homepage-feeds.js"/>
    <view:script src="/look/jsp/js/homepage-search.js"/>
    <view:script src="/look/jsp/js/homepage-space.js?version=15592-navigation-groups-v3"/>
  </view:sp-head-part>
  <view:sp-body-part cssClass="spaceHome">
    <view:browseBar spaceId="${homepageOwner}"/>
    <view:operationPane type="<%=OperationPaneType.space%>">
      <c:if test="${homepageAdmin}">
        <view:operation action="${manageHomepageUrl}" altText="Modifier la page d’accueil"/>
      </c:if>
    </view:operationPane>
    <view:window>
      <div class="main-home-page homepage-space-columns">
        <div class="homepage-space-main">
          <viewTags:displayHomepageWidgets widgets="${homepageWidgets}" news="${homepageNews}" publications="${homepagePublications}" zone="main"/>
        </div>
        <div class="homepage-space-right">
          <viewTags:displayHomepageWidgets widgets="${homepageWidgets}" news="${homepageNews}" publications="${homepagePublications}" zone="right"/>
        </div>
      </div>
    </view:window>
  </view:sp-body-part>
</view:sp-page>
