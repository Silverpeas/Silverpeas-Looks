<%--
  ~ Copyright (C) 2000 - 2018 Silverpeas
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU Affero General Public License as
  ~ published by the Free Software Foundation, either version 3 of the
  ~ License, or (at your option) any later version.
  ~
  ~ As a special exception to the terms and conditions of version 3.0 of
  ~ the GPL, you may redistribute this Program in connection with Free/Libre
  ~ Open Source Software ("FLOSS") applications as described in Silverpeas's
  ~ FLOSS exception.  You should have received a copy of the text describing
  ~ the FLOSS exception, and it is also available here:
  ~ "https://www.silverpeas.org/legal/floss_exception.html"
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU Affero General Public License for more details.
  ~
  ~ You should have received a copy of the GNU Affero General Public License
  ~ along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="lookHelper" value="${sessionScope['Silverpeas_LookHelper']}"/>
<view:setBundle bundle="${lookHelper.localizedBundle}"/>

<%@ attribute name="bookmarks"
              required="true"
              type="java.util.List" %>

<%@ attribute name="showWhenEmpty"
              required="true"
              type="java.lang.Boolean" %>

<%@ attribute name="noBookmarksFragment"
              required="false"
              type="java.lang.String" %>

<fmt:message var="labelBookmarks" key="look.home.bookmarks.title"/>
<fmt:message var="labelBookmarksManage" key="look.home.bookmarks.manage"/>
<fmt:message var="labelBookmarksMore" key="look.home.bookmarks.more"/>

<style type="text/css">
  .other-bookmark {
    display: none;
  }
</style>

<c:if test="${not empty bookmarks || showWhenEmpty}">
  <div class="secteur-container user-favorit" id="user-favorit-home">
    <h4>${labelBookmarks}</h4>
    <div class="user-favorit-main-container">
      <c:if test="${not empty bookmarks}">
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
      <c:if test="${empty bookmarks and silfn:isDefined(noBookmarksFragment)}">
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

<script type="text/javascript">
  function toggleBookmarks() {
    $(".other-bookmark").toggle("slow");
    if ($(".other-bookmark").css("display") == "none") {
      $("#user-favorit-home a").removeClass("less");
    } else {
      $("#user-favorit-home a").addClass("less");
    }
  }
</script>