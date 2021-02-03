<%--
  ~ Copyright (C) 2000 - 2021 Silverpeas
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
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>

<c:set var="lookHelper" value="${sessionScope['Silverpeas_LookHelper']}"/>
<view:setBundle bundle="${lookHelper.localizedBundle}"/>

<%@ attribute name="news"
              required="true"
              type="java.util.List" %>

<fmt:message var="labelNews" key="look.space.home.news"/>

<script type="text/javascript">
  <!--
  whenSilverpeasReady(function() {
    var $s = $('.slideshow').slides();
  });
  -->
</script>

<c:if test="${not empty news}">
  <!-- QuickInfo -->
  <div class="secteur-container" id="spaceQuiskInfo">
    <div class="header">
      <h2 class="portlet-title">${labelNews}</h2>
    </div>
    <div class="portlet-content slideshow" data-transition="crossfade" data-loop="true" data-skip="false">
      <ul class="carousel">
        <c:forEach var="aNews" items="${news}">
          <li class="slide" onclick="location.href='${aNews.permalink}'">
            <h3 class="title-quickInfo">
              <a class="sp-permalink" href="${aNews.permalink}">
                ${silfn:escapeHtml(aNews.name)}
              </a>
            </h3>
            <c:if test="${not empty aNews.thumbnail}">
              <img src="${aNews.thumbnail.URL}" alt=""/>
            </c:if>
            <div class="content-quickInfo">
              <p>${aNews.description}</p>
            </div>
          </li>
        </c:forEach>
      </ul>
    </div>
  </div>
  <!--  /QuickInfo -->
</c:if>