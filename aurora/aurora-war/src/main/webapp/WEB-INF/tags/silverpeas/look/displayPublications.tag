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
  ~ along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="lookHelper" value="${sessionScope['Silverpeas_LookHelper']}"/>
<view:setBundle bundle="${lookHelper.localizedBundle}"/>

<%@ attribute name="lookHelper"
              required="true"
              type="org.silverpeas.core.web.look.LookHelper" %>

<%@ attribute name="publications"
              required="true"
              type="java.util.List<org.silverpeas.core.contribution.publication.model.PublicationDetail>" %>

<fmt:message var="labelPublications" key="look.home.publications.title"/>

<c:if test="${not empty publications}">
<div id="last-publication-home" class="secteur-container">
  <h4>${labelPublications}</h4>
  <div id="last-publicationt-main-container">
    <ul class="last-publication-list">
      <c:forEach var="publication" items="${publications}">
        <c:set var="newPubliCssClass" value=""/>
        <c:if test="${publication.new}">
          <c:set var="newPubliCssClass" value="class=\"new-contribution\""/>
        </c:if>
        <li ${newPubliCssClass} onclick="spWindow.loadLink('${publication.permalink}')">
          <a class="sp-link publication-name" href="${publication.permalink}">${publication.name}</a>
          <view:componentPath componentId="${publication.instanceId}" includeComponent="false"/>
          <span class="user-publication"><view:username userId="${publication.updaterId}" /></span>
          <span class="date-publication">${silfn:formatAsLocalDate(publication.visibility.period.startDate, lookHelper.zoneId, lookHelper.language)}</span>
          <p class="description-publication">${publication.description}</p>
        </li>
      </c:forEach>
    </ul>
  </div>
</div>
</c:if>