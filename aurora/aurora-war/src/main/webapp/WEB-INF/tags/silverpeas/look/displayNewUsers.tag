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
  ~ along with this program.  If not, see <https://www.gnu.org/licenses/>.
  --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>

<c:set var="lookHelper" value="${sessionScope['Silverpeas_LookHelper']}"/>
<view:setBundle bundle="${lookHelper.localizedBundle}"/>

<%@ attribute name="newUsers"
              required="true"
              type="org.silverpeas.looks.aurora.NewUsersList" %>

<fmt:message var="labelNewUsers" key="look.home.users.new"/>

<c:if test="${not empty newUsers.users}">
  <div class="secteur-container" id="new-users-home">
    <h4>${labelNewUsers}</h4>
    <div class="new-users-main-container">
      <ul class="new-users-list">
        <c:forEach var="user" items="${newUsers.users}">
          <li>
            <div class="user-card" rel="${user.id}">
              <c:if test="${newUsers.avatar}">
                <div class="avatar"><view:image src="${user.avatar}" alt="avatar" size="x140"/></div>
              </c:if>
              <span class="field userToZoom" rel="${user.id}">${user.firstName} ${user.lastName}</span>
              <c:forEach var="field" items="${newUsers.fields}">
                <span class="field ${field}"></span>
              </c:forEach>
            </div>
          </li>
        </c:forEach>
      </ul>
    </div>
  </div>
</c:if>
