<%--

    Copyright (C) 2000 - 2012 Silverpeas

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

<%@ page import="java.util.*"%>
<%@ page import="org.silverpeas.core.util.URLUtil" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory" %>
<%@ page import="org.silverpeas.core.web.look.LookHelper" %>
<%@ page import="org.silverpeas.core.util.StringUtil" %>
<%@ page import="org.silverpeas.core.util.ResourceLocator" %>
<%@ page import="org.silverpeas.core.util.SettingBundle" %>
<%@ page import="org.silverpeas.core.util.EncodeHelper" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%-- Set resource bundle --%>
<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle basename="org.silverpeas.lookSilverpeasV5.multilang.lookBundle"/>

<%
String m_sContext = URLUtil.getApplicationURL();
GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);
LookHelper helper = LookHelper.getLookHelper(session);

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
      ss.href = "<%=m_sContext%>/util/styleSheets/domainsBar-tablette.css";
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
      return "<%=m_sContext%>";
    }

    function getHomepage() {
      return "<%=helper.getSettings("defaultHomepage", "/dt")%>";
    }

    function getPersoHomepage() {
      return "<%=helper.getSettings("persoHomepage", "/dt")%>";
    }

    function getSpaceIdToInit() {
      return "<%=spaceId%>";
    }

    function getComponentIdToInit() {
      return "<%=componentId%>";
    }

    function displayComponentsIcons() {
      return <%=helper.getSettings("displayComponentIcons")%>;
    }

    function getPDCLabel() {
      return '<fmt:message key="lookSilverpeasV5.pdc" />';
    }

    function getLook() {
      return "<%=gef.getCurrentLookName()%>";
    }

    function getSpaceWithCSSToApply() {
      return "<%=helper.getSpaceWithCSSToApply()%>";
    }

    function displayPDC() {
        return "<%=helper.displayPDCInNavigationFrame()%>";
    }

    function displayContextualPDC() {
        return <%=helper.displayContextualPDC()%>;
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
        labels[0] = "<%=EncodeHelper.javaStringToJsString(helper.getString("lookSilverpeasV5.personalSpace.select"))%>";
        labels[1] = "<%=EncodeHelper.javaStringToJsString(helper.getString("lookSilverpeasV5.personalSpace.remove.confirm"))%>";
        labels[2] = "<%=EncodeHelper.javaStringToJsString(helper.getString("lookSilverpeasV5.personalSpace.add"))%>";
        return labels;
    }

    function notifyAdministrators() {
      SP_openWindow('/silverpeas/RnotificationUser/jsp/Main?popupMode=Yes&editTargets=No&theTargetsUsers=Administrators', 'notifyUserPopup', '700', '400', 'menubar=no,scrollbars=no,statusbar=no');
    }

  /**
   * Using "jQuery" instead of "$" at this level prevents of getting conficts with another
   * javascript plugin.
   */
  jQuery(document).ready(function() {
	  <% if (displayPersonalSpace) { %>
	    jQuery("#spacePerso .spaceURL").css("display", "block");
	    openMySpace();
	  <% } %>
  });

</script>
<div class="fondDomainsBar">
<div id="domainsBar">
  <div id="spaceTransverse"></div>
  <div id="basSpaceTransverse">
        <table border="0" cellpadding="0" cellspacing="0" width="100%">
            <tr>
                <td class="basSpacesGauche"><img src="<%=m_sContext%>/admin/jsp/icons/silverpeasV5/px.gif" width="8" height="8" alt=""/></td>
                <td class="basSpacesMilieu"><img src="<%=m_sContext%>/admin/jsp/icons/silverpeasV5/px.gif" width="8" height="8" alt=""/></td>
                <td class="basSpacesDroite"><img src="<%=m_sContext%>/admin/jsp/icons/silverpeasV5/px.gif" width="8" height="8" alt=""/></td>
            </tr>
        </table>
    </div>
    <div id="spaceMenuDivId">
      <div id="spaces">
		  <br/><br/><fmt:message key="lookSilverpeasV5.loadingSpaces" /><br/><br/><img src="<%=m_sContext%>/admin/jsp/icons/silverpeasV5/inProgress.gif" alt="<fmt:message key="lookSilverpeasV5.loadingSpaces" />"/>
	  </div>
      <% if (!helper.isAnonymousAccess()) { %>
        <div id="spacePerso" class="spaceLevelPerso"><a class="spaceURL" href="javaScript:openMySpace();"><fmt:message key="lookSilverpeasV5.PersonalSpace" /></a></div>
      <% } %>
    </div>
    <div id="basSpaces">
        <table border="0" cellpadding="0" cellspacing="0" width="100%">
            <tr>
                <td class="basSpacesGauche"><img src="<%=m_sContext%>/admin/jsp/icons/silverpeasV5/px.gif" width="8" height="8" alt=""/></td>
                <td class="basSpacesMilieu"><img src="<%=m_sContext%>/admin/jsp/icons/silverpeasV5/px.gif" width="8" height="8" alt=""/></td>
                <td class="basSpacesDroite"><img src="<%=m_sContext%>/admin/jsp/icons/silverpeasV5/px.gif" width="8" height="8" alt=""/></td>
            </tr>
        </table>
    </div>

</div>
<form name="clipboardForm" action="<%=m_sContext+URLUtil.getURL(URLUtil.CMP_CLIPBOARD)%>Idle.jsp" method="post" target="IdleFrame">
<input type="hidden" name="message" value="SHOWCLIPBOARD"/>
</form>

  <!-- Custom domains bar javascript -->
  <view:script src="/util/javaScript/lookV5/navigation.js"/>
  <view:script src="/util/javaScript/lookV5/personalSpace.js"/>
  <view:script src="/util/javaScript/lookV5/login.js"/>
</div>