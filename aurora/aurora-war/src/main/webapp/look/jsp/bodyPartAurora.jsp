<%--

    Copyright (C) 2000 - 2019 Silverpeas

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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="../../admin/jsp/importFrameSet.jsp" %>

<c:set var="lookHelper" value="<%=LookHelper.getLookHelper(session)%>"/>
<c:if test="${lookHelper != null}">
  <jsp:useBean id="lookHelper" type="org.silverpeas.looks.aurora.LookAuroraHelper"/>
  <c:set var="navigationWidth" value="${lookHelper.getSettings('domainsBarFramesetWidth','260')}px"/>
  <c:set var="redExtLabel" value="${lookHelper.getString('look.layout.reduce')}"/>
  <c:set var="bodyPartSettings" value="${lookHelper.getBodyPartSettings(pageContext.request)}"/>
  <jsp:useBean id="bodyPartSettings" type="org.silverpeas.looks.aurora.BodyPartSettings"/>
  <c:set var="paramsForDomainsBar" value="${bodyPartSettings.domainsBarParams}"/>
  <c:set var="frameURL" value="${bodyPartSettings.mainPartURL}"/>

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
      width: ${navigationWidth};
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
    <div id="navigation-toggle" style="display: none"><img src="icons/silverpeasV5/reduct.gif" alt="${redExtLabel}" title="${redExtLabel}"/></div>
    <div id="header-toggle"><img src="icons/silverpeasV5/reductTopBar.gif" alt="${redExtLabel}" title="${redExtLabel}"/></div>
  </div>
  <div id="sp-layout-body-part-layout">
    <div id="sp-layout-body-part-layout-navigation-part"></div>
    <div id="sp-layout-body-part-layout-content-part">
      <iframe src="${frameURL}" marginwidth="0" id="MyMain" name="MyMain" marginheight="0" frameborder="0" scrolling="auto" width="100%" height="100%"></iframe>
    </div>
  </div>
  <script type="text/javascript">
    (function() {
      var lastDisplaySpace = {
        isPersonal : false
      };
      spLayout.getBody().ready(function() {
        spLayout.getBody().getNavigation().addEventListener('start-load', function(event) {
          var navigationContext = event.detail.data;
          if (navigationContext.currentSpaceId) {
            lastDisplaySpace.isPersonal = navigationContext.isPersonalSpace;
          }
          if (navigationContext.contentNotRelatedToSpaceOrPersonalSpace ) {
            lastDisplaySpace.isPersonal = false;
          }
          var showMenu = lastDisplaySpace.isPersonal
                         || navigationContext.currentSpaceId
                         || navigationContext.currentComponentId;
          if (showMenu) {
            spLayout.getBody().getNavigation().show();
          } else {
            spLayout.getBody().getNavigation().hide(true);
          }
        }, '__id__aurora-body-part');
        spLayout.getBody().getNavigation().load(${paramsForDomainsBar});
      });
    })();
  </script>
</c:if>