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

<fmt:message var="labelTitle" key="look.subscription.news.title"/>
<fmt:message var="labelPublished" key="look.subscription.news.published"/>
<fmt:message var="labelPublishedBy" key="look.subscription.news.publishedby"/>
<fmt:message var="labelAllPublished" key="look.subscription.news.allpublished"/>

<%@ attribute name="enabled"
              required="true"
              type="java.lang.Boolean" %>

<%@ attribute name="listOfNews"
              required="true"
              type="java.util.List<org.silverpeas.looks.aurora.AuroraNews>" %>


<c:if test="${enabled}">
    <c:if test="${not empty listOfNews}">
        <div class="secteur-container subscription-news" id="myNews-subscription">
            <h4>${labelTitle}</h4>
            <ul class="listing-news">
                <c:forEach var="news" items="${listOfNews}">
                    <c:choose>
                        <c:when test="${news.news.important}">
                            <c:set var="important" value="important"/>
                        </c:when>
                        <c:otherwise>
                            <c:set var="important" value=""/>
                        </c:otherwise>
                    </c:choose>
                    <li class="${important}" onclick="spWindow.loadPermalink('${news.permalink}')">
                        <div class="content-news-illustration">
                            <c:choose>
                                <c:when test="${not empty news.thumbnailURL}">
                                    <view:image src="${news.thumbnailURL}" alt="" size="400x"/>
                                </c:when>
                                <c:otherwise>
                                    <view:image src="/look/jsp/imgDesign/emptyNews.png" alt=""/>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <h3 class="news-title"><a class="sp-permalink" href="${news.permalink}">${news.title}</a></h3>
                        <div class="news-info-fonctionality">
          <span class="news-publishing">
          <span class="news-date"><span class="news-date-label">${labelPublished} </span><view:formatDate
                  value="${news.news.publishDate}"/></span>
          <span class="news-source"><span
                  class="news-source-label">${labelPublishedBy} </span>${news.publisherName}</span>
          <view:componentPath componentId="${news.news.componentInstanceId}" includeComponent="false"/>
                        </div>
                        <p class="news-teasing">${news.description}</p>
                    </li>
                </c:forEach>
            </ul>
            <div id="myNews-link-page">
                <a title="${labelAllPublished}" href="/silverpeas/look/jsp/listOfNewsBySubscription.jsp"
                   class="link-more sp-link"><span>${labelAllPublished}</span></a>
            </div>
        </div>
    </c:if>
</c:if>
