<%@ page import="org.silverpeas.looks.aurora.LookAuroraHelper" %>
<%@ page import="org.silverpeas.core.web.look.LookHelper" %>
<%@ page import="org.silverpeas.looks.aurora.NewsList" %>
<%@ page import="org.silverpeas.core.admin.service.OrganizationController" %>
<%@ page import="java.util.stream.Collectors" %><%--

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
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/look" prefix="viewTags" %>

<%
    response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
    response.setHeader("Pragma", "no-cache"); //HTTP 1.0
    response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>

<view:timeout/>

<%
    LookAuroraHelper helper = (LookAuroraHelper) LookHelper.getLookHelper(session);
%>

<c:set var="lookHelper" value="${sessionScope['Silverpeas_LookHelper']}"/>
<view:setBundle bundle="${lookHelper.localizedBundle}"/>
<c:set var="settings" value="${lookHelper.lookSettings}"/>
<c:set var="publications" value="${lookHelper.moreLatestPublications}"/>

<fmt:message var="labelPublications" key="look.home.publications.title"/>


<c:set var="extraJavascript" value="${settings.extraJavascriptForHome}"/>

<view:sp-page>
    <view:sp-head-part>
  <jsp:attribute name="atTop">
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
    <view:sp-body-part cssClass="aurora-list-publications">
        <view:browseBar />
        <view:window>
            <h1>${labelPublications}</h1>
            <c:if test="${not empty publications}">
                <div id="last-publication-home" class="secteur-container">
                    <div id="last-publicationt-main-container">
                        <ul class="last-publication-list">
                            <c:forEach var="publication" items="${publications}">
                                <c:set var="newPubliCssClass" value=""/>
                                <c:if test="${publication.new}">
                                    <c:set var="newPubliCssClass" value=" new-contribution"/>
                                </c:if>
                                <c:set var="pubInstanceId" value="${publication.instanceId}"/>
                                <jsp:useBean id="pubInstanceId" type="java.lang.String"/>
                                <c:set var="fromSpaceClasses" value='<%=OrganizationController.get().getPathToComponent(pubInstanceId).stream().map(s -> "fromSpace-" + s.getId()).collect(Collectors.joining(" "))%>'/>
                                <li class="${fromSpaceClasses} fromInst-${pubInstanceId}${newPubliCssClass}" onclick="spWindow.loadLink('${publication.permalink}')">
                                    <a class="sp-link publication-name" href="${publication.permalink}">
                                        <c:out value="${publication.name}"/></a>
                                    <view:componentPath componentId="${pubInstanceId}" includeComponent="false"/>
                                    <span class="user-publication"><view:username userId="${publication.updaterId}" /></span>
                                    <span class="date-publication">${silfn:formatAsLocalDate(publication.visibility.period.startDate, lookHelper.zoneId, lookHelper.language)}</span>
                                    <p class="description-publication"><c:out value="${publication.description}"/></p>
                                </li>
                            </c:forEach>
                        </ul>
                        <div id="morePublications-link-page">
                            <a title="${labelPublicationsMore}" href="/silverpeas/look/jsp/listOfPublications.jsp"
                               class="link-more sp-link"><span>${labelPublicationsMore}</span></a>
                        </div>
                    </div>
                </div>
            </c:if>
        </view:window>
    </view:sp-body-part>
</view:sp-page>