<%--
  ~ Copyright (C) 2000 - 2018 Silverpeas
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

<%@ attribute name="users"
              required="true"
              type="java.util.List" %>

<%@ attribute name="label"
              required="false"
              type="java.lang.String" %>

<fmt:message var="labelSpaceUsers" key="look.space.home.users"/>
<c:if test="${not empty label}">
  <c:set var="labelSpaceUsers" value="${label}"/>
</c:if>

<c:if test="${not empty users}">
  <div class="secteur-container" id="spaceUsers">
    <div class="header">
      <h4 class="portlet-title">${labelSpaceUsers}</h4>
    </div>
    <div class="portlet-content">
      <ul class="list-users">
        <c:forEach var="user" items="${users}">
          <li class="intfdcolor">
            <div class="user-card" rel="${user.id}">
              <div class="avatar"><view:image src="${user.avatar}" alt="avatar" size="x140"/></div>
              <span class="field userToZoom" rel="${user.id}">${user.firstName} ${user.lastName}</span>
              <span class="field title"></span>
              <span class="field phone"></span>
            </div>
          </li>
        </c:forEach>
      </ul>
      <br clear="all"/>
    </div>
  </div>
</c:if>