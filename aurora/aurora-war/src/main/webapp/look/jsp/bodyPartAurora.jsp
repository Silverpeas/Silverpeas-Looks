<%--

    Copyright (C) 2000 - 2013 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page import="org.silverpeas.core.web.look.LookHelper"%>
<%@ page import="org.silverpeas.core.util.StringUtil" %>
<%@ page import="org.silverpeas.core.util.URLUtil" %>
<%@ page import="org.silverpeas.looks.aurora.LookAuroraHelper" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="../../admin/jsp/importFrameSet.jsp" %>

<c:set var="lookHelper" value="${sessionScope['Silverpeas_LookHelper']}"/>
<view:setBundle bundle="${lookHelper.localizedBundle}"/>

<fmt:message var="redExtLabel" key="look.layout.reduce" />

<%
String strGoToNew 	= (String) session.getAttribute("gotoNew");
String spaceId 		= request.getParameter("SpaceId");
String subSpaceId 	= request.getParameter("SubSpaceId");
String fromTopBar 	= request.getParameter("FromTopBar");
String componentId	= request.getParameter("ComponentId");
String login		= request.getParameter("Login");

String fromMySpace 	= request.getParameter("FromMySpace");

LookHelper helper = LookHelper.getLookHelper(session);

String navigationWidth = helper.getSettings("domainsBarFramesetWidth", "260") + "px";

  StringBuilder paramsForDomainsBar = new StringBuilder().append("{");
  if ("1".equals(request.getParameter("FromTopBar"))) {
    if (spaceId != null) {
      paramsForDomainsBar.append("privateDomain:'").append(spaceId).append("', privateSubDomain:'")
          .append(subSpaceId).append("', FromTopBar:'1'");
    }
  } else if (componentId != null) {
    paramsForDomainsBar.append("privateDomain:'', component_id:'").append(componentId).append("'");
  } else {
    paramsForDomainsBar.append("privateDomain:'").append(spaceId).append("'");
  }

  if ("1".equals(fromMySpace)) {
    paramsForDomainsBar.append(",FromMySpace:'1'");
  }

  if (StringUtil.isDefined(strGoToNew) || StringUtil.isDefined(spaceId) || StringUtil.isDefined(subSpaceId) || StringUtil.isDefined(componentId)) {
    // ignore login page when try to access a direct resource
    login = null;
  }

//Allow to force a page only on login and when user clicks on logo
boolean displayLoginHomepage = false;
String loginHomepage = helper.getSettings("loginHomepage", "");
if (StringUtil.isDefined(loginHomepage) && StringUtil.isDefined(login) &&
    (!StringUtil.isDefined(spaceId) && !StringUtil.isDefined(subSpaceId) && !StringUtil.isDefined(componentId) && !StringUtil.isDefined(strGoToNew))) {
	displayLoginHomepage = true;
}

if (displayLoginHomepage) {
  displayLoginHomepage = ((LookAuroraHelper) helper).isUserCanDisplayMainHomePage();
}

String frameURL = "";
if (displayLoginHomepage) {
	frameURL = loginHomepage;
  navigationWidth = "0px";
} else if (strGoToNew == null) {
	if (StringUtil.isDefined(componentId)) {
		frameURL = URLUtil.getApplicationURL()+ URLUtil.getURL(null, componentId)+"Main";
	} else {
		String homePage = helper.getSettings("defaultHomepage", "/dt");
		String param = "";
		if (StringUtil.isDefined(spaceId)) {
		    param = "?SpaceId=" + spaceId;
		}
		frameURL = URLUtil.getApplicationURL()+homePage+param;
	}
} else {
    frameURL = URLUtil.getApplicationURL()+strGoToNew;
    if(strGoToNew.startsWith(URLUtil.getApplicationURL())) {
      frameURL = strGoToNew;
    }
}

session.removeAttribute("goto");
session.removeAttribute("gotoNew");
session.removeAttribute("RedirectToComponentId");
session.removeAttribute("RedirectToSpaceId");

  boolean hideMenu = "1".equals(fromTopBar) || "1".equals(login);
  if (hideMenu) {
    navigationWidth = "0px";
  }
%>
<style type="text/css">

  #sp-layout-body-part-layout {
    width: 100%;
    flex: 1;
    display: flex;
    flex-direction: row;
  }

  #sp-layout-body-part-layout-navigation-part {
    position: relative;
    overflow: auto;
    width: <%=navigationWidth%>;
  }

  #sp-layout-body-part-layout-content-part {
    flex: 1;
    height: 100%;
  }

  #sp-layout-body-part-layout-toggle-part {
    display: table;
  }

  #sp-layout-body-part-layout-toggle-part div {
    margin: 0;
    padding: 0 2px 0 0;
    border: none;
    display: table-cell;
    cursor: pointer;
  }
</style>
<div id="sp-layout-body-part-layout-toggle-part" style="display: none">
  <div id="navigation-toggle"><img src="icons/silverpeasV5/reduct.gif" alt="${redExtLabel}" title="${redExtLabel}"/></div>
  <div id="header-toggle"><img src="icons/silverpeasV5/reductTopBar.gif" alt="${redExtLabel}" title="${redExtLabel}"/></div>
</div>
<div id="sp-layout-body-part-layout">
  <div id="sp-layout-body-part-layout-navigation-part"></div>
  <div id="sp-layout-body-part-layout-content-part">
    <iframe src="<%=frameURL%>" marginwidth="0" id="MyMain" name="MyMain" marginheight="0" frameborder="0" scrolling="auto" width="100%" height="100%"></iframe>
  </div>
</div>
<script type="text/javascript">
  (function() {
    // bodyPart Aurora
    spLayout.getBody().ready(function() {
      spLayout.getBody().getNavigation().load(<%=paramsForDomainsBar.append('}')%>);
    });
  })();
</script>