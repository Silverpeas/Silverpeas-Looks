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
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="lookHelper" value="${sessionScope['Silverpeas_LookHelper']}"/>
<view:setBundle bundle="${lookHelper.localizedBundle}"/>

<%@ attribute name="listOfNews"
              required="true"
              type="org.silverpeas.looks.aurora.NewsList" %>

<%@ attribute name="id"
              required="false"
              type="java.lang.String" %>

<c:set var="imageSize" value="${listOfNews.imageSize}"/>
<c:set var="carousel" value="${listOfNews.renderingType.carousel}"/>

<c:if test="${empty id}">
  <c:set var="id" value="carrousel-actualite"/>
</c:if>

<fmt:message var="labelNews" key="look.home.news"/>
<fmt:message var="labelNewsMore" key="look.home.news.more"/>

<c:set var="newsClass" value="list-news"/>
<c:if test="${carousel}">
  <c:set var="newsClass" value="rslides"/>
</c:if>

<c:if test="${not empty listOfNews.news}">
  <div class="secteur-container" id="${id}">
    <h4>${labelNews}</h4>
    <ul class="${newsClass}" id="slider">
      <c:forEach var="news" items="${listOfNews.news}">
        <c:set var="tpCSS" value="${news.taxonomyPositionAsString}"/>
        <li class="news-${news.appShortcut.target} ${tpCSS}">
          <a class="sp-permalink" href="${news.permalink}">
            <c:choose>
              <c:when test="${not empty news.thumbnailURL}">
                <view:image src="${news.thumbnailURL}" alt="" size="${imageSize}"/>
              </c:when>
              <c:otherwise>
                <view:image src="/look/jsp/imgDesign/emptyNews.png" alt="" />
              </c:otherwise>
            </c:choose>
          </a>
          <div class="caption">
            <h2><a class="sp-permalink" href="${news.permalink}">${news.title}</a></h2>
            <p>
              <span class="news-date"><view:formatDate value="${news.date}"/></span>
              <view:componentPath componentId="${news.news.componentInstanceId}" includeComponent="false"/>
              <c:if test="${empty listOfNews.uniqueAppURL}">
                <a class="sp-link" href="${news.appShortcut.url}" title="${labelNewsMore}"><span class="news-app">${news.appShortcut.altText}</span></a>
              </c:if>
                ${news.description}
            </p>
          </div>
        </li>
      </c:forEach>
    </ul>
    <c:if test="${not empty listOfNews.buttons}">
      <div class="news-link-taxonomy">
        <c:forEach var="button" items="${listOfNews.buttons}">
          <a title="${labelNewsMore}" href="listOfNews.jsp?TaxonomyPosition=${button.param}&Label=${button.label}" class="link-more ${button.paramAsCSS}">${button.label}</a>
        </c:forEach>
      </div>
    </c:if>
    <c:choose>
      <c:when test="${not empty listOfNews.uniqueAppURL}">
        <a title="${labelNewsMore}" href="${listOfNews.uniqueAppURL}" class="link-more sp-link"><span>${labelNewsMore}</span></a>
      </c:when>
      <c:otherwise>
        <div id="news-link-apps">
          <c:forEach items="${listOfNews.appShortcuts}" var="appNewsShortcut">
            <a title="${labelNewsMore}" href="${appNewsShortcut.url}" class="link-more sp-link" id="link-app-${appNewsShortcut.target}"><span>${appNewsShortcut.altText}</span></a>
          </c:forEach>
        </div>
      </c:otherwise>
    </c:choose>
  </div>
  <script type="text/javascript">
    whenSilverpeasReady(function() {
      <c:if test="${carousel}">
      $("#${id} .rslides#slider").responsiveSlides({
        auto: true,
        pager: true,
        nav: true,
        speed: 500,
        pause: true,
        timeout: 6000,
        namespace: "centered-btns"
      });
      </c:if>
    });
  </script>
</c:if>