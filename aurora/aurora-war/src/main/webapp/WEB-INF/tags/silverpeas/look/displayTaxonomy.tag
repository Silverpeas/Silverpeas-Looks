<%--
  ~ Copyright (C) 2000 - 2024 Silverpeas
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

<%@ attribute name="enabled"
              required="true"
              type="java.lang.Boolean" %>

<%@ attribute name="spaceId"
              required="false"
              type="java.lang.String" %>

<%@ attribute name="labelsInsideSelect"
              required="true"
              type="java.lang.Boolean" %>

<%@ attribute name="scope"
              required="false"
              type="java.lang.String" %>

<c:if test="${empty scope}">
  <c:set var="scope" value="1"/>
</c:if>

<c:set var="scopeRestrictedToCurrentSpace" value="${not empty spaceId && scope == '1'}"/>

<fmt:message var="labelSearch" key="look.home.search.title"/>
<fmt:message var="labelSearchButton" key="look.home.search.button"/>
<fmt:message var="labelSearchInput" key="look.home.search.input"/>

<c:if test="${enabled}">
  <div class="secteur-container search" id="bloc-advancedSeach">
    <h4>${labelSearch}</h4>
    <c:url var="searchActionURL" value="/RpdcSearch/jsp/AdvancedSearch"/>
    <form method="get" action="javascript:document.querySelector('#submit-AdvancedSearch').click()" name="AdvancedSearch">
      <input type="text" id="query" value="" size="60" name="query" autocomplete="off" class="ac_input" placeholder="${labelSearchInput}"/>
      <input type="hidden" name="AxisValueCouples"/><input type="hidden" name="mode" value="clear"/>
      <c:if test="${scopeRestrictedToCurrentSpace}">
        <input type="hidden" name="spaces" value="${spaceId}"/>
      </c:if>
      <fieldset id="used_pdc" class="skinFieldset"></fieldset>
      <a id="submit-AdvancedSearch" href="javascript:search()"><span>${labelSearchButton}</span></a>
      <input type="submit" class="hide"/>
    </form>
  </div>

<script type="text/javascript">
function search() {
  var values = $('#used_pdc').pdc('selectedValues');
  if (values.length > 0) {
    document.AdvancedSearch.AxisValueCouples.value = values.flatten();
  }
  document.AdvancedSearch.action = '${searchActionURL}';
  document.AdvancedSearch.submit();
}

whenSilverpeasReady(function() {
  $('#used_pdc').pdc('used', {
    <c:if test="${scopeRestrictedToCurrentSpace}">
      workspace : '${spaceId}',
    </c:if>
    axisTypeDisplay : false,
    labelInsideSelect : ${labelsInsideSelect}
  });
});
</script>
</c:if>
