<%--
 * Copyright (C) 2000 - 2026 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 --%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="org.silverpeas.core.contribution.content.form.PagesContext" %>
<%@ page import="org.silverpeas.core.contribution.content.form.Form" %>
<%@ page import="org.silverpeas.core.admin.user.model.User" %>
<%@ page import="org.silverpeas.looks.aurora.homepage.HomepageContentStore" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<%@ taglib uri="silverpeas.tags.viewGenerator" prefix="view" %>
<c:set var="lookHelper" value="${sessionScope['Silverpeas_LookHelper']}"/>
<view:setBundle bundle="${lookHelper.localizedBundle}"/>
<fmt:message var="labelSearchButton" key="look.home.search.button"/>
<fmt:message var="labelSearchInput" key="look.home.search.input"/>
<c:set var="advanced" value="${searchWidget.searchConfiguration.mode == 'advanced'}"/>
<c:url var="searchAction" value="${advanced ? '/RpdcSearch/jsp/XMLSearch' : '/RpdcSearch/jsp/AdvancedSearch'}"/>
<view:sp-page>
  <view:sp-head-part>
    <view:includePlugin name="pdc"/>
    <view:link href="/look/jsp/css/homepage-search.css?version=15592-search-v1"/>
    <view:script src="/look/jsp/js/homepage-search-frame.js?version=15592-search-v1"/>
  </view:sp-head-part>
  <view:sp-body-part cssClass="homepage-search-document">
    <c:choose>
      <c:when test="${advanced and empty searchForm}">
        <p>Le formulaire de recherche configuré est indisponible.</p>
      </c:when>
      <c:otherwise>
        <form id="homepage-search-form" name="TemplateSearch" method="${advanced ? 'post' : 'get'}"
            action="<c:out value='${searchAction}'/>" target="_parent"
            data-mode="${searchWidget.searchConfiguration.mode}"
            data-labels-inside="${searchWidget.searchConfiguration.labelsInside}">
          <label for="query" class="accessibility">Recherche</label>
          <input type="text" id="query" name="${advanced ? 'TitleNotInXMLForm' : 'query'}"
              autocomplete="off" placeholder="<c:out value='${labelSearchInput}'/>"/>
          <input type="hidden" name="mode" value="clear"/>
          <c:choose>
            <c:when test="${advanced}">
              <input type="hidden" name="xmlSearchSelectedForm" value="<c:out value='${searchForm.formName}'/>.xml"/>
              <%
                Form form = (Form) request.getAttribute("searchForm");
                HomepageContentStore.SearchConfiguration configuration = ((HomepageContentStore.Widget) request.getAttribute("searchWidget")).searchConfiguration();
                PagesContext formContext = new PagesContext();
                formContext.setLanguage(User.getCurrentRequester().getUserPreferences().getLanguage());
                formContext.setUserId(User.getCurrentRequester().getId());
                formContext.setFormName("TemplateSearch");
                formContext.setExtraSearchFieldPeriod(configuration.period());
                formContext.setExtraSearchFieldSpace(configuration.space());
                form.display(out, formContext);
              %>
            </c:when>
            <c:otherwise>
              <input type="hidden" name="AxisValueCouples"/>
              <fieldset id="used_pdc" class="skinFieldset"></fieldset>
            </c:otherwise>
          </c:choose>
          <button type="submit" class="sp_button"><c:out value="${labelSearchButton}"/></button>
        </form>
      </c:otherwise>
    </c:choose>
  </view:sp-body-part>
</view:sp-page>
