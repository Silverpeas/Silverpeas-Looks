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

<%@ attribute name="subspaces"
              required="true"
              type="java.util.List" %>

<%@ attribute name="apps"
              required="true"
              type="java.util.List" %>

<fmt:message var="labelNews" key="lookSilverpeasV5.homepage.space.publications"/>

<script type="text/javascript">
  <!--
  $(document).ready(function() {
    // if at least one item have got a description
    if ($.trim($(".spaceNavigation li p").text()).length !== 0) {
      $(".spaceNavigation li").css("min-height", "43px");
      $(".spaceNavigation li").css("line-height", "40px");
    }
  });
  -->
</script>

<c:if test="${not empty apps || not empty subspaces}">
<div class="spaceNavigation">
  <ul>
    <c:forEach var="subspace" items="${subspaces}">
      <li class="browse-space bgDegradeGris" onclick="goToSpaceItem('${subspace.id}')">
        <div>
          <a href="javascript:void(0)">${subspace.name}</a>
          <c:if test="${not empty subspace.description}">
            <p>${subspace.description}</p>
          </c:if>
        </div>
      </li>
    </c:forEach>
    <c:forEach var="app" items="${apps}">
      <li class="browse-component bgDegradeGris" onclick="goToComponentItem('${app.id}')">
        <div>
          <img src="${app.bigIcon}"/>
          <a href="javascript:void(0)">${app.label}</a>
          <c:if test="${not empty app.description}">
            <p>${app.description}</p>
          </c:if>
        </div>
      </li>
    </c:forEach>
  </ul>
</div>
</c:if>