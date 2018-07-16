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

<%@ attribute name="admins"
              required="true"
              type="java.util.List" %>

<fmt:message var="labelSpaceAdmins" key="look.space.home.admins"/>

<c:if test="${not empty admins}">
  <!-- gestionnaires -->
  <div class="secteur-container" id="spaceManager">
    <div class="header">
      <h2 class="portlet-title">${labelSpaceAdmins}</h2>
    </div>
    <div class="portlet-content">
      <ul class="list-responsible-user">
        <c:forEach var="admin" items="${admins}">
          <li class="intfdcolor">
            <div class="content">
              <div class="profilPhoto">
                <view:image css="avatar" src="${admin.avatar}" type="avatar"/></div>
              <div class="userName"><view:username userId="${admin.id}"/></div>
            </div>
          </li>
        </c:forEach>
      </ul>
      <br clear="all"/>
    </div>
  </div>
  <!-- /gestionnaires -->
</c:if>