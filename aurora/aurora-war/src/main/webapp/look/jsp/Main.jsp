<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/look" prefix="viewTags" %>
<%@ page import="java.util.stream.Stream" %>
<%@ page import="org.silverpeas.looks.aurora.NewsList" %>
<%@ page import="java.util.function.Predicate" %>

<c:set var="lookHelper" value="${sessionScope['Silverpeas_LookHelper']}"/>
<c:if test="${lookHelper != null}">
  <jsp:useBean id="lookHelper" type="org.silverpeas.looks.aurora.LookAuroraHelper"/>
</c:if>
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

<c:set var="listOfNews" value="${lookHelper.news}"/>
<jsp:useBean id="listOfNews" type="org.silverpeas.looks.aurora.NewsList"/>
<c:set var="secondaryListOfNews" value="${lookHelper.secondaryNews}"/>
<jsp:useBean id="secondaryListOfNews" type="org.silverpeas.looks.aurora.NewsList"/>
<c:set var="thirdListOfNews" value="${lookHelper.thirdNews}"/>
<jsp:useBean id="thirdListOfNews" type="org.silverpeas.looks.aurora.NewsList"/>

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
    <style type="text/css">
    html, body {
      height:100%;
      min-height:644px;
    }
    </style>
    <script type="text/javascript" src="js/responsiveslides.min.js"></script>
    <script type="text/javascript" src="js/jquery.cookie.js"></script>
    <script type="text/javascript" src="js/ephemeris.min.js"></script>
    <c:if test="${not empty extraJavascript}">
      <script type="text/javascript" src="${extraJavascript}"></script>
    </c:if>
    <script type="text/javascript">
    function changeBody(url) {
      if (StringUtil.isDefined(url)) {
        spWindow.loadLink(webContext+url);
      }
    }

    whenSilverpeasReady(function() {
      sp.ajaxRequest('parts/MainNextEventsPart.jsp').loadTarget('#next-event-part');
      sp.ajaxRequest('parts/MainRSSPart.jsp').loadTarget('#rss-part');
    });

    </script>
  </jsp:body>
</view:sp-head-part>
<view:sp-body-part cssClass="main-home-page">
<div class="main-container">
  <div class="main wrapper clearfix">
    <div class="right-main-container">

      <viewTags:displayFreeZone freeZone="${lookHelper.thinFreeZone}" freeZoneId="thinFreeZone"/>

      <div id="next-event-part"></div>

      <viewTags:displayFAQ questions="${questions}"/>

      <viewTags:displayWeather showEphemeris="${showEphemeris}" showWeather="${showWeather}" cities="${weatherCities}"/>

      <viewTags:displayTaxonomy enabled="${settings.displaySearchOnHome}" labelsInsideSelect="${labelInsideSelectOnTaxonomy}" />

      <viewTags:displayFormSearch searchForm="${searchForm}" extraFieldPeriod="${settings.extraSearchFieldPeriodUsed}" extraFieldSpace="${settings.extraSearchFieldSpaceUsed}"/>

      <viewTags:displayUserBookmarks bookmarks="${bookmarks}" showWhenEmpty="${showBookmarksAreaWhenEmpty}" noBookmarksFragment="${noBookmarksFragment}"/>
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

    </div>
  </div>
</div>
</view:sp-body-part>
</view:sp-page>
