<%--
  ~ Copyright (C) 2000 - 2026 Silverpeas
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
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<%@ taglib uri="silverpeas.tags.viewGenerator" prefix="view" %>
<%@ taglib uri="silverpeas.tags.silverFunctions" prefix="silfn" %>

<c:set var="lookHelper" value="${sessionScope['Silverpeas_LookHelper']}"/>
<view:setBundle bundle="${lookHelper.localizedBundle}"/>

<%@ attribute name="rssFeeds"
              required="true"
              type="org.silverpeas.looks.aurora.RSSFeeds" %>

<%@ attribute name="id" required="false" type="java.lang.String" %>
<%@ attribute name="title" required="false" type="java.lang.String" %>
<c:set var="rssId" value="${empty id ? 'rssNews' : id}"/>
<c:set var="rssSliderId" value="${rssId}-slider"/>
<c:if test="${empty id}"><c:set var="rssSliderId" value="rss-slider"/></c:if>

<fmt:message var="labelRSS" key="look.home.rssFeeds"/>

<c:set var="rssDisplayMode" value="aggregate"/>
<c:if test="${rssFeeds.tabs}">
  <c:set var="rssDisplayMode" value="rss_tab" />
</c:if>
<c:if test="${rssFeeds.carrousel}">
  <c:set var="rssDisplayMode" value="rss_carrousel" />
</c:if>

<c:if test="${not empty rssFeeds and not empty rssFeeds.aggregatedItems}">
  <c:set var="rssChannels" value="${rssFeeds.channels}"/>
  <c:set var="onceChannel" value="${fn:length(rssChannels) == 1}"/>
  <c:set var="rssItems" value="${rssFeeds.aggregatedItems}"/>

  <div id="${rssId}" class="homepage-rss feed-home secteur-container ${rssDisplayMode} onceChannel-${onceChannel}">
    <h4><c:out value="${empty title ? labelRSS : title}"/></h4>
    <c:choose>
      <c:when test="${rssFeeds.carrousel}">
        <ul id="${rssSliderId}" class="homepage-rss-slider">
          <!-- Display each RSS items -->
          <c:forEach var="rssItem" items="${rssItems}">
            <li class="item-channel channel_${rssItem.channelId}">
              <h3 class="title-item-rssNews">
                <a href="${rssItem.itemLink}" target="_blank">${rssItem.itemTitle}</a>
              </h3>
            </li>
          </c:forEach>
        </ul>
      </c:when>
      <c:otherwise>
        <div class="rss_type">
          <c:forEach var="rssChannel" items="${rssChannels}">
            <a href="#" data-channel="channel_${rssChannel.PK.id}" class="link"><span>${rssChannel.feed.title}</span></a>
          </c:forEach>
        </div>
        <ul>
          <!-- Display each RSS items -->
          <c:forEach var="rssItem" items="${rssItems}">
            <li class="item-channel channel_${rssItem.channelId} deploy-item">
              <h3 class="title-item-rssNews">
                <span class="channelName-rssNews"><span>${rssItem.channelTitle}</span></span>
                <a href="${rssItem.itemLink}" target="_blank">${rssItem.itemTitle}</a>
              </h3>
              <div class="lastUpdate-item-rssNews"><view:formatDateTime value="${rssItem.itemDate}"/></div>
              <div class="itemDeploy" hidden>
                <div class="description-item-rssNews">${rssItem.itemDescription}</div>
                <br class="clear"/>
              </div>
            </li>
          </c:forEach>
        </ul>
      </c:otherwise>
    </c:choose>

  </div>

  <view:script src="/look/jsp/js/homepage-feeds.js?version=15592-feeds-v5"/>
</c:if>