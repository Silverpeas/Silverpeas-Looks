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
<c:set var="directoryURL" value="${lookHelper.directoryURL}"/>
<c:set var="directoryDomains" value="${lookHelper.directoryDomains}"/>
<c:set var="directoryDomainIds" value="${lookHelper.directoryDomainIds}"/>
<c:set var="directoryGroups" value="${lookHelper.directoryGroups}"/>

<c:set var="isAnonymous" value="${lookHelper.anonymousUser}"/>
<c:set var="isAccessGuest" value="${lookHelper.accessGuest}"/>
<c:set var="anonymousMode" value=""/>

<c:set var="shortcuts" value="${lookHelper.toolsShortcuts}"/>
<c:set var="extraJavascript" value="${settings.extraJavascriptForBanner}"/>

<c:if test="${isAnonymous}">
  <c:set var="anonymousMode" value="anonymousMode"/>
</c:if>
<c:url var="urlLogin" value="/Login"/>

<c:set var="isDisplayDirectory" value="${silfn:isDefined(directoryURL) && ((isAnonymous && settings.displayDirectoryForAnonymous) || not isAnonymous)}"/>

<c:choose>
<c:when test="${lookHelper == null or lookHelper.localizedBundle == null}">
  <script type="text/javascript">
    top.location = '${urlLogin}';
  </script>
</c:when>
<c:otherwise>

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
<fmt:message var="labelLogin" key="look.banner.login"/>

<fmt:message var="labelSearch" key="look.banner.search"/>
<fmt:message var="labelSearchButton" key="look.banner.search.button"/>
<fmt:message var="labelSearchPlaceholder" key="look.banner.search.input.placeholder"/>
<fmt:message var="labelSearchAdvanced" key="look.banner.search.advanced"/>
<fmt:message var="labelSearchResults" key="look.banner.search.lastresults"/>
<fmt:message var="labelSearchPlatform" key="look.banner.search.scope.platform"/>
<fmt:message var="labelSearchPlatformShort" key="look.banner.search.scope.platform.short"/>
<fmt:message var="labelSearchDirectory" key="look.banner.search.scope.directory"/>
<fmt:message var="labelSearchDirectoryShort" key="look.banner.search.scope.directory.short"/>

<fmt:message var="labelUserNotifications" key="look.banner.notifications"/>
<fmt:message var="labelUnreadUserNotification" key="look.banner.notifications.unread.one"/>
<fmt:message var="labelUnreadUserNotifications" key="look.banner.notifications.unread.many"/>

<c:set var="smartmenusSkin" value="sm-silverpeas"/>

<view:includePlugin name="userSession"/>
<view:includePlugin name="userNotification"/>
<view:includePlugin name="basketSelection"/>
<view:includePlugin name="ticker" />

<view:loadScript src="js/jquery.smartmenus.min.js" jsPromiseName="smartMenuPromise"/>
<link href="css/sm-core-css.css" rel="stylesheet" type="text/css" />
<link href='css/${smartmenusSkin}/${smartmenusSkin}.css' rel='stylesheet' type='text/css' />

<c:if test="${not empty extraJavascript}">
  <script type="text/javascript" src="${extraJavascript}"></script>
</c:if>

<script type="text/javascript">
function goToHome() {
  selectHeading('home');
  spWindow.loadHomePage({
    "FromTopBar" : '1'
  });
}

function getTopBarPage() {
	return "TopBar.jsp";
}

function selectHeading(id) {
	unselectHeadings();
	$('#'+id).addClass("selected");
}

function unselectHeadings() {
	$('#nav > ul > li').each(function(index) {
		$(this).removeClass("selected");
	});
}

function goToMainSpace(id) {
  selectHeading("space-"+id);
  spWindow.loadSpace(id);
}

function selectMainSpace(id) {
  $('#'+id).parents().map(function() {
    if ($(this).parent() && $(this).parent().attr("id") === "main-menu") {
      selectHeading($(this).attr("id"));
    }
  });
}

function goToSpace(id) {
  selectMainSpace("space-"+id);
  spWindow.loadSpace(id);
}

function goToSpaceApp(id) {
  selectMainSpace("app-"+id);
  spWindow.loadComponent(id);
}

function changeBody(url) {
  if (StringUtil.isDefined(url)) {
    unselectHeadings();
    spWindow.loadLink(webContext+url);
  }
}

function goToPersonalSpace() {
  unselectHeadings();
  spWindow.loadPersonalSpace();
}

function goToApplication(url) {
	unselectHeadings();
  changeBody(url);
	$("#application-select").val("");
}

function goToProject(projectSpaceId) {
	unselectHeadings();
  goToSpace(projectSpaceId);
  $("#project-select").val("");
}

function searchEngine() {
  if (document.searchForm.query.value !== "" || document.searchForm.queryDirectory.value !== "") {
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
  var urlParameters = hasToSerializeForm ? jQuery(document.searchForm).serializeFormJSON() : {};
  var url = "";
  if (searchScope === searchEngineScope) {
    url = sp.url.format(webContext + "/RpdcSearch/jsp/" + action, urlParameters);
  } else if (searchScope === directoryScope) {
    url = sp.url.format(webContext + "/Rdirectory/jsp/searchByKey", urlParameters);
  }
  spWindow.loadContent(url);
}

function jumpToUser(selectionUserAPI) {
  var userIds = selectionUserAPI.getSelectedUserIds();
  if (userIds.length) {
    var url = webContext+"/Rprofil/jsp/Main?userId="+userIds[0];
    spWindow.loadContent(url);
  }
}

$("#inputSearchSwitchable").keypress(function(e) {
  if (e.which == 13) {
    e.preventDefault();
    searchEngine();
    return false;
  }
  return true;
});

var searchEngineScope = "SearchEngineScope";
var directoryScope = "DirectoryScope";
var searchScope = searchEngineScope;

whenSilverpeasReady(function() {
  <c:if test="${silfn:isDefined(currentHeading)}">
    selectHeading('space-${currentHeading}');
  </c:if>

  $('#select-user-group-queryDirectory').hide();

  $('#searchEngineScope').click(function() {
    searchScope = searchEngineScope;
    $(this).removeClass('off').addClass('on');
    $('#directoryScope').removeClass('on').addClass('off');
    $('#select-user-group-queryDirectory').hide();
    $('#lastResult-link-header').css('visibility', 'visible');
    $('#advancedSearch-link-header').css('visibility', 'visible');
    $('#query').show();
  });

  $('#directoryScope').click(function() {
    searchScope = directoryScope;
    $(this).removeClass('off').addClass('on');
    $('#searchEngineScope').removeClass('on').addClass('off');
    $('#select-user-group-queryDirectory').css('display', 'inline-block');
    $('#lastResult-link-header').css('visibility', 'hidden');
    $('#advancedSearch-link-header').css('visibility', 'hidden');
    $('#query').hide();
    // setting 'platform' query as 'directory' query
    var directoryInput = $("input[name='queryDirectory']");
    var query = directoryInput.val();
    if (query.isNotDefined()) {
      directoryInput.val($('#query').val())
    }
  });

  $('#show-menu-spacePerso').hover(function() {
		 $('.spacePerso').show();
	}, function() {
	});
	
	$('.avatarName').hover(function() {
		
	}, function() {
		$('.spacePerso').hide();
	});

	<c:choose>
  <c:when test="${settings.displayMenuSubElements}">
    smartMenuPromise.then(function() {
      setTimeout(function() {
        $('#main-menu').smartmenus({
          subMenusMinWidth:"15em",
          subMenusMinWidth:"30em"
        });
        $('#nav').show();
      }, 0);
      spLayout.getBody().ready(function() {
        var __menuTimeout;
        var __enableMenu = function() {
          clearTimeout(__menuTimeout);
          $('#main-menu').smartmenus('enable');
        };
        spLayout.getBody().getContent().addEventListener('start-load', function() {
          $('#main-menu').smartmenus('disable', true);
          clearTimeout(__menuTimeout);
          __menuTimeout = setTimeout(__enableMenu, 3000);
        }, '__id__top-bar');
        spLayout.getBody().getContent().addEventListener('load', __enableMenu, '__id__top-bar');
      });
    });
  </c:when>
  <c:otherwise>
    $('#nav').show();
  </c:otherwise>
  </c:choose>
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
<div class="header-container ${anonymousMode}">
  <div class="wrapper clearfix">
    <h1 class="title">Intranet</h1>
    <a id="logo-header" href="#" onclick="goToHome();"> <img alt="" src="${settings.logo}" /> </a>
    <div id="topar-header">
      <div id="infoConnection">
        <c:if test="${isAnonymous}">
          <a href="${urlLogin}" id="login" class="sp_button logOn"><span>${labelLogin}</span></a>
        </c:if>
        <c:if test="${not isAnonymous}">
          <a href="javascript:goToPersonalSpace()"><view:image type="avatar" id="avatar-img" alt="mon avatar" src="${lookHelper.userDetail.avatar}" /></a>
            <div class="avatarName">
              <div class="btn-header">
                <a title="${labelProfile}" href="javascript:goToPersonalSpace()">${lookHelper.userFullName}</a>
                <a id="show-menu-spacePerso" href="#"> Mon espace perso</a>
              </div>
              <div class="spacePerso">
                <ul>
                  <li><a id="link-settings" href="javascript:changeBody('/RMyProfil/jsp/MyInfos')">${labelProfileSettings}</a> </li>
                  <c:if test="${not isAccessGuest}">
                    <li><a id="link-myspace" href="javascript:goToPersonalSpace()">${labelProfileMySpace}</a></li>
                  </c:if>
                  <li><a id="link-feed" href="javascript:changeBody('/RMyProfil/jsp/Main')">${labelProfileMyFeed}</a></li>
                  <li><a id="link-logout" id="logOut-link" href="javascript:onClick=spUserSession.logout();">${labelLogout}</a> </li>
                </ul>
              </div>
              <div id="btn-logout">
                <a href="javascript:onClick=spUserSession.logout();">${labelLogout}</a>
              </div>
            </div>
            <c:if test="${not isAccessGuest}">
              <div id="topbar-user-notifications" class="silverpeas-user-notifications">
                <silverpeas-user-notifications no-unread-label="${labelUserNotifications}"
                                               one-unread-label="${labelUnreadUserNotification}"
                                               several-unread-label="${labelUnreadUserNotifications}">
                  <div id="notification-count" class="btn-header"> <a href="javascript:void(0)"></a></div>
                </silverpeas-user-notifications>
              </div>
              </c:if>
            <div id="topbar-basket-selection" class="silverpeas-basket-selection">
              <silverpeas-basket-selection v-on:api="setApi">
                <div id="basket-selection" class="btn-header"><a href="javascript:void(0)"></a></div>
              </silverpeas-basket-selection>
            </div>

            <c:if test="${not isAccessGuest}">
              <script type="text/javascript">
                whenSilverpeasReady(function() {
                  SpVue.createApp().mount('#topbar-user-notifications');
                  SpVue.createApp({
                    methods : {
                      setApi : function(api) {
                        window.spBasketSelectionApi = api;
                      }
                    }
                  }).mount('#topbar-basket-selection');
                });
              </script>
            </c:if>
        </c:if>
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
        <li id="map-link-header"><a href="javascript:changeBody('/admin/jsp/Map.jsp')" title="${labelMap}">${labelMap}</a></li>
        <c:if test="${silfn:isDefined(settings.helpURL)}">
          <li id="help-link-header"><a target="_blank" href="${settings.helpURL}" title="${labelHelp}">${labelHelp}</a></li>
        </c:if>
        <c:if test="${isDisplayDirectory}">
          <li id="directory-link-header"><a href="javascript:changeBody('${directoryURL}')" title="${labelDirectory}">${labelDirectory}</a></li>
        </c:if>
        <c:if test="${lookHelper.backOfficeVisible}">
          <li id="adminstration-link-header"> <a href="javascript:void(0)" onclick="spWindow.loadAdminHomePage();" title="${labelBackoffice}">${labelBackoffice}</a></li>
        </c:if>

        <c:forEach var="shortcut" items="${shortcuts}" varStatus="count">
          <c:set var="className" value="sp-link"/>
          <c:if test="${shortcut.target == '_blank'}">
            <c:set var="className" value=""/>
          </c:if>
          <li id="tools-shorcut${count.index}">
            <a class="${className}" href="${shortcut.url}" title="${shortcut.altText}" target="${shortcut.target}">
            <c:if test="${not empty shortcut.iconURL}">
              <img alt="${shortcut.altText}" src="${shortcut.iconURL}" />
            </c:if>
            <span>${shortcut.altText}</span></a>
          </li>
        </c:forEach>

      </ul>
      <div id="search-zone-header">
        <form id="search-form-header" method="get" name="searchForm">
          <div id="inputSearchSwitchable">
            <label for="query">${labelSearch}</label>
            <input id="query" size="30" name="query" placeholder="${labelSearchPlaceholder}"/>
            <viewTags:selectUsersAndGroups selectionType="USER" noUserPanel="true" noSelectionClear="true"
                                           doNotSelectAutomaticallyOnDropDownOpen="true"
                                           queryInputName="queryDirectory" id="queryDirectory"
                                           navigationalBehavior="true" onChangeJsCallback="jumpToUser"
                                           domainsFilter="${directoryDomains}" groupsFilter="${directoryGroups}" />
            <input type="hidden" value="clear" name="mode"/>
            <input type="hidden" value="${directoryDomainIds}" name="DomainIds"/>
            <input type="hidden" value="true" name="Global"/>
            <a href="#" id="searchEngineScope" class="switchSearchMode platform on" title="${labelSearchPlatform}"><span>${labelSearchPlatformShort}</span></a>
            <a href="#" id="directoryScope" class="switchSearchMode directory off" title="${labelSearchDirectory}"><span>${labelSearchDirectoryShort}</span></a>
          </div>
          <a href="javascript:searchEngine()">${labelSearchButton}</a>
        </form>
        <a id="lastResult-link-header" href="javascript:lastResultsSearchEngine()" title="${labelSearchResults}"><span>${labelSearchResults}</span></a>
        <a id="advancedSearch-link-header" href="javascript:advancedSearchEngine()" title="${labelSearchAdvanced}"><span>${labelSearchAdvanced}</span></a>
      </div>
    </div>
    <div id="nav" style="display: none;">
      <ul id="main-menu" class="sm ${smartmenusSkin} displayMenuAppIcons-${settings.displayMenuAppIcons}">
        <c:if test="${lookHelper.userCanDisplayMainHomePage}">
          <li class="selected" id="home">
            <a href="javascript:goToHome();"><span>${labelHome}</span></a>
          </li>
        </c:if>
        <c:forEach var="item" items="${mainItems}">
          <view:map spaceId="${item.space.id}" displayAppsFirst="${settings.displayMenuAppsFirst}"
                    displayAppIcon="${settings.displayMenuAppIcons}" megaMenu="${settings.displayMegaMenu}"
                    callbackJSForMainSpace="goToMainSpace" callbackJSForSubspaces="goToSpace" callbackJSForApps="goToSpaceApp"
                    forceHidingComponents="true"/>
        </c:forEach>
      </ul>
    </div>
    <div id="deco-header"> </div>
  </div>
</div>

</c:otherwise>
</c:choose>