<%@page import="com.stratelia.webactiv.beans.admin.ComponentInstLight"%>
<%@page import="org.silverpeas.looks.aurora.BannerMainItem"%>
<%@page import="org.silverpeas.looks.aurora.Project"%>
<%@page import="com.stratelia.webactiv.beans.admin.ComponentInst"%>
<%@page import="org.silverpeas.looks.aurora.LookAuroraHelper"%>
<%@ include file="../../admin/jsp/importFrameSet.jsp" %>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="com.stratelia.webactiv.beans.admin.SpaceInstLight"%>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@ page import="java.util.List"%>
<%@ page import="com.silverpeas.util.StringUtil"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>
<%
LookAuroraHelper 	helper 	= (LookAuroraHelper) session.getAttribute("Silverpeas_LookHelper");
String currentHeading = helper.getSpaceId();

String wallPaper = helper.getSpaceWallPaper();
if (wallPaper == null) {
  wallPaper = gef.getIcon("banner.wallPaper");
}
if (wallPaper == null) {
  wallPaper = "imgDesign/bandeau.jpg";
}

List<BannerMainItem> mainItems = helper.getBannerMainItems();

List<ComponentInst> apps = helper.getApplications();
List<Project> projects = helper.getProjects();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>bandeau</title>
<view:looknfeel/>
<view:includePlugin name="ticker" />
<link rel="stylesheet" href="css/normalize.min.css" />
<script type="text/javascript" src="<%=m_sContext%>/util/javaScript/lookV5/topBar.js"></script>
<script type="text/javascript" src="<%=m_sContext%>/util/javaScript/lookV5/connectedUsers.js"></script>
<script type="text/javascript">
function goToHome() {
	selectHeading('home');
    top.bottomFrame.location.href = "<%=helper.getLoginHomePage()%>";
}

function getContext() {
	return "<%=m_sContext%>";
}

function getDomainsBarPage() {
	return "DomainsBar.jsp";
}

function getTopBarPage() {
	return "TopBar.jsp";
}

function reloadTopBar() {
	//Silverpeas V4 compatibility
}

function selectHeading(id) {
	unselectHeadings();
	$('#'+id+" > div").addClass("selected");
}

function unselectHeadings() {
	$('#nav > ul > li > div').each(function(index) {
		$(this).removeClass("selected");
	});
}

function goToTool(url) {
	unselectHeadings();
	top.bottomFrame.location.href = url;
}

function goToApplication(url) {
	unselectHeadings();
	top.bottomFrame.location.href = url;
}

function goToProject(url) {
	unselectHeadings();
	top.bottomFrame.location.href = url;
}

function setConnectedUsers(nb) {
	if (nb <= 0) {
		$("#connectedUsers").hide();
	} else {
		var label = " <%=helper.getString("lookSilverpeasV5.connectedUsers")%>";
		if (nb == 1) {
		    label = " <%=helper.getString("lookSilverpeasV5.connectedUser")%>";
		}
		$("#connectedUsers").show();
		$("#connectedUsers a").text(nb + label);
	}
}

function searchEngine() {
    if (document.searchForm.query.value !== "") {
      document.searchForm.submit();
    }
}

$(document).ready(function() {
	<% if (StringUtil.isDefined(currentHeading)) { %>
		selectHeading('<%=currentHeading%>');
	<% } %>
	
	setConnectedUsers(<%=helper.getNBConnectedUsers()%>);
	
	$('#show-menu-spacePerso').hover(function() {
		 $('.spacePerso').show();
	}, function() {
	});
	
	$('.avatarName').hover(function() {
		
	}, function() {
		$('.spacePerso').hide();
	});
	
	$('#nav >  ul > li > div').hover(function() {
	
  if($(this).children('ul').text().length!=0) {
    				$('#nav').addClass('sousMenu');
    				$('.nav-niveau-2').hide();
    				$('#nav   ul  div').removeClass('hover');
    				$('#nav   ul  div').removeClass('simple-hover');
    				$(this).addClass('hover');
    				$(this).children('ul').show();
    			}else {
    				$('#nav').removeClass('sousMenu');
    				$('#nav   ul  div').removeClass('simple-hover');
    				$('#nav   ul  div').removeClass('hover');
    				$('.nav-niveau-2').hide();
    				
    				$(this).addClass('simple-hover');
    			}
			
	}, function() {
		$('#nav').removeClass('sousMenu');
		$('#nav   ul  div').removeClass('hover');
		$('.nav-niveau-2').hide();
		$('#nav   ul  div').removeClass('simple-hover');
	});
	
	$('#top').hover(function() {
			
	}, function() {
		$('#nav').removeClass('sousMenu');
		$('#nav   ul  div').removeClass('hover');
		$('.nav-niveau-2').hide();
		$('#nav   ul  div').removeClass('simple-hover');
	});
	
	$.getJSON("<%=m_sContext%>/PersonalSpace?Action=GetTools&IEFix="+new Date().getTime(),
			function(data){
				try {
					// display tools
					for (var i = 0; i < data.length; ++i) {
		                var tool = data[i];
		                if (tool.id === "notification") {
		                	if (tool.nb === 0) {
		                		$("#notification-count").hide();
		                	} else {
		                		$("#notification-count span").text(tool.nb);
		                		if (tool.nb === 1) {
		                			$("#notification-label").text("notification");
		                		} else {
		                			$("#notification-label").text("notifications");
		                		}
		                	}
		                }
					}
				} catch (e) {
					//do nothing
					alert(e);
				}
			});

});
</script>
<style>
body {
	background-image: url(<%=wallPaper%>);
}
</style>
</head>
<body id="top">
<viewTags:displayTicker/>
<div class="header-container">
  <div  class="wrapper clearfix">
    <h1 class="title">Intranet</h1>
    <a id="logo-header" href="#" onclick="javaScript:goToHome();"> <img alt="" src="<%=helper.getSettings("logo", "icons/1px.gif") %>" /> </a>
    <div id="topar-header">
      <div id="infoConnection"> <a target="bottomFrame" href="frameBottom.jsp?FromMySpace=1"><view:image type="avatar" id="avatar-img" alt="mon avatar" src="<%=helper.getUserDetail().getAvatar()%>" /></a>
        <div class="avatarName">
          <div class="btn-header">	
          	<a title="<%=helper.getString("look.banner.profile.title")%>" target="bottomFrame" href="frameBottom.jsp?FromMySpace=1"><%=helper.getUserFullName() %></a> 
            <a id="show-menu-spacePerso" href="#"> Mon espace perso</a>
          </div>
          <div class="spacePerso">
            <ul>
              <li><a id="link-settings" target="bottomFrame" href="/silverpeas/RMyProfil/jsp/MySettings"><%=helper.getString("look.banner.profile.settings") %></a> </li>
              <li><a id="link-myspace" target="bottomFrame" href="frameBottom.jsp?FromMySpace=1"><%=helper.getString("look.banner.profile.myspace") %></a></li>
              <li><a id="link-feed" target="bottomFrame" href="/silverpeas/RMyProfil/jsp/Main"><%=helper.getString("look.banner.profile.feed") %></a></li>
              <li><a id="link-logout" id="logOut-link" target="_top" href="/silverpeas/LogoutServlet"><%=helper.getString("look.banner.logout") %></a> </li>
            </ul>
          </div>
        </div>
        
        <div id="notification-count" class="btn-header"> <a href="<%=m_sContext %>/RSILVERMAIL/jsp/Main" target="bottomFrame"><span>...</span> <span id="notification-label">notifications</span></a> </div>

        <% if (projects != null && !projects.isEmpty()) { %>
        <div class="btn-header">
          <label class="select-header">
            <select id="project-select" onchange="goToProject(this.value)">
              <option selected="selected"><%=helper.getString("look.banner.projects") %></option>
              <% for (Project project : helper.getProjects()) { %>
              <option value="<%=URLManager.getSimpleURL(URLManager.URL_SPACE, project.getSpace().getShortId()) %>"><%=project.getName() %></option>
              <% } %>
            </select>
          </label>
        </div>
        <% } %>

        <% if (apps != null && !apps.isEmpty()) { %>
        <div class="btn-header">
          <label class="select-header">
            <select id="application-select" onchange="goToApplication(this.value)">
              <option selected="selected"><%=helper.getString("look.banner.applications") %></option>
              <% for (ComponentInst app : helper.getApplications()) { %>
              <option value="<%=URLManager.getApplicationURL()+URLManager.getURL(app.getName(), "", app.getId()) %>Main"><%=app.getLabel() %></option>
              <% } %>
            </select>
          </label>
        </div>
        <% } %>
      </div>
      <ul id="outils">
        <% if (helper.getSettings("displayConnectedUsers", true)) { %>
        <li id="connectedUsers"><a onclick="openConnectedUsers();" href="#">2 autres utilisateurs connectés, </a></li>
        <% } %>
        <li id="map-link-header"><a target="bottomFrame" href="/silverpeas/admin/jsp/Map.jsp" title="<%=helper.getString("lookSilverpeasV5.Map") %>"><%=helper.getString("lookSilverpeasV5.Map") %></a></li>
        <li id="help-link-header"><a target="_blank" href="/help_fr/index.html" title="<%=helper.getString("lookSilverpeasV5.Help") %>"><%=helper.getString("lookSilverpeasV5.Help") %></a></li>
        <li id="directory-link-header"><a target="bottomFrame" href="/silverpeas/Rdirectory/jsp/Main?Sort=NEWEST" title="<%=helper.getString("look.banner.directory") %>"><%=helper.getString("look.banner.directory") %></a></li>
        <% if(helper.isBackOfficeVisible()) { %>
        <li id="adminstration-link-header"> <a target="_top" href="/silverpeas/RjobManagerPeas/jsp/Main"><%=helper.getString("lookSilverpeasV5.backOffice") %></a></li>
        <% } %>
      </ul>
      <div id="search-zone-header">
        <form id="search-form-header" target="bottomFrame" method="post" action="/silverpeas/RpdcSearch/jsp/AdvancedSearch" name="searchForm">
          <label for="query">Recherche rapide</label>
          <input id="query" size="30" name="query" autocomplete="off" class="ac_input" />
          <input type="hidden" value="clear" name="mode"/>
          <a href="javascript:searchEngine()">Go</a>
        </form>
        <a id="lastResult-link-header" target="bottomFrame" href="<%=m_sContext%>/RpdcSearch/jsp/LastResults"><span><%=helper.getString("lookSilverpeasV5.LastSearchResults") %></span></a>
        <a id="advancedSearch-link-header" target="bottomFrame" href="<%=m_sContext%>/RpdcSearch/jsp/ChangeSearchTypeToExpert"><span><%=helper.getString("lookSilverpeasV5.AdvancedSearch") %> </span></a>
      </div>
    </div>
    <div id="nav">
      <ul>
      	<li>
        	<div class="selected"> <a href="#" onclick="javaScript:goToHome();"><span><%=helper.getString("look.banner.home") %></span></a> </div>
        </li>
        <% for (BannerMainItem item : mainItems) { %>
        <li id="<%=item.getSpace().getFullId()%>"> 
        	<div>
        		<a href="frameBottom.jsp?SpaceId=<%=item.getSpace().getFullId()%>" target="bottomFrame" onclick="javascript:selectHeading('<%=item.getSpace().getFullId()%>')"><span><%=item.getSpace().getName(helper.getLanguage()) %></span></a>
         		 <ul class="nav-niveau-2 nav-<%=item.getNumberOfColumns()%>-column">
					<% if (item.getSubspaces() != null) { %>
	                    <% for (SpaceInstLight subspace : item.getSubspaces()) { %>
	                    	<li class="space"><a href="frameBottom.jsp?SpaceId=<%=subspace.getFullId()%>" target="bottomFrame"><span><%=subspace.getName(helper.getLanguage())%></span></a></li>
	                    <% } %>
                    <% } %>
                    <% if (item.getApps() != null) { %>
	                    <% for (ComponentInstLight app : item.getApps()) { %>
	                   		<li><a href="<%=URLManager.getSimpleURL(URLManager.URL_COMPONENT, app.getId())%>" target="bottomFrame"><span><%=app.getName(helper.getLanguage())%></span></a></li>
	                    <% } %>
                    <% } %>
         		</ul>
             </div>
        </li>
        <% } %>
      </ul>
    </div>
    <div id="deco-header"> </div>
  </div>
</div>
</body>
</html>