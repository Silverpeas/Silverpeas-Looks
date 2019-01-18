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

<c:set var="rssFeeds" value="${lookHelper.RSSFeeds}"/>
<c:set var="searchForm" value="${lookHelper.mainSearchForm}"/>
<view:setBundle bundle="${lookHelper.localizedBundle}"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Homepage</title>
<c:if test="${newsWithCarrousel}">
  <link rel="stylesheet" href="css/responsiveslides.css" type="text/css" media="screen" />
  <link rel="stylesheet" href="css/themes.css" type="text/css" media="screen" />
</c:if>
<view:link href="/look/jsp/css/aurora.css"/>
<view:looknfeel/>
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
<script type="text/javascript">
function changeBody(url) {
  if (StringUtil.isDefined(url)) {
    spWindow.loadLink(webContext+url);
  }
}

whenSilverpeasReady(function() {
  sp.load('#next-event-part','parts/MainNextEventsPart.jsp');
});

</script>
</head>
<body class="main-home-page">
<div class="main-container">
  <div class="main wrapper clearfix">
    <div class="right-main-container">
      <div id="next-event-part"></div>

      <viewTags:displayFAQ questions="${questions}"/>

      <viewTags:displayWeather showEphemeris="${showEphemeris}" showWeather="${showWeather}" cities="${weatherCities}"/>

      <viewTags:displayTaxonomy enabled="${settings.displaySearchOnHome}" labelsInsideSelect="${labelInsideSelectOnTaxonomy}" />

      <viewTags:displayFormSearch searchForm="${searchForm}"/>

      <viewTags:displayUserBookmarks bookmarks="${bookmarks}" showWhenEmpty="${showBookmarksAreaWhenEmpty}" noBookmarksFragment="${noBookmarksFragment}"/>
    </div>
                
    <div class="principal-main-container">

      <viewTags:displayShortcuts shortcuts="${shortcuts}"/>

      <viewTags:displayNews listOfNews="${listOfNews}" carrousel="${newsWithCarrousel}" imageSize="${newsImageSize}"/>

      <viewTags:displayPublications publications="${publications}"/>

      <viewTags:displayRSSFeeds rssFeeds="${rssFeeds}"/>

      <viewTags:displayFreeZone/>

    </div>
  </div>
</div>
</body>
</html>