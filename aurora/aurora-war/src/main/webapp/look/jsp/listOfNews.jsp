<%@ page import="org.silverpeas.looks.aurora.LookAuroraHelper" %>
<%@ page import="org.silverpeas.core.web.look.LookHelper" %>
<%@ page import="org.silverpeas.looks.aurora.NewsList" %><%--

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

<%
  LookAuroraHelper helper = (LookAuroraHelper) LookHelper.getLookHelper(session);
  NewsList newsList = helper.getAllNewsByTaxonomyPosition(request.getParameter("TaxonomyPosition"));
  String title = request.getParameter("Label");
%>

<c:set var="lookHelper" value="${sessionScope['Silverpeas_LookHelper']}"/>
<view:setBundle bundle="${lookHelper.localizedBundle}"/>
<c:set var="settings" value="${lookHelper.lookSettings}"/>

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
<view:sp-body-part cssClass="aurora-list-news">
<view:browseBar />
<view:window>
  <h1><%=title%></h1>
<viewTags:displayNews listOfNews="<%=newsList%>" carrousel="false" imageSize="800x"/>
</view:window>
</view:sp-body-part>
</view:sp-page>
