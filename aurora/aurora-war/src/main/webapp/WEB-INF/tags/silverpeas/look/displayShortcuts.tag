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
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<%@ taglib uri="silverpeas.tags.viewGenerator" prefix="view" %>
<%@ taglib uri="silverpeas.tags.silverFunctions" prefix="silfn" %>

<c:set var="lookHelper" value="${sessionScope['Silverpeas_LookHelper']}"/>
<view:setBundle bundle="${lookHelper.localizedBundle}"/>

<%@ attribute name="shortcuts"
              required="true"
              type="java.util.List" %>

<%@ attribute name="id" required="false" type="java.lang.String" %>
<%@ attribute name="title" required="false" type="java.lang.String" %>

<fmt:message var="labelShortcuts" key="look.home.shortcuts"/>

<c:if test="${not empty shortcuts}">
  <div class="secteur-container ${empty id ? 'cg-favorit' : 'homepage-shortcuts'}" id="<c:out value='${empty id ? "cg-favorit-home" : id}'/>">
    <h4><c:out value="${empty title ? labelShortcuts : title}"/></h4>
    <div class="${empty id ? 'cg-favorit-main-container' : 'homepage-shortcuts-container'}">
      <ul class="${empty id ? 'cg-favorit-list' : 'homepage-shortcuts-list'}">
        <c:forEach var="shortcut" items="${shortcuts}">
          <c:set var="className" value="sp-link"/>
          <c:if test="${shortcut.target == '_blank'}">
            <c:set var="className" value=""/>
          </c:if>
          <li>
            <a class="${className}" href="<c:out value='${shortcut.url}'/>" title="<c:out value='${shortcut.altText}'/>" target="<c:out value='${shortcut.target}'/>" rel="${shortcut.target == '_blank' ? 'noopener noreferrer' : ''}">
              <c:if test="${not empty shortcut.iconURL}">
                <img alt="" src="<c:out value='${shortcut.iconURL}'/>" />
              </c:if>
              <span><c:out value="${empty shortcut.altText ? shortcut.url : shortcut.altText}"/></span>
            </a>
          </li>
        </c:forEach>
      </ul>
    </div>
  </div>
</c:if>