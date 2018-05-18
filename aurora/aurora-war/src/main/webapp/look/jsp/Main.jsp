<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>

<c:set var="lookHelper" value="${sessionScope['Silverpeas_LookHelper']}"/>
<c:set var="settings" value="${lookHelper.lookSettings}"/>

<c:set var="weatherCities" value="${lookHelper.weatherCities}"/>
<c:set var="showWeather" value="${not empty weatherCities}"/>
<c:set var="showEphemeris" value="${settings.displayEphemeris}"/>

<c:set var="listOfNews" value="${lookHelper.news}"/>
<c:set var="newsImageSize" value="${settings.newsImageSize}"/>
<c:set var="shortcuts" value="${lookHelper.mainShortcuts}"/>
<c:set var="questions" value="${lookHelper.questions}"/>
<c:set var="publications" value="${lookHelper.dernieresPublications}"/>
<c:set var="bookmarks" value="${lookHelper.bookmarks}"/>
<c:set var="someBookmarks" value="${not empty bookmarks}"/>
<c:set var="showBookmarksAreaWhenEmpty" value="${settings.displayBookmarksAreaWhenEmpty}"/>
<c:set var="noBookmarksFragment" value="${settings.noBookmarksFragmentURL}"/>
<c:set var="labelInsideSelectOnTaxonomy" value="${settings.labelsInsideSelectOnTaxonomy}"/>

<c:set var="newsClass" value="list-news"/>
<c:set var="newsWithCarrousel" value="${settings.displayNewsWithCarrousel}"/>
<c:if test="${newsWithCarrousel}">
  <c:set var="newsClass" value="rslides"/>
</c:if>
<c:set var="now" value="<%=new java.util.Date()%>" />

<view:setBundle bundle="${lookHelper.localizedBundle}"/>

<fmt:message var="labelNews" key="look.home.news"/>
<fmt:message var="labelNewsMore" key="look.home.news.more"/>
<fmt:message var="labelQuestions" key="look.home.faq.title"/>
<fmt:message var="labelQuestionsPost" key="look.home.faq.post"/>
<fmt:message var="labelQuestionsMore" key="look.home.faq.more"/>

<fmt:message var="labelWeather" key="look.home.weather.title"/>
<fmt:message var="labelWeatherToday" key="look.home.weather.today"/>
<fmt:message var="labelWeatherTomorrow" key="look.home.weather.tomorrow"/>

<fmt:message var="labelSearch" key="look.home.search.title"/>
<fmt:message var="labelSearchButton" key="look.home.search.button"/>
<fmt:message var="labelBookmarks" key="look.home.bookmarks.title"/>
<fmt:message var="labelBookmarksManage" key="look.home.bookmarks.manage"/>
<fmt:message var="labelBookmarksMore" key="look.home.bookmarks.more"/>
<fmt:message var="labelShortcuts" key="look.home.shortcuts"/>
<fmt:message var="labelPublications" key="look.home.publications.title"/>

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
<style>
html, body {
	height:100%;
	min-height:644px;
}

.other-bookmark {
  display: none;
}
</style>
<script type="text/javascript" src="js/responsiveslides.min.js"></script>
<script type="text/javascript" src="js/jquery.cookie.js"></script>
<script type="text/javascript" src="js/ephemeris.min.js"></script>
<script type="text/javascript">
var weatherCookieName = "Silverpeas_Intranet_LastVisitedCity";

function changeBody(url) {
  if (StringUtil.isDefined(url)) {
    spWindow.loadLink(webContext+url);
  }
}

function addPrefix(str) {
	if($.browser.edge || $.browser.safari) {
	  return str;
	}
	return "yweather\\:"+str;
}

jQuery.browser = {};
jQuery.browser.edge = /edge/.test(navigator.userAgent.toLowerCase());
jQuery.browser.chrome = /chrome/.test(navigator.userAgent.toLowerCase());
jQuery.browser.safari = /safari/.test(navigator.userAgent.toLowerCase());
if (jQuery.browser.chrome && jQuery.browser.safari) {
  jQuery.browser.safari = false;
}

function showWeather(woeid) {
	$.cookie(weatherCookieName, woeid);
	$('#localisation-weather a').removeClass("select");
	$('#'+woeid).addClass("select");
	var url = "http://fr.meteo.yahoo.com/france/dummy/unknown-"+woeid;
	$('#external_meteo').attr("href", url);
	$.ajax({
		url: webContext+"/RAjaxMeteo/",
		type: "get",
		dataType: "xml",
		cache: false,
			  
		// As part of the data, we have to pass in the
		// the target url for our server-side AJAX request.
		data: {
			woeid: woeid
		},
			  
	 	// Alert when content has been loaded.
	 	success: function( xmlData ){
	 		// Get the content from the response XML.
	 		var city = $(xmlData).find(addPrefix("location")).attr('city');
			var numeroJour = 0;
	 		var strData = $(xmlData).find(addPrefix("forecast")).each(function(){
				numeroJour++;
				var date = $(this).attr('date');
				var day = $(this).attr('day');
				var low = $(this).attr('low');
				var high = $(this).attr('high');

				// Convert F to C°
				low = Math.round((low - 32) / 1.8);
				high = Math.round((high - 32) / 1.8);

				var code = $(this).attr('code');
				$('#day'+numeroJour+' .temperature .min').html("min "+low+"&deg;");
				$('#day'+numeroJour+' .temperature .max').html("max "+high+"&deg;");
				$('#day'+numeroJour+' img').attr("alt", $(this).attr('text'));
				$('#day'+numeroJour+' img').attr("src", webContext+"/look/jsp/imgDesign/meteo/meteo_"+code+".png");
			});
	 	},
	 
	 	error : function(text) {
	 		$('#day1').html("M&eacute;t&eacute;o indisponible");
	 	}
	});	
}

function search() {
  var values = $('#used_pdc').pdc('selectedValues');
  if (values.length > 0) {
    document.AdvancedSearch.AxisValueCouples.value = values.flatten();
  }
  document.AdvancedSearch.submit();
}

function toggleBookmarks() {
	$(".other-bookmark").toggle("slow");
	if ($(".other-bookmark").css("display") == "none") {
		$("#user-favorit-home a").removeClass("less");
	} else {
		$("#user-favorit-home a").addClass("less");
	}
}

$(document).ready(function() {

  sp.load('#next-event-part','parts/MainNextEventsPart.jsp');

  $("#ephemeride").html(ephemeris.getTodayEphemerisName());

  <c:if test="${newsWithCarrousel}">
	$("#slider").responsiveSlides({
        auto: true,
        pager: false,
        nav: true,
        speed: 500,
        pause: true,
        timeout: 6000,
        namespace: "centered-btns"
    });
  </c:if>

	<c:if test="${showWeather}">
    // init weather
    var woeid = $.cookie(weatherCookieName);
    if(woeid == null){
      woeid = $("#localisation-weather a:first").attr("id");
    }
    showWeather(woeid);
	</c:if>
	
	$('#used_pdc').pdc('used', {
	  labelInsideSelect : ${labelInsideSelectOnTaxonomy}
  });
});
</script>
</head>
<body class="main-home-page">
<div class="main-container">
  <div class="main wrapper clearfix">
    <div class="right-main-container">
      <div id="next-event-part"></div>
      <c:if test="${not empty questions.list}">
			  <div class="secteur-container faq" id="faq-home">
				  <h4>${labelQuestions}</h4>
					<div class="FAQ-entry-main-container">
					  <div class="FAQ-entry">
              <c:forEach var="question" items="${questions.list}">
                <c:url var="questionURL" value="${question._getPermalink()}"/>
					      <p><a class="sp-permalink" href="${questionURL}">${question.title}</a></p>
              </c:forEach>
            </div>
            <c:if test="${questions.canAskAQuestion}">
              <c:url var="newQuestionURL" value="${questions.requestURL}"/>
    				  <a title="${labelQuestionsPost}" href="${newQuestionURL}" class="link-add sp-link"><span>${labelQuestionsPost}</span> </a>
            </c:if>
          </div>
					<a title="${labelQuestionsMore}" href="${questions.appURL}" class="link-more sp-link"><span>${labelQuestionsMore}</span> </a>
        </div>
      </c:if>
					
      <c:if test="${showEphemeris}">
			  <div class="secteur-container weather" id="weather-home">
				  <h4><span class="title">${labelWeather}</span><span class="date-today"> <fmt:formatDate value="${now}" pattern="dd MMMMM yyyy"/></span></h4>
					<div id="ephemeride">Brigitte</div>
          <c:if test="${showWeather}">
				    <div id="localisation-weather">
					  <c:set var="firstCity" value="true"/>
					  <c:forEach var="city" items="${weatherCities}">
								<a class="select" id="${city.woeid}" href="#" onclick="javascript:showWeather(${city.woeid});return false;"><span>${city.label}</span></a>
					  </c:forEach>
					</div>
				    <div class="day" id="day1"> <img alt="soleil et nuage" src="imgDesign/meteo/meteo_44.png" />
				      <div class="temperature"><span class="min">min XX&deg;</span> <br />
				        <span class="max">max XX&deg;</span> </div>
              <div class="label"><span>${labelWeatherToday}</span></div>
            </div>
				    <div class="day" id="day2"> <img alt="soleil et nuage" src="imgDesign/meteo/meteo_44.png" />
				      <div class="temperature"><span class="min">min XX&deg;</span> <br />
				        <span class="max">max XX&deg;</span> </div>
					  <div class="label"><span>${labelWeatherTomorrow}</span></div>
					</div>
          </c:if>
        </div>
      </c:if>
					
			<c:if test="${settings.displaySearchOnHome}">
				<div class="secteur-container search" id="bloc-advancedSeach">
			    <h4>${labelSearch}</h4>
          <c:url var="searchActionURL" value="/RpdcSearch/jsp/AdvancedSearch"/>
          <form method="post" action="${searchActionURL}" name="AdvancedSearch">
            <input type="text" id="query" value="" size="60" name="query" onkeypress="checkEnter(event)" autocomplete="off" class="ac_input"/>
				    <input type="hidden" name="AxisValueCouples"/><input type="hidden" name="mode" value="clear"/>
				    <fieldset id="used_pdc" class="skinFieldset"></fieldset>
				    <a id="submit-AdvancedSearch" href="javascript:search()"><span>${labelSearchButton}</span></a>
          </form>
        </div>
      </c:if>

      <c:if test="${someBookmarks || showBookmarksAreaWhenEmpty}">
        <div class="secteur-container user-favorit" id="user-favorit-home">
				  <h4>${labelBookmarks}</h4>
					<div class="user-favorit-main-container">
					  <c:if test="${someBookmarks}">
							<ul class="user-favorit-list">
                <c:set var="classFrag" value="main-bookmark"/>
                <c:set var="bId" value="0"/>
                <c:forEach var="bookmark" items="${bookmarks}">
                  <c:if test="${bId > 4}">
                    <c:set var="classFrag" value="other-bookmark"/>
					<c:set var="areaNeedLinkMore" value="true"/>
                  </c:if>
                  <c:set var="bookmarkUrl" value="${bookmark.url}"/>
                  <c:set var="target" value="_blank"/>
                  <c:if test="${not bookmarkUrl.toLowerCase().startsWith('http')}">
                    <c:url var="bookmarkUrl" value="${bookmark.url}"/>
                    <c:set var="target" value=""/>
                  </c:if>
								  <li class="${classFrag}"><a class="sp-link" href="${bookmarkUrl}" target="${target}" title="${bookmark.description}">${bookmark.name}</a></li>
                  <c:set var="bId" value="${bId+1}"/>
                </c:forEach>
							</ul>
            </c:if>
            <c:if test="${not someBookmarks and silfn:isDefined(noBookmarksFragment)}">
              <c:import var="htmlFragment" url="${noBookmarksFragment}" charEncoding="UTF-8"/>
              <c:out value="${htmlFragment}" escapeXml="false"/>
            </c:if>
		 
		   <c:if test="${areaNeedLinkMore}">
           <a title="${labelBookmarksMore}" href="#" class="link-more" onclick="toggleBookmarks();return false;"><span>${labelBookmarksMore}</span> </a>
          </c:if>
          </div>
          <a title="${labelBookmarksManage}" href="javaScript:changeBody('/RmyLinksPeas/jsp/Main')" class="link-add manage" ><span>${labelBookmarksManage}</span></a>
        </div>
      </c:if>
    </div>
                
    <div class="principal-main-container">

      <c:if test="${not empty shortcuts}">
				<div class="secteur-container cg-favorit" id="cg-favorit-home">
					<h4>${labelShortcuts}</h4>
					<div class="cg-favorit-main-container">
						<ul class="cg-favorit-list">
              <c:forEach var="shortcut" items="${shortcuts}">
							<li><a class="sp-link" href="${shortcut.url}" title="${shortcut.altText}" target="${shortcut.target}"><img alt="${shortcut.altText}" src="${shortcut.iconURL}" /> <span>${shortcut.altText}</span></a></li>
              </c:forEach>
						</ul>
					</div>
				</div>
      </c:if>

			<c:if test="${not empty listOfNews.news}">
        <div class="secteur-container" id="carrousel-actualite">
          <h4>${labelNews}</h4>
				  <ul class="${newsClass}" id="slider">
            <c:forEach var="news" items="${listOfNews.news}">
              <li class="news-${news.appShortcut.target}">
                <a class="sp-permalink" href="${news.permalink}">
                  <c:choose>
                    <c:when test="${not empty news.thumbnailURL}">
                      <view:image src="${news.thumbnailURL}" alt="" size="${newsImageSize}"/>
                    </c:when>
                    <c:otherwise>
                      <view:image src="/look/jsp/imgDesign/emptyNews.png" alt="" />
                    </c:otherwise>
                  </c:choose>
                </a>
                <div class="caption">
                  <h2><a class="sp-permalink" href="${news.permalink}">${news.title}</a></h2>
                  <p>
                    <span class="news-date"><view:formatDate value="${news.date}"/></span>
                    <c:if test="${empty listOfNews.uniqueAppURL}">
                      <a class="sp-link" href="${news.appShortcut.url}" title="${labelNewsMore}"><span class="news-app">${news.appShortcut.altText}</span></a>
                    </c:if>
                    ${news.description}
                  </p>
                </div>
              </li>
            </c:forEach>
				  </ul>
          <c:choose>
            <c:when test="${not empty listOfNews.uniqueAppURL}">
              <a title="${labelNewsMore}" href="${listOfNews.uniqueAppURL}" class="link-more sp-link"><span>${labelNewsMore}</span></a>
            </c:when>
            <c:otherwise>
              <div id="news-link-apps">
              <c:forEach items="${listOfNews.appShortcuts}" var="appNewsShortcut">
                <a title="${labelNewsMore}" href="${appNewsShortcut.url}" class="link-more sp-link" id="link-app-${appNewsShortcut.target}"><span>${appNewsShortcut.altText}</span></a>
              </c:forEach>
              </div>
            </c:otherwise>
          </c:choose>
				</div>
      </c:if>
				
      <div id="last-publication-home" class="secteur-container">
		    <h4>${labelPublications}</h4>
		    <div id="last-publicationt-main-container">
		      <ul class="last-publication-list">
            <c:forEach var="publication" items="${publications}">
		          <li onclick="location.href='${publication.permalink}'">
			          <a class="sp-link" href="${publication.permalink}">${publication.name}</a>
			          <view:username userId="${publication.updaterId}" />
			          <span class="date-publication"><view:formatDate value="${publication.updateDate}"/></span>
			          <p class="description-publication">${publication.description}</p>
              </li>
            </c:forEach>
          </ul>
        </div>
    	</div>
    </div>
  </div>
</div>
</body>
</html>