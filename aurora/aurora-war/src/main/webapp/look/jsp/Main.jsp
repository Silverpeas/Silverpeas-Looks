<%--
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
 --%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="silverpeas.tags.viewGenerator" prefix="view"%>
<%@ taglib uri="jakarta.tags.core" prefix="c"%>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt"%>
<%@ taglib uri="silverpeas.tags.silverFunctions" prefix="silfn" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/look" prefix="viewTags" %>
<%@ page import="java.util.stream.Stream" %>
<%@ page import="org.silverpeas.looks.aurora.NewsList" %>
<%@ page import="org.silverpeas.looks.aurora.LookAuroraHelper" %>
<%@ page import="java.util.function.Predicate" %>

<c:set var="lookHelper" value="${sessionScope['Silverpeas_LookHelper']}"/>
<c:if test="${lookHelper != null}">
  <jsp:useBean id="lookHelper" type="org.silverpeas.looks.aurora.LookAuroraHelper"/>
</c:if>
<%@ page import="org.silverpeas.looks.aurora.homepage.HomepageContentStore" %>
<%@ page import="org.silverpeas.looks.aurora.homepage.HomepageContentServlet" %>
<%@ page import="org.silverpeas.looks.aurora.homepage.HomepagePage" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory" %>
<%@ page import="org.silverpeas.core.web.mvc.controller.MainSessionController" %>
<%
  MainSessionController homepageController = MainSessionController.getInstance(session);
  GraphicElementFactory homepageFactory = (GraphicElementFactory) session.getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);
  request.setAttribute("homepageAdmin", homepageController.getCurrentUserDetail().isAccessAdmin());
  request.setAttribute("homepageLanguage", homepageController.getFavoriteLanguage());
  request.setAttribute("homepageWidgets", HomepageContentStore.forInstallation().widgets(
      new HomepagePage("skins", HomepageContentServlet.skinId(homepageFactory)),
      homepageController.getFavoriteLanguage()));
%>
<c:set var="settings" value="${lookHelper.lookSettings}"/>

<c:set var="weatherCities" value="${lookHelper.weatherCities}"/>
<c:set var="showWeather" value="${not empty weatherCities}"/>
<c:set var="showEphemeris" value="${settings.displayEphemeris}"/>
<c:set var="showLastSubscribedNews" value="${settings.displayLastSubscribedNews}"/>

<c:set var="shortcuts" value="${lookHelper.mainShortcuts}"/>
<c:set var="questions" value="${lookHelper.questions}"/>
<c:set var="publications" value="${lookHelper.latestPublications}"/>
<c:set var="bookmarks" value="${lookHelper.bookmarks}"/>
<c:set var="showBookmarksAreaWhenEmpty" value="${settings.displayBookmarksAreaWhenEmpty}"/>
<c:set var="noBookmarksFragment" value="${settings.noBookmarksFragmentURL}"/>
<c:set var="labelInsideSelectOnTaxonomy" value="${settings.labelsInsideSelectOnTaxonomy}"/>

<%
  request.setAttribute("listOfNews", ((LookAuroraHelper) session.getAttribute("Silverpeas_LookHelper"))
      .getConfiguredNews(1, HomepageContentStore.forInstallation().readNews(
          new HomepagePage("skins", HomepageContentServlet.skinId(homepageFactory)), 1)));
%>
<jsp:useBean id="listOfNews" scope="request" type="org.silverpeas.looks.aurora.NewsList"/>
<%
  request.setAttribute("secondaryListOfNews", ((LookAuroraHelper) session.getAttribute("Silverpeas_LookHelper"))
      .getConfiguredNews(2, HomepageContentStore.forInstallation().readNews(
          new HomepagePage("skins", HomepageContentServlet.skinId(homepageFactory)), 2)));
%>
<jsp:useBean id="secondaryListOfNews" scope="request" type="org.silverpeas.looks.aurora.NewsList"/>
<%
  request.setAttribute("thirdListOfNews", ((LookAuroraHelper) session.getAttribute("Silverpeas_LookHelper"))
      .getConfiguredNews(3, HomepageContentStore.forInstallation().readNews(
          new HomepagePage("skins", HomepageContentServlet.skinId(homepageFactory)), 3)));
%>
<jsp:useBean id="thirdListOfNews" scope="request" type="org.silverpeas.looks.aurora.NewsList"/>

<c:if test="${showLastSubscribedNews}">
  <c:set var="subscribedListOfNews" value="${lookHelper.lastNewsSubscribed}"/>
  <jsp:useBean id="subscribedListOfNews" type="java.util.List<org.silverpeas.looks.aurora.AuroraNews>"/>
</c:if>

<c:set var="newUsers" value="${lookHelper.newUsersList}"/>

<c:set var="extraJavascript" value="${settings.extraJavascriptForHome}"/>

<c:set var="searchForm" value="${lookHelper.mainSearchForm}"/>
<view:setBundle bundle="${lookHelper.localizedBundle}"/>

<c:set var="isAtLeastOneCarousel" value="<%=Stream.of(listOfNews, secondaryListOfNews, thirdListOfNews)
      .filter(Predicate.not(NewsList::isEmpty))
      .map(NewsList::getRenderingType)
      .anyMatch(NewsList.RenderingType::isCarousel)%>"/>

<view:sp-page>
<view:sp-head-part>
  <jsp:attribute name="atTop">
    <c:if test="${isAtLeastOneCarousel}">
    <view:link href="/look/jsp/css/responsiveslides.css"/>
    <view:link href="/look/jsp/css/themes.css"/>
    </c:if>
    <view:link href="/look/jsp/css/aurora.css"/>
  </jsp:attribute>
  <jsp:body>
    <view:link href="/look/jsp/css/aurora.css"/>
    <view:includePlugin name="pdc" />
    <view:includePlugin name="popup"/>
    <view:link href="/look/jsp/css/homepage-main.css?version=15592-admin-button-v1"/>
    <script type="text/javascript" src="js/responsiveslides.min.js"></script>
    <script type="text/javascript" src="js/jquery.cookie.js"></script>
    <script type="text/javascript" src="js/ephemeris.min.js"></script>
    <c:if test="${not empty extraJavascript}">
      <script type="text/javascript" src="${extraJavascript}"></script>
    </c:if>
    <view:script src="/look/jsp/js/homepage-main.js"/>
  </jsp:body>
</view:sp-head-part>
<view:sp-body-part cssClass="main-home-page">
<div class="main-container">
  <c:if test="${homepageAdmin}">
    <c:url value="/AuroraHomepageContent" var="manageHomepageUrl">
      <c:param name="action" value="manage"/>
      <c:param name="kind" value="skins"/>
      <c:param name="language" value="${homepageLanguage}"/>
    </c:url>
    <div class="homepage-administration wrapper">
      <a class="sp_button homepage-edit-button" href="<c:out value='${manageHomepageUrl}'/>">
        <span class="homepage-edit-icon" aria-hidden="true">✎</span>
        <span>Modifier la page d’accueil</span>
      </a>
    </div>
  </c:if>
  <div class="main wrapper clearfix">
    <div class="right-main-container">

      <viewTags:displayFreeZone freeZone="${lookHelper.thinFreeZone}" freeZoneId="thinFreeZone"/>

      <div id="next-event-part"></div>

      <viewTags:displayFAQ questions="${questions}"/>

      <viewTags:displayWeather showEphemeris="${showEphemeris}" showWeather="${showWeather}" cities="${weatherCities}"/>

      <viewTags:displayTaxonomy enabled="${settings.displaySearchOnHome}" labelsInsideSelect="${labelInsideSelectOnTaxonomy}" />

      <viewTags:displayFormSearch searchForm="${searchForm}" extraFieldPeriod="${settings.extraSearchFieldPeriodUsed}" extraFieldSpace="${settings.extraSearchFieldSpaceUsed}"/>

      <viewTags:displayUserBookmarks bookmarks="${bookmarks}" showWhenEmpty="${showBookmarksAreaWhenEmpty}" noBookmarksFragment="${noBookmarksFragment}"/>
      <viewTags:displayHomepageWidgets widgets="${homepageWidgets}" zone="right"/>
    </div>

    <div class="principal-main-container">

      <viewTags:displayShortcuts shortcuts="${shortcuts}"/>

      <viewTags:displayNews listOfNews="${listOfNews}"/>

      <viewTags:displayNews listOfNews="${secondaryListOfNews}" id="secondaryNews"/>

      <viewTags:displayNews listOfNews="${thirdListOfNews}" id="thirdNews"/>

      <viewTags:displayNewsSubscribed enabled="${showLastSubscribedNews}" listOfNews="${subscribedListOfNews}"/>

      <viewTags:displayPublications lookHelper="${lookHelper}" publications="${publications}"/>

      <div id="rss-part"></div>

      <viewTags:displayNewUsers newUsers="${newUsers}"/>

      <viewTags:displayFreeZone freeZone="${lookHelper.freeZone}" freeZoneId="freeZone"/>

      <viewTags:displayHomepageWidgets widgets="${homepageWidgets}" zone="main"/>

    </div>
  </div>
</div>
</view:sp-body-part>
</view:sp-page>
