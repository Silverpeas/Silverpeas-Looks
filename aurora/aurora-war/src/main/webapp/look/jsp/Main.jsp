<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/look" prefix="viewTags" %>

<c:set var="lookHelper" value="${sessionScope['Silverpeas_LookHelper']}"/>
<c:set var="settings" value="${lookHelper.lookSettings}"/>

<c:set var="weatherCities" value="${lookHelper.weatherCities}"/>
<c:set var="showWeather" value="${not empty weatherCities}"/>
<c:set var="showEphemeris" value="${settings.displayEphemeris}"/>

<c:set var="listOfNews" value="${lookHelper.news}"/>
<c:set var="newsImageSize" value="${settings.newsImageSize}"/>
<c:set var="shortcuts" value="${lookHelper.mainShortcuts}"/>
<c:set var="questions" value="${lookHelper.questions}"/>
<c:set var="publications" value="${lookHelper.latestPublications}"/>
<c:set var="bookmarks" value="${lookHelper.bookmarks}"/>
<c:set var="showBookmarksAreaWhenEmpty" value="${settings.displayBookmarksAreaWhenEmpty}"/>
<c:set var="noBookmarksFragment" value="${settings.noBookmarksFragmentURL}"/>
<c:set var="labelInsideSelectOnTaxonomy" value="${settings.labelsInsideSelectOnTaxonomy}"/>

<c:set var="newsWithCarrousel" value="${settings.displayNewsWithCarrousel}"/>

<c:set var="secondaryListOfNews" value="${lookHelper.secondaryNews}"/>
<c:set var="secondaryNewsImageSize" value="${settings.secondaryNewsImageSize}"/>
<c:set var="secondaryNewsWithCarrousel" value="${settings.displaySecondaryNewsWithCarrousel}"/>

<c:set var="thirdListOfNews" value="${lookHelper.thirdNews}"/>
<c:set var="thirdNewsImageSize" value="${settings.thirdNewsImageSize}"/>
<c:set var="thirdNewsWithCarrousel" value="${settings.displayThirdNewsWithCarrousel}"/>

<c:set var="newUsers" value="${lookHelper.newUsersList}"/>

<c:set var="extraJavascript" value="${settings.extraJavascriptForHome}"/>

<c:set var="searchForm" value="${lookHelper.mainSearchForm}"/>
<view:setBundle bundle="${lookHelper.localizedBundle}"/>

<view:sp-page>
<view:sp-head-part>
  <jsp:attribute name="atTop">
    <c:if test="${newsWithCarrousel}">
      <link rel="stylesheet" href="css/responsiveslides.css" type="text/css" media="screen" />
      <link rel="stylesheet" href="css/themes.css" type="text/css" media="screen" />
    </c:if>
    <view:link href="/look/jsp/css/aurora.css"/>
  </jsp:attribute>
  <jsp:body>
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

      <viewTags:displayNews listOfNews="${listOfNews}" carrousel="${newsWithCarrousel}" imageSize="${newsImageSize}"/>

      <viewTags:displayNews listOfNews="${secondaryListOfNews}" carrousel="${secondaryNewsWithCarrousel}" imageSize="${secondaryNewsImageSize}" id="secondaryNews"/>

      <viewTags:displayNews listOfNews="${thirdListOfNews}" carrousel="${thirdNewsWithCarrousel}" imageSize="${thirdNewsImageSize}" id="thirdNews"/>

      <viewTags:displayPublications lookHelper="${lookHelper}" publications="${publications}"/>

      <div id="rss-part"></div>

      <viewTags:displayNewUsers newUsers="${newUsers}"/>

      <viewTags:displayFreeZone freeZone="${lookHelper.freeZone}" freeZoneId="freeZone"/>

    </div>
  </div>
</div>
</view:sp-body-part>
</view:sp-page>
