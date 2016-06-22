<%@page import="com.stratelia.webactiv.util.DateUtil"%>
<%@page import="com.stratelia.webactiv.almanach.model.EventOccurrence"%>
<%@page import="java.util.Locale"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="com.silverpeas.sdis84.look.NextEventsDate"%>
<%@page import="com.silverpeas.sdis84.look.Heading"%>
<%@page import="com.silverpeas.sdis84.look.LookSDIS84Helper"%>

<%@ page import="com.stratelia.webactiv.beans.admin.SpaceInstLight"%>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@ page import="java.util.List"%>
<%@page import="com.silverpeas.util.StringUtil"%>
<%@page import="com.silverpeas.look.Shortcut"%>
<%@page import="com.stratelia.webactiv.util.publication.model.PublicationDetail"%>

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%
String spaceId = request.getParameter("SpaceId");

LookSDIS84Helper helper = (LookSDIS84Helper) session.getAttribute("Silverpeas_LookHelper");

Heading heading = helper.getHeading(spaceId);

SimpleDateFormat nextEventsDateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH);
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>SDIS84</title>
<view:looknfeel/>
<style>
html, body {
	height:100%;
	min-height:644px;
}
</style>
</head>
<body id="rubrique">
<div class="content">
  <div id="multiple-access">
    <a href="<%=heading.getImageURL() %>" target="_blank"><img src="<%=heading.getImageURL() %>" class="imgDeco"/></a>
    <div id="fast-access">
      <% if (heading.getShortcuts() != null && !heading.getShortcuts().isEmpty()) { %>
      <h3 class="title"><%=helper.getString("look.heading.shortcuts") %></h3>
      <ul>
      	<% for (Shortcut shortcut : heading.getShortcuts()) { %>
      		<li><a style="background-image:url(<%=shortcut.getIconURL() %>);" href="<%=shortcut.getUrl()%>" target="<%=shortcut.getTarget()%>"><span><%=shortcut.getAltText() %></span></a></li>
      	<% } %>
      </ul>
      <% } %>
    </div> 
  </div>
  
  <div id="presentation">
  	<h2 class="title"><%=heading.getTitle(helper.getString("look.heading.title.default")) %>
  	<% if (heading.isAdmin()) { %>
  		<a href="<%=heading.getBackOfficeAppURL()%>" title="Modifier cette page d'accueil"><img src="<%=URLManager.getApplicationURL() %>/util/icons/update.gif" alt="Modifier"/></a>
  	<% } %>
  	</h2>
    <div class="content-wysiwyg">
      <p>
      	<%=heading.getEdito(helper.getString("look.heading.edito.default")) %>
      </p>
    </div>
  </div>
  
  <div id="second-part">
  	  <% if (heading.getNextEventsDate() != null && !heading.getNextEventsDate().isEmpty()) { %>
      <div id="next-event">
        <h3 class="title"><%=helper.getString("look.heading.events.next") %></h3>
        <ul>
          <% for (int i=0; i<=1 && i < heading.getNextEventsDate().size(); i++) {
    	  NextEventsDate date = heading.getNextEventsDate().get(i);
    		%>
    	  <li>
	        <div class="date-events"><%=nextEventsDateFormat.format(date.date) %></div>
	        <% for (EventOccurrence event : date.events) { %>
	        	<span class="clock-events">
	        	<% if (StringUtil.isDefined(event.getEventDetail().getStartHour())) { %>
					<%=event.getEventDetail().getStartHour()%>
				<% } %>
				<% if (StringUtil.isDefined(event.getEventDetail().getStartHour()) && StringUtil.isDefined(event.getEventDetail().getEndHour()) && !event.getEventDetail().getEndHour().equals(event.getEventDetail().getStartHour())) { %>
					 - 
				<% } %>
				<% if (StringUtil.isDefined(event.getEventDetail().getEndHour()) && !event.getEventDetail().getEndHour().equals(event.getEventDetail().getStartHour())) { %>
					 <%=event.getEventDetail().getEndHour()%>
				<% } %>
	        	</span> <a href="<%=event.getEventDetail().getPermalink()%>"><%=event.getEventDetail().getName() %> </a> 
	        <% } %>
	      </li>
    	<% } %>
        </ul>
      </div>
      <% } %>
    <div id="last-publication">
        <h3 class="title"><%=helper.getString("look.heading.publications") %></h3>
      <p>
      <% for (PublicationDetail publication : heading.getLastPublications()) { %>
      <a href="<%=URLManager.getSimpleURL(URLManager.URL_PUBLI, publication.getId())%>"><strong><%=publication.getName() %></strong></a> <br />
          <view:username userId="<%=publication.getUpdaterId() %>"/> - <%=DateUtil.getOutputDate(publication.getUpdateDate(), "fr") %> <br />
        <br />
      <% } %>
        </p>
   	  <a href="latestPublis.jsp?SpaceId=<%=spaceId %>" class="link-more"><%=helper.getString("look.heading.publications.more") %></a>
      </div>
   </div>
</div>
</body>
</html>