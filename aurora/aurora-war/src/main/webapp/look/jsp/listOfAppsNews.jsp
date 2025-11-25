<%@ page import="org.silverpeas.looks.aurora.LookAuroraHelper" %>
<%@ page import="org.silverpeas.core.web.look.LookHelper" %>
<%@ page import="java.util.List" %>
<%@ page import="org.silverpeas.looks.aurora.AuroraNews" %><%--

    Copyright (C) 2000 - 2024 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "https://www.silverpeas.org/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/look" prefix="viewTags" %>

<%
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
  response.setHeader("Pragma", "no-cache"); //HTTP 1.0
  response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>

<view:timeout/>

<c:set var="lookHelper" value="${sessionScope['Silverpeas_LookHelper']}"/>
<view:setBundle bundle="${lookHelper.localizedBundle}"/>
<c:set var="settings" value="${lookHelper.lookSettings}"/>

<%
  List<AuroraNews> newsLists;
  LookAuroraHelper helper = (LookAuroraHelper) LookHelper.getLookHelper(session);
  if (request.getParameter("delegated").equals("true")) {
    newsLists = helper.getAllDelegatedNews();
  } else {
    // any other value means no delegated news, whatever they are
    newsLists = helper.getNews(1, helper.getSettings("home.news.listing.size", 20));
  }
%>

<fmt:message var="labelDelegatedNewsTitle" key="look.delegated.news.title"/>
<fmt:message var="labelDelegatedNewsPublishDate" key="look.delegated.news.published"/>
<fmt:message var="labelDelegatedNewsPublisher" key="look.delegated.news.publishedby"/>


<c:set var="extraJavascript" value="${settings.extraJavascriptForHome}"/>

<view:sp-page>
<view:sp-head-part>
  <jsp:attribute name="atTop">
    <c:if test="${isAtLeastOneCarousel}">
    <view:link href="/look/jsp/css/responsiveslides.css"/>
    <view:link href="/look/jsp/css/themes.css"/>
    </c:if>
    <view:link href="/look/jsp/css/aurora.css"/>
  </jsp:attribute>
  <jsp:body>
    <c:if test="${not empty extraJavascript}">
      <script type="text/javascript" src="${extraJavascript}"></script>
    </c:if>
    <script type="text/javascript">
      <!--
      -->
    </script>
  </jsp:body>
</view:sp-head-part>
<view:sp-body-part cssClass="aurora-list-news">
<view:browseBar />
<view:window>
  <h1>${labelDelegatedNewsTitle}</h1>
  <div class="subscription-news" id="all-news-delegated">
    <ul class="listing-news">
      <c:forEach var="news" items="<%=newsLists%>">
        <c:choose>
          <c:when test="${news.news.important}">
            <c:set var="important" value="important"/>
          </c:when>
          <c:otherwise>
            <c:set var="important" value=""/>
          </c:otherwise>
        </c:choose>
        <li class="${important}">
          <a class="sp-permalink" href="${news.news.permalink}"
             onkeydown="spWindow.loadPermalink('${news.news.permalink}">
          <div class="content-news-illustration">
            <c:choose>
              <c:when test="${not empty news.thumbnailURL}">
                <view:image src="${news.thumbnailURL}" alt="" size="400x"/>
              </c:when>
              <c:otherwise>
                <view:image src="/look/jsp/imgDesign/emptyNews.png" alt="" />
              </c:otherwise>
            </c:choose>
          </div>
          <h3 class="news-title">${news.news.title}</h3>
          <div class="news-info-fonctionality">
          <span class="news-publishing">
            <span class="news-date"><span class="news-date-label">${labelDelegatedNewsPublishDate} </span><view:formatDate value="${news.date}"/></span>
            <span class="news-source"><span class="news-source-label">${labelDelegatedNewsPublisher} </span>${news.publisherName}</span>
            <view:componentPath componentId="${news.news.componentInstanceId}" includeComponent="false"/>
          </div>
          <p class="news-teasing">${news.description}</p>
          </a>
        </li>
      </c:forEach>
    </ul>
  </div>

</view:window>
</view:sp-body-part>
</view:sp-page>
