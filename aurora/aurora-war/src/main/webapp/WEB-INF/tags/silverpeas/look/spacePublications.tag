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
<c:set var="language" value="${lookHelper.language}"/>

<%@ attribute name="publications"
              required="true"
              type="java.util.List" %>

<fmt:message var="labelNews" key="look.space.home.publications"/>

<c:if test="${not empty publications}">
<div class="bgDegradeGris portlet" id="publication">
  <div class="bgDegradeGris header">
    <h4 class="clean">${labelNews}</h4>
  </div>

  <ul id="publicationList">
    <c:forEach var="publication" items="${publications}">
    <li>
      <a class="sp-permalink" href="${publication.permalink}">
        <b>${publication.name}</b>
      </a>
      <view:username userId="${publication.updaterId}"/> - <view:formatDate value="${publication.updateDate}" language="${language}"/>
      <br/>${publication.description}
    </li>
    </c:forEach>
  </ul>

</div>
</c:if>