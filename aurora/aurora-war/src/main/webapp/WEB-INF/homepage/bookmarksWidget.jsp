<%--
 * Copyright (C) 2000 - 2026 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 --%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<%@ taglib uri="silverpeas.tags.viewGenerator" prefix="view" %>
<c:set var="lookHelper" value="${sessionScope['Silverpeas_LookHelper']}"/>
<view:setBundle bundle="${lookHelper.localizedBundle}"/>
<fmt:message var="labelManage" key="look.home.bookmarks.manage"/>
<c:url var="manageBookmarksUrl" value="/RmyLinksPeas/jsp/Main"/>
<view:sp-page>
  <view:sp-head-part>
    <view:script src="/util/javaScript/silverpeas-mylinks.js"/>
    <view:script src="/myLinksPeas/jsp/javaScript/vuejs/mylinkspeas.js"/>
    <view:link href="/look/jsp/css/aurora.css?version=15592-bookmarks-v1"/>
    <view:link href="/myLinksPeas/jsp/styleSheets/myLinksPeas.css"/>
    <view:link href="/look/jsp/css/homepage-bookmarks.css?version=15592-bookmarks-v1"/>
    <view:script src="/look/jsp/js/homepage-bookmarks.js?version=15592-bookmarks-v1"/>
  </view:sp-head-part>
  <view:sp-body-part cssClass="main-home-page homepage-bookmarks-document">
    <c:choose>
      <c:when test="${showBookmarks}">
        <section class="secteur-container user-favorit" id="user-favorit-home">
          <c:if test="${not empty bookmarksWidget.title}"><h4><c:out value="${bookmarksWidget.title}"/></h4></c:if>
          <div class="user-favorit-main-container">
            <c:choose>
              <c:when test="${not empty bookmarks}">
                <div id="mylinkspeas-widget" data-links="<c:out value='${bookmarksJson}'/>">
                  <mylinkspeas-widget v-bind:links="links"/>
                </div>
              </c:when>
              <c:otherwise>
                <div id="homepage-bookmarks-empty" data-fragment="<c:out value='${bookmarksWidget.bookmarksConfiguration.emptyFragment}'/>"></div>
              </c:otherwise>
            </c:choose>
          </div>
          <a title="<c:out value='${labelManage}'/>" href="<c:out value='${manageBookmarksUrl}'/>"
              target="_parent" class="link-add manage"><span><c:out value="${labelManage}"/></span></a>
        </section>
      </c:when>
      <c:otherwise><div id="homepage-bookmarks-hidden" hidden></div></c:otherwise>
    </c:choose>
  </view:sp-body-part>
</view:sp-page>
