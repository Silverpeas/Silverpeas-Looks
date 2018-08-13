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

<%@ attribute name="shortcuts"
              required="true"
              type="java.util.List" %>

<fmt:message var="labelShortcuts" key="look.home.shortcuts"/>

<c:if test="${not empty shortcuts}">
  <div class="secteur-container cg-favorit" id="cg-favorit-home">
    <h4>${labelShortcuts}</h4>
    <div class="cg-favorit-main-container">
      <ul class="cg-favorit-list">
        <c:forEach var="shortcut" items="${shortcuts}">
          <c:set var="className" value="sp-link"/>
          <c:if test="${shortcut.target == '_blank'}">
            <c:set var="className" value=""/>
          </c:if>
          <li><a class="${className}" href="${shortcut.url}" title="${shortcut.altText}" target="${shortcut.target}"><img alt="${shortcut.altText}" src="${shortcut.iconURL}" /> <span>${shortcut.altText}</span></a></li>
        </c:forEach>
      </ul>
    </div>
  </div>
</c:if>