<%--

    Copyright (C) 2000 - 2021 Silverpeas

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
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page import="org.silverpeas.core.web.util.viewgenerator.html.operationpanes.OperationPaneType" %>
<%@page import="org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory" %>
<%@page import="org.silverpeas.core.admin.user.model.UserDetail" %>
<%@page import="org.silverpeas.core.contribution.publication.model.PublicationDetail" %>
<%@page import="java.util.List" %>
<%@page import="org.silverpeas.core.web.look.LookHelper" %>
<%@ page import="org.silverpeas.looks.aurora.LookAuroraHelper" %>
<%@ page import="org.silverpeas.looks.aurora.AuroraSpaceHomePage" %>
<%@ page import="org.silverpeas.looks.aurora.Space" %>
<%@ page import="org.silverpeas.looks.aurora.App" %>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/look" prefix="viewTags" %>

<%
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
  response.setHeader("Pragma", "no-cache"); //HTTP 1.0
  response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>

<view:timeout/>

<%
  LookAuroraHelper helper = (LookAuroraHelper) LookHelper.getLookHelper(session);
  AuroraSpaceHomePage homepage = helper.getHomePage(request.getParameter("SpaceId"));
  List<PublicationDetail> publications = homepage.getPublications();
  List<PublicationDetail> news = homepage.getNews();
  Space space = homepage.getSpace();
  List<Space> subspaces = homepage.getSubSpaces();
  List<App> apps = homepage.getApps();
  List<UserDetail> admins = homepage.getAdmins();

  GraphicElementFactory gef = (GraphicElementFactory) session
      .getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);
  gef.setSpaceIdForCurrentRequest(homepage.getSpace().getId());
%>

<c:set var="backOfficeURL" value="<%=homepage.getBackOfficeURL()%>"/>
<c:set var="spaceId" value="<%=homepage.getSpace().getId()%>"/>

<c:set var="lookHelper" value="${sessionScope['Silverpeas_LookHelper']}"/>
<view:setBundle bundle="${lookHelper.localizedBundle}"/>

<fmt:message var="actionUpdate" key="look.space.home.update"/>

<view:sp-page angularJsAppName="silverpeas.spaceHomepage">
<view:sp-head-part>
  <jsp:attribute name="atTop">
    <!-- CSS SpaceHome -->
    <style type="text/css">
      .spaceHome .portlet {
        margin-bottom: 0.5em;
      }
    </style>
    <link rel="stylesheet" href="css/responsiveslides.css" type="text/css" media="screen" />
    <link rel="stylesheet" href="css/themes.css" type="text/css" media="screen" />
    <viewTags:spaceNewsCSS/>
    <viewTags:spaceNavigationCSS/>
    <viewTags:spacePublicationsCSS/>
    <view:link href="/look/jsp/css/aurora.css"/>
  </jsp:attribute>
  <jsp:body>
    <view:includePlugin name="pdc" />
    <view:includePlugin name="lightslideshow"/>
    <view:includePlugin name="toggle"/>
    <script type="text/javascript" src="js/responsiveslides.min.js"></script>
    <script type="text/javascript">
      <!--
      function goToSpaceItem(spaceId) {
        spWindow.loadSpace(spaceId);
      }
      function goToComponentItem(spaceId) {
        spWindow.loadComponent(spaceId);
      }

      whenSilverpeasReady(function() {
        // if right column is empty
        if ($.trim($(".rightContent").text()).length === 0) {
          $(".rightContent").css("display", "none");
          $(".principalContent").css("margin-right", "0");
        }
      });
      -->
    </script>
  </jsp:body>
</view:sp-head-part>
<view:sp-body-part cssClass="spaceHome ${spaceId}">
<view:browseBar spaceId="${spaceId}"/>
<view:operationPane type="<%=OperationPaneType.space %>">
  <c:if test="${not empty backOfficeURL}">
    <view:operation action="<%=homepage.getBackOfficeURL()%>" altText="${actionUpdate}"/>
  </c:if>
</view:operationPane>
<view:window>
  <div id="portletPages" class="rightContent">

    <viewTags:spacePicture pictureURL="<%=homepage.getSecondPicture()%>"/>

    <viewTags:spaceAdmins admins="<%=admins%>"/>

    <viewTags:spaceUsers users="<%=homepage.getUsers()%>" label="<%=homepage.getUsersLabel()%>"/>

    <viewTags:spaceNews news="<%=news%>"/>

    <viewTags:displayNextEvents nextEvents="<%=homepage.getNextEvents()%>"/>

    <viewTags:displayTaxonomy enabled="<%=homepage.isTaxonomyEnabled()%>" labelsInsideSelect="${true}" spaceId="${spaceId}"/>

    <viewTags:displayMedias medias="<%=homepage.getLatestMedias()%>"/>
  </div>

  <div class="principalContent">
    <viewTags:spaceIntro space="<%=space%>"/>

    <viewTags:spaceNavigation apps="<%=apps%>" subspaces="<%=subspaces%>"/>

    <viewTags:displayShortcuts shortcuts="<%=homepage.getShortcuts()%>"/>

    <viewTags:displayPublications lookHelper="${lookHelper}" publications="<%=publications%>"/>
  </div>
</view:window>
</view:sp-body-part>
</view:sp-page>