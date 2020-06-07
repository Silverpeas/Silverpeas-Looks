<%--
  ~ Copyright (C) 2000 - 2020 Silverpeas
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

<%@ tag import="org.silverpeas.core.contribution.content.form.PagesContext" %>
<%@ tag import="org.silverpeas.core.admin.user.model.User" %>

<c:set var="lookHelper" value="${sessionScope['Silverpeas_LookHelper']}"/>
<view:setBundle bundle="${lookHelper.localizedBundle}"/>

<%@ attribute name="searchForm"
              required="false"
              type="org.silverpeas.core.contribution.content.form.Form" %>

<fmt:message var="labelSearch" key="look.home.search.title"/>
<fmt:message var="labelSearchButton" key="look.home.search.button"/>
<fmt:message var="labelSearchInput" key="look.home.search.input"/>

<c:if test="${not empty searchForm}">
  <div class="secteur-container search" id="bloc-advancedSeach">
    <h4>${labelSearch}</h4>
    <c:url var="searchActionURL" value="/RpdcSearch/jsp/XMLSearch"/>
    <form method="post" action="${searchActionURL}" name="TemplateSearch" enctype="multipart/form-data">
      <input type="text" id="query" value="" size="60" name="TitleNotInXMLForm" onkeypress="checkEnter(event)" autocomplete="off" class="ac_input" placeholder="${labelSearchInput}"/>
      <input type="hidden" name="mode" value="clear"/>
      <input type="hidden" name="xmlSearchSelectedForm" value="<%=searchForm.getFormName()%>.xml"/>
      <%
        PagesContext formContext = new PagesContext();
        formContext.setLanguage(User.getCurrentRequester().getUserPreferences().getLanguage());
        searchForm.display(out, formContext);
      %>
      <a id="submit-AdvancedSearch" href="javascript:templateSearch()"><span>${labelSearchButton}</span></a>
    </form>
  </div>

<script type="text/javascript">
function templateSearch() {
  $.progressMessage();
  document.TemplateSearch.submit();
}
</script>
<view:progressMessage/>
</c:if>