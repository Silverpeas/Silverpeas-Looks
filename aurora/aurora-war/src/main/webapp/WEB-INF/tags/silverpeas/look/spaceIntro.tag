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
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>

<c:set var="lookHelper" value="${sessionScope['Silverpeas_LookHelper']}"/>
<view:setBundle bundle="${lookHelper.localizedBundle}"/>

<%@ attribute name="space"
              required="true"
              type="org.silverpeas.looks.aurora.Space" %>

<h1 class="spaceName">${space.name}</h1>
<c:if test="${not empty space.picture}">
  <div id="mainPicture"><view:image src="${space.picture}"/></div>
</c:if>
<c:choose>
  <c:when test="${not empty space.description || not empty space.intro}">
        <c:if test="${not empty space.intro}">
          <div silverpeas-toggle originalClass="spaceIntro spaceDescription rich-content">
            ${space.intro}
          </div>
        </c:if>
        <c:if test="${empty space.intro}">
          <p class="spaceDescription">
            ${space.description}
          </p>
        </c:if>
  </c:when>
  <c:otherwise>
    <p></p>
  </c:otherwise>
</c:choose>
