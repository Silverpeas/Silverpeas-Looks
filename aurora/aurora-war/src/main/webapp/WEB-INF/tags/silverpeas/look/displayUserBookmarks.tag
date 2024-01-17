<%--
  ~ Copyright (C) 2000 - 2024 Silverpeas
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
  ~ along with this program.  If not, see <https://www.gnu.org/licenses/>.
  --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ tag import="org.silverpeas.core.util.JSONCodec" %>
<%@ tag import="org.silverpeas.core.webapi.mylinks.MyLinkEntity" %>
<%@ tag import="java.util.stream.Collectors" %>

<c:set var="lookHelper" value="${sessionScope['Silverpeas_LookHelper']}"/>
<view:setBundle bundle="${lookHelper.localizedBundle}"/>

<%@ attribute name="bookmarks"
              required="true"
              type="java.util.List<org.silverpeas.core.mylinks.model.LinkDetail>" %>

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
        <view:script src="/myLinksPeas/jsp/javaScript/vuejs/mylinkspeas.js"/>
        <view:link href="/myLinksPeas/jsp/styleSheets/myLinksPeas.css"/>
        <div id="mylinkspeas-widget">
          <mylinkspeas-widget v-bind:links="links"/>
        </div>
        <c:set var="jsonBookmark" value="<%=JSONCodec.encode(bookmarks.stream().map(b -> MyLinkEntity.fromLinkDetail(b, null)).collect(Collectors.toList()))%>"/>
        <script type="text/javascript">
          new Vue({
            el : '#mylinkspeas-widget',
            data : function() {
              return {
                links : ${jsonBookmark}
              }
            }
          });
        </script>
      </c:if>
    </div>
    <a title="${labelBookmarksManage}" href="javaScript:changeBody('/RmyLinksPeas/jsp/Main')" class="link-add manage" ><span>${labelBookmarksManage}</span></a>
  </div>
</c:if>

<script type="text/javascript">
  whenSilverpeasReady(function() {
    <c:if test="${empty bookmarks and showWhenEmpty and silfn:isDefined(noBookmarksFragment)}">
      sp.ajaxRequest('${noBookmarksFragment}').loadTarget('.user-favorit-main-container');
    </c:if>
  });
</script>