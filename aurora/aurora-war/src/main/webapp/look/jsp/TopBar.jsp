<%@page import="org.silverpeas.looks.aurora.BannerMainItem"%>
<%@page import="org.silverpeas.looks.aurora.Project"%>
<%@page import="org.silverpeas.looks.aurora.LookAuroraHelper"%>
<%@ include file="../../admin/jsp/importFrameSet.jsp" %>
<%@ page import="java.util.List"%>
<%@ page import="org.silverpeas.core.admin.component.model.ComponentInst" %>
<%@ page import="org.silverpeas.core.util.StringUtil" %>
<%@ page import="org.silverpeas.core.util.URLUtil" %>
<%@ page import="org.silverpeas.core.admin.space.SpaceInstLight" %>
<%@ page import="org.silverpeas.core.admin.component.model.ComponentInstLight" %>
<%@ page import="org.silverpeas.core.web.look.LookHelper" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>

<c:set var="curHelper" value="${sessionScope.Silverpeas_LookHelper}" />
<%
LookAuroraHelper 	helper 	= (LookAuroraHelper) LookHelper.getLookHelper(session);
String currentHeading = helper.getSpaceId();

List<BannerMainItem> mainItems = helper.getBannerMainItems();

List<ComponentInst> apps = helper.getApplications();
List<Project> projects = helper.getProjects();
String directoryURL = helper.getSettings("directoryURL", null);
%>

<view:includePlugin name="ticker" />
<link rel="stylesheet" href="css/normalize.min.css" />
<view:script src="/util/javaScript/lookV5/connectedUsers.js"/>
<script type="text/javascript">
function goToHome() {
	selectHeading('home');
  var params = {};
  params.Login = "1";
  params.FromTopBar = "1";
  spLayout.getBody().load(params);
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
	$('#navMainItem-'+id+" > div").addClass("selected");
}

function unselectHeadings() {
	$('#nav > ul > li > div').each(function(index) {
		$(this).removeClass("selected");
	});
}

function goToMainSpace(id) {
  selectHeading(id);
  goToSpace(id);
}

function goToSpace(id) {
  var params = {};
  params.SpaceId = id;
  spLayout.getBody().load(params);
}

function goToSpaceApp(id) {
  var params = {};
  params.ComponentId = id;
  spLayout.getBody().load(params);
}

function changeBody(url) {
  if (StringUtil.isDefined(url)) {
    spLayout.getBody().getContent().load(url);
  }
}

function goToPersonalSpace() {
  var params = {};
  params.FromMySpace = '1';
  spLayout.getBody().load(params);
}

function goToApplication(url) {
	unselectHeadings();
  changeBody(url);
}

function goToProject(projectSpaceId) {
	unselectHeadings();
  goToSpace(projectSpaceId)
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
    executeSearchActionToBodyPartTarget("AdvancedSearch", true);
  }
}

function advancedSearchEngine(){
  executeSearchActionToBodyPartTarget("ChangeSearchTypeToExpert", true);
}

function lastResultsSearchEngine(){
  executeSearchActionToBodyPartTarget("LastResults", false);
}

function executeSearchActionToBodyPartTarget(action, hasToSerializeForm) {
  var urlParameters = hasToSerializeForm ?
      jQuery(document.searchForm).serializeFormJSON() : {};
  var url = sp.formatUrl("<%=m_sContext%>/RpdcSearch/jsp/" + action, urlParameters);
  spLayout.getBody().getContent().load(url);
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

	<% if (helper.getSettings("banner.subElements", true)) { %>

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

	<% } %>
	
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
					alert("ici "+e);
				}
			});

});
</script>
<viewTags:displayTicker/>
<div class="header-container">
  <div class="wrapper clearfix">
    <h1 class="title">Intranet</h1>
    <a id="logo-header" href="#" onclick="javascript:goToHome();"> <img alt="" src="<%=helper.getSettings("logo", "icons/1px.gif") %>" /> </a>
    <div id="topar-header">
      <div id="infoConnection"> <a href="javascript:goToPersonalSpace()"><view:image type="avatar" id="avatar-img" alt="mon avatar" src="<%=helper.getUserDetail().getAvatar()%>" /></a>
        <div class="avatarName">
          <div class="btn-header">	
          	<a title="<%=helper.getString("look.banner.profile.title")%>" href="javascript:goToPersonalSpace()"><%=helper.getUserFullName() %></a>
            <a id="show-menu-spacePerso" href="#"> Mon espace perso</a>
          </div>
          <div class="spacePerso">
            <ul>
              <li><a id="link-settings" href="javascript:changeBody('/silverpeas/RMyProfil/jsp/MySettings')"><%=helper.getString("look.banner.profile.settings") %></a> </li>
              <li><a id="link-myspace" href="javascript:goToPersonalSpace()"><%=helper.getString("look.banner.profile.myspace") %></a></li>
              <li><a id="link-feed" href="javascript:changeBody('/silverpeas/RMyProfil/jsp/Main')"><%=helper.getString("look.banner.profile.feed") %></a></li>
              <li><a id="link-logout" id="logOut-link" target="_top" href="/silverpeas/LogoutServlet"><%=helper.getString("look.banner.logout") %></a> </li>
            </ul>
          </div>
        </div>
        
        <div id="notification-count" class="btn-header"> <a href="javascript:changeBody('<%=m_sContext %>/RSILVERMAIL/jsp/Main')"><span>...</span> <span id="notification-label">notifications</span></a> </div>

        <% if (projects != null && !projects.isEmpty()) { %>
        <div class="btn-header">
          <label class="select-header">
            <select id="project-select" onchange="goToProject(this.value)">
              <option selected="selected" value=""><%=helper.getString("look.banner.projects") %></option>
              <% for (Project project : helper.getProjects()) { %>
              <option value="<%=project.getSpace().getId() %>"><%=project.getName() %></option>
              <% } %>
            </select>
          </label>
        </div>
        <% } %>

        <% if (apps != null && !apps.isEmpty()) { %>
        <div class="btn-header">
          <label class="select-header">
            <select id="application-select" onchange="goToApplication(this.value)">
              <option selected="selected" value=""><%=helper.getString("look.banner.applications") %></option>
              <% for (ComponentInst app : helper.getApplications()) { %>
              <option value="<%=URLUtil.getApplicationURL()+URLUtil.getURL(app.getName(), "", app.getId()) %>Main"><%=app.getLabel() %></option>
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
        <li id="map-link-header"><a href="javascript:changeBody('/silverpeas/admin/jsp/Map.jsp')" title="<%=helper.getString("lookSilverpeasV5.Map") %>"><%=helper.getString("lookSilverpeasV5.Map") %></a></li>
        <li id="help-link-header"><a target="_blank" href="<%=helper.getSettings("helpURL", "https://extranet.silverpeas.com/help_fr/")%>" title="<%=helper.getString("lookSilverpeasV5.Help") %>"><%=helper.getString("lookSilverpeasV5.Help") %></a></li>
        <% if (StringUtil.isDefined(directoryURL)) { %>
          <li id="directory-link-header"><a href="javascript:changeBody('<%=directoryURL%>')" title="<%=helper.getString("look.banner.directory") %>"><%=helper.getString("look.banner.directory") %></a></li>
        <% } %>
        <% if(helper.isBackOfficeVisible()) { %>
        <li id="adminstration-link-header"> <a target="_top" href="/silverpeas/RjobManagerPeas/jsp/Main"><%=helper.getString("lookSilverpeasV5.backOffice") %></a></li>
        <% } %>
      </ul>
      <div id="search-zone-header">
        <form id="search-form-header" method="get" action="javascript:searchEngine()" name="searchForm">
          <label for="query"><%=helper.getString("look.banner.search") %></label>
          <input id="query" size="30" name="query" />
          <input type="hidden" value="clear" name="mode"/>
          <a href="javascript:searchEngine()">Go</a>
        </form>
        <a id="lastResult-link-header" href="javascript:lastResultsSearchEngine()"><span><%=helper.getString("lookSilverpeasV5.LastSearchResults") %></span></a>
        <a id="advancedSearch-link-header" href="javascript:advancedSearchEngine()"><span><%=helper.getString("lookSilverpeasV5.AdvancedSearch") %> </span></a>
      </div>
    </div>
    <div id="nav">
      <ul>
      	<li>
        	<div class="selected"> <a href="javascript:goToHome();"><span><%=helper.getString("look.banner.home") %></span></a> </div>
        </li>
        <% for (BannerMainItem item : mainItems) { %>
        <li id="navMainItem-<%=item.getSpace().getId()%>">
        	<div>
        		<a href="javascript:goToMainSpace('<%=item.getSpace().getId()%>')"><span><%=item.getSpace().getName(helper.getLanguage()) %></span></a>
         		 <ul class="nav-niveau-2 nav-<%=item.getNumberOfColumns()%>-column">
					<% if (item.getSubspaces() != null) { %>
	                    <% for (SpaceInstLight subspace : item.getSubspaces()) { %>
	                    	<li class="space"><a href="javascript:goToSpace('<%=subspace.getId()%>')"><span><%=subspace.getName(helper.getLanguage())%></span></a></li>
	                    <% } %>
                    <% } %>
                    <% if (item.getApps() != null) { %>
	                    <% for (ComponentInstLight app : item.getApps()) { %>
	                   		<li><a href="javascript:goToSpaceApp('<%=app.getId()%>')"><span><%=app.getName(helper.getLanguage())%></span></a></li>
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