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
<%@ taglib uri="silverpeas.tags.silverFunctions" prefix="silfn" %>
<%@ taglib uri="silverpeas.tags.viewGenerator" prefix="view" %>
<%@ tag import="org.silverpeas.core.admin.service.OrganizationController" %>
<%@ tag import="org.silverpeas.core.admin.space.SpaceInstLight" %>
<%@ tag import="java.util.stream.Collectors" %>

<c:set var="lookHelper" value="${sessionScope['Silverpeas_LookHelper']}"/>
<view:setBundle bundle="${lookHelper.localizedBundle}"/>

<%@ attribute name="lookHelper"
              required="true"
              type="org.silverpeas.core.web.look.LookHelper" %>

<%@ attribute name="publications"
              required="true"
              type="java.util.List<org.silverpeas.core.contribution.publication.model.PublicationDetail>" %>

<%@ attribute name="embedded" required="false" type="java.lang.Boolean" %>

<fmt:message var="labelPublications" key="look.home.publications.title"/>
<fmt:message var="labelPublicationsMore" key="look.home.publications.more"/>

<c:if test="${not empty publications}">
<div <c:if test="${not embedded}">id="last-publication-home"</c:if> class="secteur-container latest-publications">
  <h4>${labelPublications}</h4>
  <div <c:if test="${not embedded}">id="last-publicationt-main-container"</c:if> class="latest-publications-main-container">
    <ul class="last-publication-list">
      <c:forEach var="publication" items="${publications}">
        <c:set var="newPubliCssClass" value=""/>
        <c:if test="${publication.new}">
          <c:set var="newPubliCssClass" value=" new-contribution"/>
        </c:if>
        <c:set var="pubInstanceId" value="${publication.instanceId}"/>
        <jsp:useBean id="pubInstanceId" type="java.lang.String"/>
        <c:set var="fromSpaceClasses" value='<%=OrganizationController.get().getPathToComponent(pubInstanceId).stream().map(s -> "fromSpace-" + s.getId()).collect(Collectors.joining(" "))%>'/>
        <li class="${fromSpaceClasses} fromInst-${pubInstanceId}${newPubliCssClass}">
          <a class="sp-link publication-name" href="${publication.permalink}">
            <c:out value="${publication.name}"/></a>
          <view:componentPath componentId="${pubInstanceId}" includeComponent="false"/>
          <span class="user-publication"><view:username userId="${publication.updaterId}" /></span>
          <span class="date-publication">${silfn:formatAsLocalDate(publication.visibility.period.startDate, lookHelper.zoneId, lookHelper.language)}</span>
          <p class="description-publication"><c:out value="${publication.description}"/></p>
        </li>
      </c:forEach>
    </ul>
    <c:if test="${not embedded}">
    <div id="morePublications-link-page">
      <a title="${labelPublicationsMore}" href="/silverpeas/look/jsp/listOfPublications.jsp"
         class="link-more sp-link"><span>${labelPublicationsMore}</span></a>
    </div>
    </c:if>
  </div>
</div>
</c:if>