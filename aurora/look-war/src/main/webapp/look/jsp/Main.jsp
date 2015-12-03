<%@page import="org.silverpeas.components.quickinfo.model.News"%>
<%@page import="com.silverpeas.questionReply.model.Question"%>
<%@page import="com.silverpeas.myLinks.model.LinkDetail"%>
<%@page import="java.util.Calendar"%>
<%@page import="com.stratelia.webactiv.almanach.model.EventDetail"%>
<%@page import="java.util.Locale"%>
<%@page import="java.util.Date"%>
<%@page import="org.silverpeas.looks.aurora.NextEventsDate"%>
<%@page import="com.stratelia.webactiv.almanach.model.EventOccurrence"%>
<%@page import="org.silverpeas.looks.aurora.City"%>
<%@page import="org.silverpeas.looks.aurora.Zoom"%>
<%@page import="org.silverpeas.looks.aurora.LookAuroraHelper"%>
<%@ include file="../../admin/jsp/importFrameSet.jsp"%>

<%@page import="java.util.List"%>
<%@page import="com.silverpeas.util.StringUtil"%>
<%@page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@page import="com.stratelia.webactiv.util.publication.model.PublicationDetail"%>
<%@page import="com.silverpeas.look.Shortcut"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@ page import="org.silverpeas.looks.aurora.FAQ" %>

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:set var="lookHelper" value="${sessionScope['Silverpeas_LookHelper']}"/>

<%
LookAuroraHelper helper = (LookAuroraHelper) session.getAttribute("Silverpeas_LookHelper");
SimpleDateFormat eventDateFormat = new SimpleDateFormat("EEEE dd MMMM yyyy", Locale.FRENCH);
SimpleDateFormat mainDateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.FRENCH);
Zoom zoom = helper.getZoom();
List<EventOccurrence> eventsOfTheDay = helper.getTodayEvents();
List<NextEventsDate> nextEvents = helper.getNextEvents();
List<News> listOfNews = helper.getNews();
List<City> cities = helper.getWeatherCities();
boolean showWeather = cities != null && !cities.isEmpty();
String defaultWoeid = "";
if (showWeather) {
  defaultWoeid = cities.get(0).getWoeid();
}
Calendar calendar = Calendar.getInstance();
List<Shortcut> shortcuts = helper.getMainShortcuts();
List<LinkDetail> bookmarks = helper.getBookmarks();
FAQ faq = helper.getAQuestion();
boolean showEphemeris = helper.getSettings("home.ephemeris", true);
String newsSize = helper.getSettings("home.news.width","1095") + "x" + helper.getSettings("home.news.height","");
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Homepage</title>
<link rel="stylesheet" href="css/responsiveslides.css" type="text/css" media="screen" />
<link rel="stylesheet" href="css/themes.css" type="text/css" media="screen" />
<view:looknfeel/>
<view:includePlugin name="pdc" />
<view:includePlugin name="popup"/>
<link rel="stylesheet" href="css/normalize.min.css" />
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

function addPrefix(str) {
	if($.browser.msie || $.browser.mozilla) {
		return "yweather\\:"+str;
	}
	return str;
}

jQuery.browser = {};
jQuery.browser.mozilla = /mozilla/.test(navigator.userAgent.toLowerCase()) && !/webkit/.test(navigator.userAgent.toLowerCase());
jQuery.browser.webkit = /webkit/.test(navigator.userAgent.toLowerCase());
jQuery.browser.opera = /opera/.test(navigator.userAgent.toLowerCase());
jQuery.browser.msie = /msie/.test(navigator.userAgent.toLowerCase());

function showWeather(woeid) {
	$.cookie(weatherCookieName, woeid);
	$('#localisation-weather a').removeClass("select");
	$('#'+woeid).addClass("select");
	var url = "http://fr.meteo.yahoo.com/france/dummy/unknown-"+woeid;
	$('#external_meteo').attr("href", url);
	$.ajax({
		url: "<%=m_sContext%>/RAjaxMeteo/",
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
				var code = $(this).attr('code');
				$('#day'+numeroJour+' .temperature .min').html("min "+low+"&deg;");
				$('#day'+numeroJour+' .temperature .max').html("max "+high+"&deg;");
				$('#day'+numeroJour+' img').attr("alt", $(this).attr('text'));
				$('#day'+numeroJour+' img').attr("src", "/silverpeas/look/jsp/imgDesign/meteo/meteo_"+code+".png");
				//$('#day'+numeroJour).html('<a href="'+url+'" target="_blank"><img src="http://l.yimg.com/a/i/us/we/52/'+code+'.gif"/></a><div class="jour"><a href="'+url+'" target="_blank">'+days[day]+'<br/>'+low+'&deg;/'+high+'&deg;</a>');
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

  $("#ephemeride").html(ephemeris.getTodayEphemerisName());

	$("#slider").responsiveSlides({
        auto: true,
        pager: false,
        nav: true,
        speed: 500,
        pause: true,
        timeout: 6000,
        namespace: "centered-btns"
    });
	
	<% if (showWeather) { %>
	// init weather
	var woeid = $.cookie(weatherCookieName);
	if(woeid == null){
		woeid = <%=defaultWoeid%>;
	} 
	showWeather(woeid);
	<% } %>
	
	$('#used_pdc').pdc('used');
});
</script>
</head>
<body>
<div class="main-container">
            <div class="main wrapper clearfix">
               <div class="right-main-container">
               		<% if (nextEvents != null && !nextEvents.isEmpty()) { %>
					<div class="secteur-container events portlet" id="home-event" >
                            <div class="header">
                              <h2 class="portlet-title"><%=helper.getString("look.home.events.next")%></h2>
                            </div>
                            <div class="portlet-content" id="calendar">
                              <ul class="eventList" id="eventList">
                              	<% for (NextEventsDate date : nextEvents) {
                              	  calendar.setTime(date.date);
                              	%>
                                <li class="events">
                                  <div class="eventShortDate"><span class="number"><%=calendar.get(Calendar.DATE)%></span>/<span class="month"><%=calendar.get(Calendar.MONTH)+1 %></span></div>
                                  <div class="eventLongDate"><%=eventDateFormat.format(date.date) %></div>
                                  <% for (EventOccurrence eventOccurence : date.events) {
							          	EventDetail event = eventOccurence.getEventDetail();
							        %>
                                  <div class="event">
                                    <div class="eventName"> > <a href="<%=event.getPermalink()%>"><%=event.getName() %></a><span class="clock-events">
											<% if (StringUtil.isDefined(event.getStartHour())) { %>
												<%=event.getStartHour()%>
											<% } %>
											<% if (StringUtil.isDefined(event.getStartHour()) && StringUtil.isDefined(event.getEndHour()) && !event.getEndHour().equals(event.getStartHour())) { %>
												 - 
											<% } %>
											<% if (StringUtil.isDefined(event.getEndHour()) && !event.getEndHour().equals(event.getStartHour())) { %>
												 <%=event.getEndHour()%>
											<% } %>
											</span></div>
									<% if (StringUtil.isDefined(event.getPlace())) { %>
                                    <div class="eventInfo">
                                      <div class="eventPlace">
                                        <div class="bloc"><span><%=event.getPlace() %></span></div>
                                      </div>
                                      <br clear="left"/>
                                    </div>
                                    <% } %>
                                  </div>
                                  <% } %>
                                </li>
                                <% } %>
                              </ul>
                            </div>
							<a title="<%=helper.getString("look.home.events.more")%>" href="<%=URLManager.getSimpleURL(URLManager.URL_COMPONENT, helper.getSettings("home.events.appId", "")) %>" class="link-more"><span><%=helper.getString("look.home.events.more")%></span> </a>
                          </div>
                          <% } %>
			   
					<% if (faq != null) { %>
				   <div class="secteur-container faq" id="faq-home">
						<h4><%=helper.getString("look.home.faq.title")%></h4>
						<div class="FAQ-entry-main-container">
							<div class="FAQ-entry">
								<p><a href="<%=m_sContext %><%=faq.getQuestion()._getPermalink()%>"><%=faq.getQuestion().getTitle() %></a></p>
							</div>
              <% if (faq.isCanAskAQuestion()) { %>
							<a href="<%=m_sContext %>/RquestionReply/<%=faq.getQuestion().getInstanceId() %>/CreateQQuery" class="link-add"><span><%=helper.getString(
                  "look.home.faq.post")%></span> </a>
              <% } %>
						</div>
						<a title="<%=helper.getString("look.home.faq.more")%>" href="<%=URLManager.getSimpleURL(URLManager.URL_COMPONENT, faq.getQuestion().getInstanceId()) %>" class="link-more"><span><%=helper.getString(
                "look.home.faq.more")%></span> </a>
					</div>
					<% } %>
					
          <% if (showEphemeris) { %>
					<div class="secteur-container weather" id="weather-home">
					  <h4><span class="title"><%=helper.getString("look.home.weather.title") %></span><span class="date-today"><%=mainDateFormat.format(new Date())%></span></h4>
					  <div id="ephemeride">Brigitte</div>
            <% if (showWeather) { %>
				      <div id="localisation-weather"> <span class="label">&Agrave; : </span>
				      <%
				        boolean firstCity = true;
				      	for (City city : cities) { %>
				      		<% if (!firstCity) { %>
				      			/ 
				      		<% } %>
				      		<a class="select" id="<%=city.getWoeid() %>" href="#" onclick="javascript:showWeather(<%=city.getWoeid()%>);return false;"><%=city.getLabel() %></a>
				      <%	firstCity = false; 
				      	} %>
				      </div>
				      <div class="day" id="day1"> <img alt="soleil et nuage" src="imgDesign/meteo/meteo_44.png" />
				        <div class="temperature"><span class="min">min 22&deg;</span> <br />
				          <span class="max">max 32&deg;</span> </div>
				        <div class="label"><%=helper.getString("look.home.weather.today") %></div>
				      </div>
				      <div class="day" id="day2"> <img alt="soleil et nuage" src="imgDesign/meteo/meteo_44.png" />
				        <div class="temperature"><span class="min">min ??&deg;</span> <br />
				          <span class="max">max ??&deg;</span> </div>
				        <div class="label"><%=helper.getString("look.home.weather.tomorrow") %></div>
				      </div>
            <% } %>
          </div>
          <% } %>
					
					<% if (helper.displaySearchOnHome()) { %>
					<div class="secteur-container search" id="bloc-advancedSeach">
				      <h4><%=helper.getString("look.home.search.title") %></h4>
				      <form method="post" action="<%=m_sContext%>/RpdcSearch/jsp/AdvancedSearch" name="AdvancedSearch">
				        <input type="text" id="query" value="" size="60" name="query" onkeypress="checkEnter(event)" autocomplete="off" class="ac_input"/>
				        <input type="hidden" name="AxisValueCouples"/><input type="hidden" name="mode" value="clear"/>
				        <fieldset id="used_pdc" class="skinFieldset"></fieldset>
				        <a id="submit-AdvancedSearch" href="javascript:search()"><span><%=helper.getString("look.home.search.button") %></span></a>
				      </form>
				    </div>
				    <% } %>
    
    				<% if (bookmarks != null && !bookmarks.isEmpty()) { %>
				   <div class="secteur-container user-favorit" id="user-favorit-home">
						<h4><%=helper.getString("look.home.bookmarks.title")%></h4>
						<div class="user-favorit-main-container">
							<ul class="user-favorit-list">
								<% for (int i=0; i<bookmarks.size(); i++) {
								  LinkDetail bookmark = bookmarks.get(i);
								  String classFrag = "class=\"main-bookmark\"";
								  if (i > 4) {
								    classFrag = "class=\"other-bookmark\"";
								  }
								  String bookmarkUrl = bookmark.getUrl();
								  String target = "_blank";
								  if (!bookmarkUrl.toLowerCase().startsWith("http")) {
									bookmarkUrl = m_sContext + bookmarkUrl;
									target = "";
								  }
								%>
								<li <%=classFrag%>><a href="<%= bookmarkUrl%>" target="<%=target%>"><%=bookmark.getName() %></a></li>
								<% } %>
							</ul>
						</div>
						<a title="<%=helper.getString("look.home.bookmarks.more")%>" href="#" class="link-more" onclick="toggleBookmarks();return false;"><span><%=helper.getString("look.home.bookmarks.more")%></span> </a>
					</div>
					<% } %>
				   
               </div>
                
                <div class="principal-main-container">     
				
				<% if (shortcuts != null && !shortcuts.isEmpty()) { %>
				<div class="secteur-container cg-favorit" id="cg-favorit-home">
					<h4><%=helper.getString("look.home.shortcuts")%></h4>
					<div class="cg-favorit-main-container">
						<ul class="cg-favorit-list">
							<% for (Shortcut shortcut : shortcuts) { %>
							   <li><a href="<%=shortcut.getUrl() %>" title="<%=shortcut.getAltText() %>" target="<%=shortcut.getTarget() %>"><img alt="<%=shortcut.getAltText() %>" src="<%=shortcut.getIconURL() %>" /> <span><%=shortcut.getAltText() %></span></a></li>
							<% } %>	
						</ul>
					</div>
				</div>
				<% } %>

				  <% if (listOfNews != null && !listOfNews.isEmpty()) { %>
                <div id="carrousel-actualite">
				  <ul class="rslides" id="slider">
					<% for (News news : listOfNews) { %>
						<li>
						  <a href="<%=news.getPermalink()%>">
							  <view:image src="<%=news.getPublication().getThumbnail().getURL() %>" alt="" size="<%=newsSize%>"/>
						  </a>
						  <div class="caption">
							<h2><a href="<%=news.getPermalink()%>"><%=news.getTitle() %></a></h2>
							<p><%=news.getDescription() %></p>
						  </div>
						</li>
					<% } %>
				  </ul>
				</div>
				<% } %>
				
                <div id="last-publication-home" class="secteur-container">
		          <h4><%=helper.getString("look.home.publications.title")%></h4>
		            <div id="last-publicationt-main-container">
		              <ul class="last-publication-list">
		              	<% for (PublicationDetail publication : helper.getDernieresPublications()) { %>
		              		<li onclick="location.href='<%=URLManager.getSimpleURL(URLManager.URL_PUBLI, publication.getId())%>'">
			                  <a href="<%=URLManager.getSimpleURL(URLManager.URL_PUBLI, publication.getId())%>"><%=publication.getName() %></a>
			                  <view:username userId="<%=publication.getUpdaterId() %>" />
			                  <span class="date-publication"><view:formatDate value="<%=publication.getUpdateDate() %>"/></span>
			                  <p class="description-publication"><%=publication.getDescription() %></p>
			                </li>
		              	<% } %>
		              </ul>
		           </div>
     
    	</div>
            </div> <!-- #main -->
        </div>
</body>
</html>
