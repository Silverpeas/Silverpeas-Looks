<%--
  ~ Copyright (C) 2000 - 2022 Silverpeas
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
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>

<c:set var="lookHelper" value="${sessionScope['Silverpeas_LookHelper']}"/>
<view:setBundle bundle="${lookHelper.localizedBundle}"/>

<%@ attribute name="listOfNews"
              required="true"
              type="org.silverpeas.looks.aurora.NewsList" %>

<fmt:message var="labelNews" key="look.space.home.news"/>

<script type="text/javascript">
  <c:if test="${listOfNews.renderingType.carousel}">
  whenSilverpeasReady(function() {
    $("#spaceQuiskInfo ul.carousel").responsiveSlides({
      auto: true,
      pager: true,
      nav: true,
      speed: 500,
      pause: true,
      timeout: 6000,
      namespace: "centered-btns"
    });
  });
  </c:if>
</script>

<c:set var="listOrcarouselCss" value="list"/>
<c:if test="${listOfNews.renderingType.carousel}">
  <c:set var="listOrcarouselCss" value="carousel rslides"/>
</c:if>

<c:if test="${not empty listOfNews.news}">
  <!-- QuickInfo -->
  <div class="secteur-container" id="spaceQuiskInfo">
    <div class="header">
      <h2 class="portlet-title">${labelNews}</h2>
    </div>
    <div class="portlet-content slideshow" data-transition="crossfade" data-loop="true" data-skip="false">
      <ul class="${listOrcarouselCss}">
        <c:forEach var="aNews" items="${listOfNews.news}">
          <c:set var="tpCSS" value="${aNews.taxonomyPositionAsString}"/>
          <li class="slide ${tpCSS}" onclick="spWindow.loadPermalink('${aNews.permalink}')">
            <c:if test="${not empty aNews.thumbnailURL}">
              <div class="thumbnail"><img src="${aNews.thumbnailURL}" alt=""/></div>
            </c:if>
            <div class="content-quickInfo caption">
              <h3 class="title-quickInfo">
                <a class="sp-permalink" href="${aNews.permalink}">
                    ${silfn:escapeHtml(aNews.title)}
                </a>
              </h3>
              <p class="news-date"><view:formatDate value="${aNews.date}"/></p>
              <view:componentPath componentId="${aNews.news.componentInstanceId}" includeComponent="false"/>
              <c:if test="${not empty aNews.description}">
                <p>${aNews.description}</p>
              </c:if>
            </div>
          </li>
        </c:forEach>
      </ul>
    </div>
  </div>
  <!--  /QuickInfo -->
</c:if>
