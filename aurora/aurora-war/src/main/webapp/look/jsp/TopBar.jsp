<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>

<c:set var="lookHelper" value="${sessionScope.Silverpeas_LookHelper}" />
<c:set var="currentHeading" value="${lookHelper.spaceId}"/>
<c:set var="mainItems" value="${lookHelper.bannerMainItems}"/>
<c:set var="apps" value="${lookHelper.applications}"/>
<c:set var="projects" value="${lookHelper.projects}"/>
<c:set var="settings" value="${lookHelper.lookSettings}"/>
<c:set var="directoryURL" value="${settings.directoryURL}"/>

<view:setBundle bundle="${lookHelper.localizedBundle}"/>

<fmt:message var="labelConnectedUser" key="look.banner.connected.user.one"/>
<fmt:message var="labelConnectedUsers" key="look.banner.connected.user.many"/>

<fmt:message var="labelProfile" key="look.banner.profile.title"/>
<fmt:message var="labelProfileSettings" key="look.banner.profile.settings"/>
<fmt:message var="labelProfileMySpace" key="look.banner.profile.myspace"/>
<fmt:message var="labelProfileMyFeed" key="look.banner.profile.feed"/>

<fmt:message var="labelLogout" key="look.banner.logout"/>
<fmt:message var="labelProjects" key="look.banner.projects"/>
<fmt:message var="labelApplications" key="look.banner.applications"/>

<fmt:message var="labelHome" key="look.banner.home"/>
<fmt:message var="labelMap" key="look.banner.map"/>
<fmt:message var="labelHelp" key="look.banner.help"/>
<fmt:message var="labelDirectory" key="look.banner.directory"/>
<fmt:message var="labelBackoffice" key="look.banner.backoffice"/>

<fmt:message var="labelSearch" key="look.banner.search"/>
<fmt:message var="labelSearchAdvanced" key="look.banner.search.advanced"/>
<fmt:message var="labelSearchResults" key="look.banner.search.lastresults"/>

<fmt:message var="labelUserNotifications" key="look.banner.notifications"/>
<fmt:message var="labelUnreadUserNotification" key="look.banner.notifications.unread.one"/>
<fmt:message var="labelUnreadUserNotifications" key="look.banner.notifications.unread.many"/>

<c:url var="urlAdmin" value="/RjobManagerPeas/jsp/Main"/>

<c:set var="smartmenusSkin" value="sm-silverpeas"/>

<view:includePlugin name="userSession"/>
<view:includePlugin name="userNotification"/>
<view:includePlugin name="ticker" />

<view:loadScript src="js/jquery.smartmenus.min.js" jsPromiseName="smartMenuPromise"/>
<link href="css/sm-core-css.css" rel="stylesheet" type="text/css" />
<link href='css/${smartmenusSkin}/${smartmenusSkin}.css' rel='stylesheet' type='text/css' />
<script type="text/javascript">
function goToHome() {
	selectHeading('home');
  var params = {};
  params.Login = "1";
  params.FromTopBar = "1";
  spLayout.getBody().load(params);
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
	$('#space-'+id).addClass("selected");
}

function unselectHeadings() {
	$('#nav > ul > li').each(function(index) {
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
  changeBody(webContext+url);
}

function goToProject(projectSpaceId) {
	unselectHeadings();
  goToSpace(projectSpaceId)
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
  var url = sp.formatUrl(webContext+"/RpdcSearch/jsp/" + action, urlParameters);
  spLayout.getBody().getContent().load(url);
}

$(document).ready(function() {
  <c:if test="${silfn:isDefined(currentHeading)}">
    selectHeading('${currentHeading}');
  </c:if>

  $('#show-menu-spacePerso').hover(function() {
		 $('.spacePerso').show();
	}, function() {
	});
	
	$('.avatarName').hover(function() {
		
	}, function() {
		$('.spacePerso').hide();
	});

	<c:if test="${settings.displayMenuSubElements}">
  smartMenuPromise.then(function() {
    $('#main-menu').smartmenus({
      showOnClick : false
    });
  });
  </c:if>
});

window.USERNOTIFICATION_PROMISE.then(function() {
  var $container = jQuery("#notification-count");
  spUserNotification.addEventListener('unreadUserNotificationsChanged', function(event) {
    var unreadUserNotificationCount = event.detail.data.nbUnread;
    $container.show();
    var label = unreadUserNotificationCount + " ${labelUnreadUserNotifications}";
    if (unreadUserNotificationCount === 1) {
      label = unreadUserNotificationCount + " ${labelUnreadUserNotification}";
    } else if (unreadUserNotificationCount === 0) {
      label = "${labelUserNotifications}";
      $container.hide();
    }
    jQuery("a", $container).text(label);
  }, 'unreadUserNotificationsChanged@TopBar');
});


window.USERSESSION_PROMISE.then(function() {
  spUserSession.addEventListener('connectedUsersChanged', function(event) {
    var nb = event.detail.data.nb;
    var $container = jQuery("#connectedUsers");
    if (nb <= 0) {
      $container.hide();
    } else {
      var label = " ${labelConnectedUsers}";
      if (nb === 1) {
        label = " ${labelConnectedUser}";
      }
      $container.show();
      jQuery("a", $container).text(nb + label);
    }
  });
});
</script>
<viewTags:displayTicker/>
<div class="header-container">
  <div class="wrapper clearfix">
    <h1 class="title">Intranet</h1>
    <a id="logo-header" href="#" onclick="javascript:goToHome();"> <img alt="" src="${settings.logo}" /> </a>
    <div id="topar-header">
      <div id="infoConnection"> <a href="javascript:goToPersonalSpace()"><view:image type="avatar" id="avatar-img" alt="mon avatar" src="${lookHelper.userDetail.avatar}" /></a>
        <div class="avatarName">
          <div class="btn-header">	
          	<a title="${labelProfile}" href="javascript:goToPersonalSpace()">${lookHelper.userFullName}</a>
            <a id="show-menu-spacePerso" href="#"> Mon espace perso</a>
          </div>
          <div class="spacePerso">
            <ul>
              <li><a id="link-settings" href="javascript:changeBody(webContext+'/RMyProfil/jsp/MySettings')">${labelProfileSettings}</a> </li>
              <li><a id="link-myspace" href="javascript:goToPersonalSpace()">${labelProfileMySpace}</a></li>
              <li><a id="link-feed" href="javascript:changeBody(webContext+'/RMyProfil/jsp/Main')">${labelProfileMyFeed}</a></li>
              <li><a id="link-logout" id="logOut-link" href="javascript:onClick=spUserSession.logout();">${labelLogout}</a> </li>
            </ul>
          </div>
        </div>
        
        <div id="notification-count" class="btn-header"> <a href="javascript:changeBody(webContext+'/RSILVERMAIL/jsp/Main')"></a> </div>

        <c:if test="${not empty projects}">
        <div class="btn-header">
          <label class="select-header">
            <select id="project-select" onchange="goToProject(this.value)">
              <option selected="selected" value="">${labelProjects}</option>
              <c:forEach var="project" items="${projects}">
                <option value="${project.space.id}">${project.name}</option>
              </c:forEach>
            </select>
          </label>
        </div>
        </c:if>

        <c:if test="${not empty apps}">
        <div class="btn-header">
          <label class="select-header">
            <select id="application-select" onchange="goToApplication(this.value)">
              <option selected="selected" value="">${labelApplications}</option>
              <c:forEach var="app" items="${apps}">
                <option value="${app.internalLink}">${app.label}</option>
              </c:forEach>
            </select>
          </label>
        </div>
        </c:if>
      </div>
      <ul id="outils">
        <c:if test="${settings.displayConnectedUsers}">
          <li id="connectedUsers"><a onclick="javascript:onClick=spUserSession.viewConnectedUsers();" href="#"></a></li>
        </c:if>
        <li id="map-link-header"><a href="javascript:changeBody(webContext+'/admin/jsp/Map.jsp')" title="${labelMap}">${labelMap}</a></li>
        <li id="help-link-header"><a target="_blank" href="${settings.helpURL}" title="${labelHelp}">${labelHelp}</a></li>
        <c:if test="${silfn:isDefined(directoryURL)}">
          <li id="directory-link-header"><a href="javascript:changeBody('${directoryURL}')" title="${labelDirectory}">${labelDirectory}</a></li>
        </c:if>
        <c:if test="${lookHelper.backOfficeVisible}">
          <li id="adminstration-link-header"> <a target="_top" href="${urlAdmin}">${labelBackoffice}</a></li>
        </c:if>
      </ul>
      <div id="search-zone-header">
        <form id="search-form-header" method="get" action="javascript:searchEngine()" name="searchForm">
          <label for="query">${labelSearch}</label>
          <input id="query" size="30" name="query" />
          <input type="hidden" value="clear" name="mode"/>
          <a href="javascript:searchEngine()">Go</a>
        </form>
        <a id="lastResult-link-header" href="javascript:lastResultsSearchEngine()"><span>${labelSearchResults}</span></a>
        <a id="advancedSearch-link-header" href="javascript:advancedSearchEngine()"><span>${labelSearchAdvanced}</span></a>
      </div>
    </div>
    <div id="nav">
      <ul id="main-menu" class="sm ${smartmenusSkin} displayMenuAppIcons-${settings.displayMenuAppIcons}">
      	<c:if test="${lookHelper.userCanDisplayMainHomePage}">
          <li class="selected"><a href="javascript:goToHome();"><span>${labelHome}</span></a></li>
        </c:if>

        <c:forEach var="item" items="${mainItems}">
          <view:map spaceId="${item.space.id}" displayAppsFirst="${settings.displayMenuAppsFirst}" displayAppIcon="${settings.displayMenuAppIcons}" callbackJSForMainSpace="goToMainSpace" callbackJSForSubspaces="goToSpace" callbackJSForApps="goToSpaceApp"/>
        </c:forEach>
      </ul>
    </div>
    <div id="deco-header"> </div>
  </div>
</div>