<%--

    Copyright (C) 2000 - 2018 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory" %>
<%@ page import="org.silverpeas.core.web.look.LookHelper" %>
<%@ page import="org.silverpeas.core.util.StringUtil" %>
<%@ page import="org.silverpeas.core.util.URLUtil" %>
<%@ page import="org.silverpeas.looks.aurora.LookAuroraHelper" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>

<c:set var="lookHelper" value="${sessionScope['Silverpeas_LookHelper']}"/>
<jsp:useBean id="lookHelper" type="org.silverpeas.looks.aurora.LookAuroraHelper"/>
<view:setBundle bundle="${lookHelper.localizedBundle}"/>
<c:set var="settings" value="${lookHelper.lookSettings}"/>

<fmt:message var="labelLoading" key="look.loading"/>
<fmt:message var="labelPersonal" key="look.personalSpace"/>
<fmt:message var="labelPersonalAdd" key="look.personalSpace.add"/>
<fmt:message var="labelPersonalRemove" key="look.personalSpace.remove.confirm"/>
<fmt:message var="labelPersonalSelect" key="look.personalSpace.select"/>

<%
GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);
LookHelper helper = LookAuroraHelper.getLookHelper(session);

String spaceId    	= request.getParameter("privateDomain");
String subSpaceId   = request.getParameter("privateSubDomain");
String componentId  = request.getParameter("component_id");
boolean displayPersonalSpace = StringUtil.getBooleanValue(request.getParameter("FromMySpace"));

if (!StringUtil.isDefined(spaceId) && StringUtil.isDefined(componentId)) {
  spaceId = helper.getSpaceId(componentId);
} else if (StringUtil.isDefined(subSpaceId)) {
  spaceId = subSpaceId;
}
gef.setSpaceIdForCurrentRequest(spaceId);

%>
<script type="text/javascript">
  if (navigator.userAgent.match(/(android|iphone|ipad|blackberry|symbian|symbianos|symbos|netfront|model-orange|javaplatform|iemobile|windows phone|samsung|htc|opera mobile|opera mobi|opera mini|presto|huawei|blazer|bolt|doris|fennec|gobrowser|iris|maemo browser|mib|cldc|minimo|semc-browser|skyfire|teashark|teleca|uzard|uzardweb|meego|nokia|bb10|playbook)/gi)) {
    if ( ((screen.width  >= 480) && (screen.height >= 800)) || ((screen.width  >= 800) && (screen.height >= 480)) || navigator.userAgent.match(/ipad/gi) ) {
      var ss = document.createElement("link");
      ss.type = "text/css";
      ss.rel = "stylesheet";
      ss.href = webContext+"/util/styleSheets/domainsBar-tablette.css";
      document.getElementsByTagName("head")[0].appendChild(ss);
    }
  }

  function reloadTopBar(reload) {
    if (reload) {
      top.topFrame.location.href=getContext()+getTopBarPage();
    }
  }

  // Callback methods to navigation.js
    function getContext() {
      return webContext;
    }

    function getHomepage() {
      return "${settings.defaultHomepageURL}";
    }

    function getPersoHomepage() {
      return "${settings.personalHomepageURL}";
    }

    function getSpaceIdToInit() {
      return "<%=spaceId%>";
    }

    function getComponentIdToInit() {
      return "<%=componentId%>";
    }

    function displayComponentsIcons() {
      return ${settings.displayAppIcons};
    }

    function getPDCLabel() {
      return 'useless';
    }

    function getLook() {
      return "<%=gef.getCurrentLookName()%>";
    }

    function getSpaceWithCSSToApply() {
      return "${lookHelper.spaceWithCSSToApply}";
    }

    function displayPDC() {
        return "${settings.displayPDCInNavigationFrame}";
    }

    function displayContextualPDC() {
        return ${settings.displayContextualPDC};
    }

    function getTopBarPage() {
        return "/look/jsp/TopBar.jsp";
    }

    /**
     * Reload bottom frame
     */
    function reloadSpacesBarFrame(tabId) {
      spLayout.getBody().load({
        "UserMenuDisplayMode" : tabId
      });
    }

    function getPersonalSpaceLabels() {
        var labels = new Array(2);
        labels[0] = "${silfn:escapeJs(labelPersonalSelect)}";
        labels[1] = "${silfn:escapeJs(labelPersonalRemove)}";
        labels[2] = "${silfn:escapeJs(labelPersonalAdd)}";
        return labels;
    }

    function notifyAdministrators() {
      SP_openWindow('/silverpeas/RnotificationUser/jsp/Main?popupMode=Yes&editTargets=No&theTargetsUsers=Administrators', 'notifyUserPopup', '900', '400', 'menubar=no,scrollbars=no,statusbar=no');
    }

    function openClipboard() {
      sp.formRequest('${silfn:applicationURL()}<%=URLUtil.getURL(URLUtil.CMP_CLIPBOARD)%>Idle.jsp')
          .withParam('message','SHOWCLIPBOARD')
          .toTarget('IdleFrame')
          .submit();
    }

  /**
   * Using "jQuery" instead of "$" at this level prevents of getting conficts with another
   * javascript plugin.
   */
  jQuery(document).ready(function() {
	  <% if (displayPersonalSpace) { %>
	    jQuery("#spacePerso .spaceURL").css("display", "block");
      openMySpace({
        itemIdToSelect : '<%=componentId%>'
      });
	  <% } %>
  });

</script>
<div class="fondDomainsBar">
<div id="domainsBar">
  <div id="spaceTransverse"></div>
  <div id="basSpaceTransverse">
        <table border="0" cellpadding="0" cellspacing="0" width="100%">
            <tr>
                <td class="basSpacesGauche"><img src="icons/1px.gif" width="8" height="8" alt=""/></td>
                <td class="basSpacesMilieu"><img src="icons/1px.gif" width="8" height="8" alt=""/></td>
                <td class="basSpacesDroite"><img src="icons/1px.gif" width="8" height="8" alt=""/></td>
            </tr>
        </table>
    </div>
    <div id="spaceMenuDivId">
      <div id="spaces">
		  <br/><br/>${labelLoading}<br/><br/><img src="icons/inProgress.gif" alt="${labelLoading}"/>
      </div>
      <c:if test="${not lookHelper.anonymousAccess}">
        <div id="spacePerso" class="spaceLevelPerso"><a class="spaceURL" href="javaScript:openMySpace();">${labelPersonal}</a></div>
      </c:if>
    </div>
    <div id="basSpaces">
        <table border="0" cellpadding="0" cellspacing="0" width="100%">
            <tr>
                <td class="basSpacesGauche"><img src="icons/1px.gif" width="8" height="8" alt=""/></td>
                <td class="basSpacesMilieu"><img src="icons/1px.gif" width="8" height="8" alt=""/></td>
                <td class="basSpacesDroite"><img src="icons/1px.gif" width="8" height="8" alt=""/></td>
            </tr>
        </table>
    </div>

</div>

  <!-- Custom domains bar javascript -->
  <view:loadScript src="/util/javaScript/lookV5/navigation.js"/>
  <view:loadScript src="/util/javaScript/lookV5/personalSpace.js"/>
  <view:loadScript src="/util/javaScript/lookV5/login.js"/>
</div>