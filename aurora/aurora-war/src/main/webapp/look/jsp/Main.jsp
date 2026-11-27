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
<%@ page import="org.silverpeas.looks.aurora.homepage.HomepageWidgets" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory" %>
<%@ page import="org.silverpeas.core.web.mvc.controller.MainSessionController" %>
<%
  MainSessionController homepageController = MainSessionController.getInstance(session);
  GraphicElementFactory homepageFactory = (GraphicElementFactory) session.getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);
  request.setAttribute("homepageOwner", HomepageContentServlet.skinId(homepageFactory));
  request.setAttribute("homepageAdmin", homepageController.getCurrentUserDetail().isAccessAdmin());
  request.setAttribute("homepageLanguage", homepageController.getFavoriteLanguage());
  HomepagePage homepagePage = new HomepagePage("skins", HomepageContentServlet.skinId(homepageFactory));
  LookAuroraHelper homepageHelper = (LookAuroraHelper) session.getAttribute("Silverpeas_LookHelper");
  java.util.List<HomepageContentStore.Widget> homepageWidgets = HomepageWidgets.all(
      HomepageContentStore.forInstallation(), homepagePage,
      homepageController.getFavoriteLanguage(), homepageHelper);
  java.util.Map<String, NewsList> homepageNews = HomepageWidgets.news(
      HomepageContentStore.forInstallation(), homepagePage, homepageWidgets, homepageHelper);
  request.setAttribute("homepageWidgets", homepageWidgets);
  request.setAttribute("homepageNews", homepageNews);
  request.setAttribute("homepageQuestions", HomepageWidgets.questions(homepageWidgets, homepageHelper));
  request.setAttribute("homepageSubscriptions", HomepageWidgets.subscriptions(homepageWidgets, homepageHelper));
  request.setAttribute("homepageUsers", HomepageWidgets.users(homepageWidgets, homepageHelper));
  request.setAttribute("homepagePublications", HomepageWidgets.publications(homepageWidgets, homepageHelper));
  request.setAttribute("isAtLeastOneCarousel", homepageNews.values().stream()
      .filter(news -> !news.isEmpty()).anyMatch(news -> news.getRenderingType().isCarousel()) || homepageWidgets.stream()
      .filter(widget -> widget.isEnabled() && "rss".equals(widget.type()))
      .anyMatch(widget -> "carrousel".equals(widget.rssConfiguration().rendering())));
%>
<c:set var="settings" value="${lookHelper.lookSettings}"/>










<c:set var="extraJavascript" value="${settings.extraJavascriptForHome}"/>

<view:setBundle bundle="${lookHelper.localizedBundle}"/>

<view:sp-page>
<view:sp-head-part>

  <jsp:attribute name="atTop">
    <c:if test="${isAtLeastOneCarousel}">
    <view:link href="/look/jsp/css/responsiveslides.css"/>
    <view:link href="/look/jsp/css/themes.css"/>
    </c:if>
    <view:link href="/look/jsp/css/aurora.css?version=15592-weather-v2"/>
  </jsp:attribute>
  <jsp:body>
    <view:script src="/look/jsp/js/silverpeas-weather.js"/>
    <view:script src="/look/jsp/js/silverpeas-weather-yahoo.js"/>
    <view:script src="/look/jsp/js/silverpeas-weather-openweathermap.js"/>
    <view:script src="/look/jsp/js/silverpeas-weather-accuweather.js"/>
    <view:script src="/look/jsp/js/homepage-weather.js?version=15592-weather-v2"/>
    <view:link href="/look/jsp/css/homepage-weather.css?version=15592-weather-v2"/>

    <view:link href="/look/jsp/css/aurora.css?version=15592-weather-v2"/>
    <view:includePlugin name="pdc" />
    <view:includePlugin name="popup"/>
    <view:link href="/look/jsp/css/homepage-image.css?version=15592-image-v1"/>
    <view:link href="/look/jsp/css/homepage-main.css?version=15592-weather-v2"/>
    <script type="text/javascript" src="js/responsiveslides.min.js"></script>
    <view:script src="/look/jsp/js/homepage-feeds.js?version=15592-weather-v2"/>
    <script type="text/javascript" src="js/jquery.cookie.js"></script>
    <script type="text/javascript" src="js/ephemeris.min.js"></script>
    <c:if test="${not empty extraJavascript}">
      <script type="text/javascript" src="${extraJavascript}"></script>
    </c:if>
    <view:script src="/look/jsp/js/homepage-search.js?version=15592-weather-v2"/>
    <view:script src="/look/jsp/js/homepage-main.js?version=15592-weather-v2"/>
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







      <viewTags:displayHomepageWidgets widgets="${homepageWidgets}" news="${homepageNews}" publications="${homepagePublications}" questions="${homepageQuestions}" users="${homepageUsers}" subscriptions="${homepageSubscriptions}" zone="right"/>
    </div>

    <div class="principal-main-container">

      <viewTags:displayHomepageWidgets widgets="${homepageWidgets}" news="${homepageNews}" publications="${homepagePublications}" questions="${homepageQuestions}" users="${homepageUsers}" subscriptions="${homepageSubscriptions}" zone="main"/>

    </div>
  </div>
</div>
</view:sp-body-part>
</view:sp-page>
