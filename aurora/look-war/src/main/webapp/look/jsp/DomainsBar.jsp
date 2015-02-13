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
<%@ page import="com.silverpeas.util.StringUtil"%>
<%@ page import="com.silverpeas.util.EncodeHelper"%>
<%@ page import="com.stratelia.webactiv.util.*"%>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>

<%@ page import="com.silverpeas.look.LookHelper" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%-- Set resource bundle --%>
<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle basename="com.silverpeas.lookSilverpeasV5.multilang.lookBundle"/>

<%
String m_sContext = URLManager.getApplicationURL();
GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);
LookHelper helper = (LookHelper) session.getAttribute(LookHelper.SESSION_ATT);

String spaceId    	= request.getParameter("privateDomain");
String subSpaceId   = request.getParameter("privateSubDomain");
String componentId  = request.getParameter("component_id");
boolean displayPersonalSpace = StringUtil.getBooleanValue(request.getParameter("FromMySpace"));

if (!StringUtil.isDefined(spaceId) && StringUtil.isDefined(componentId)) {
  spaceId = helper.getSpaceId(componentId);
} else if (StringUtil.isDefined(subSpaceId)) {
  spaceId = subSpaceId;
}

ResourceLocator resourceSearchEngine = new ResourceLocator("com.stratelia.silverpeas.pdcPeas.settings.pdcPeasSettings", "");
int autocompletionMinChars = resourceSearchEngine.getInteger("autocompletion.minChars", 3);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title></title>
<view:looknfeel></view:looknfeel>
<!-- Add JQuery mask plugin css -->
<link href="<%=m_sContext%>/util/styleSheets/jquery.loadmask.css" rel="stylesheet" type="text/css" />
<link href="<%=m_sContext%>/util/styleSheets/jquery.autocomplete.css" rel="stylesheet" type="text/css" media="screen"/>

<!-- Add RICO javascript library -->
<script type="text/javascript" src="<%=m_sContext%>/util/ajax/prototype.js"></script>
<script type="text/javascript" src="<%=m_sContext%>/util/ajax/rico.js"></script>
<script type="text/javascript" src="<%=m_sContext%>/util/ajax/ricoAjax.js"></script>

<!-- Add jQuery javascript library -->
<script type="text/javascript" src="<%=m_sContext%>/util/javaScript/jquery/jquery.loadmask.js"></script>
<script type="text/javascript" src="<%=m_sContext%>/util/javaScript/jquery/jquery.autocomplete.js"></script>
<script type="text/javascript" src="<%=m_sContext%>/util/javaScript/jquery/jquery.bgiframe.min.js"></script>

<!-- Custom domains bar javascript -->
<script type="text/javascript" src="<%=m_sContext%>/util/javaScript/lookV5/navigation.js"></script>
<script type="text/javascript" src="<%=m_sContext%>/util/javaScript/lookV5/personalSpace.js"></script>
<script type="text/javascript" src="<%=m_sContext%>/util/javaScript/lookV5/login.js"></script>

<script type="text/javascript">
  function reloadTopBar(reload) {
    if (reload) {
      top.topFrame.location.href=getContext()+getTopBarPage();
    }
  }

  function checkSubmitToSearch(ev) {
    var touche = ev.keyCode;
    if (touche == 13) {
      searchEngine();
    }
  }

  function searchEngine() {
        if (document.searchForm.query.value != "")
        {
        document.searchForm.action = "<%=m_sContext%>/RpdcSearch/jsp/AdvancedSearch";
          document.searchForm.submit();
        }
  }

  function advancedSearchEngine(){
    document.searchForm.action = "<%=m_sContext%>/RpdcSearch/jsp/ChangeSearchTypeToExpert";
    document.searchForm.submit();
  }

  var navVisible = true;
  function resizeFrame() {
    parent.resizeFrame('10,*');
    if (navVisible)
    {
      document.body.scroll = "no";
      document.images['expandReduce'].src="<%=m_sContext%>/admin/jsp/icons/silverpeasV5/extend.gif";
    }
    else
    {
      document.body.scroll = "auto";
      document.images['expandReduce'].src="<%=m_sContext%>/admin/jsp/icons/silverpeasV5/reduct.gif";
    }
    document.images['expandReduce'].blur();
    navVisible = !navVisible;
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

    function getFooterPage() {
    	return getContext()+"/RpdcSearch/jsp/ChangeSearchTypeToExpert?SearchPage=/admin/jsp/pdcSearchSilverpeasV5.jsp&";
    }

    /**
     * Reload bottom frame
     */
    function reloadSpacesBarFrame(tabId) {
       top.bottomFrame.location.href="<%=m_sContext%>/admin/jsp/frameBottomSilverpeasV5.jsp?UserMenuDisplayMode=" + tabId;
    }

    function getPersonalSpaceLabels() {
        var labels = new Array(2);
        labels[0] = "<%=EncodeHelper.javaStringToJsString(helper.getString("lookSilverpeasV5.personalSpace.select"))%>";
        labels[1] = "<%=EncodeHelper.javaStringToJsString(helper.getString("lookSilverpeasV5.personalSpace.remove.confirm"))%>";
        labels[2] = "<%=EncodeHelper.javaStringToJsString(helper.getString("lookSilverpeasV5.personalSpace.add"))%>";
        return labels;
    }

  /**
   * Using "jQuery" instead of "$" at this level prevents of getting conficts with another
   * javascript plugin.
   */
  //used by keyword autocompletion
  jQuery(document).ready(function() {
	  <% if (displayPersonalSpace) { %>
	    jQuery("#spacePerso .spaceURL").css("display", "block");
	    openMySpace();
	  <% } %>
	  <%  if(resourceSearchEngine.getBoolean("enableAutocompletion", false)){ %>
    jQuery("#query").autocomplete("<%=m_sContext%>/AutocompleteServlet", {
      minChars : <%=autocompletionMinChars%>,
      max : 50,
      autoFill : false,
      mustMatch : false,
      matchContains : false,
      scrollHeight : 220
    });
    <%}%>
    
    
  });

</script>
</head>
<body class="fondDomainsBar">
<div id="redExp"><a href="javascript:resizeFrame();"><img src="<%=m_sContext%>/admin/jsp/icons/silverpeasV5/reduct.gif" border="0" name="expandReduce" alt="<fmt:message key="lookSilverpeasV5.reductExtend" />" title="<fmt:message key="lookSilverpeasV5.reductExtend" />"/></a></div>
<div id="domainsBar">
  <div id="recherche">
    <div id="submitRecherche">
      <form name="searchForm" action="<%=m_sContext%>/RpdcSearch/jsp/AdvancedSearch" method="post" target="MyMain">
      <input name="query" size="30" id="query"/>
      <input type="hidden" name="mode" value="clear"/>
      <a href="javascript:searchEngine()"><img src="<%=m_sContext%>/admin/jsp/icons/silverpeasV5/px.gif" width="20" height="20" border="0" alt=""/></a>
      </form>
    </div>
        <div id="bodyRecherche">
            <a href="javascript:advancedSearchEngine()"><fmt:message key="lookSilverpeasV5.AdvancedSearch" /></a> | <a href="<%=m_sContext%>/RpdcSearch/jsp/LastResults" target="MyMain"><fmt:message key="lookSilverpeasV5.LastSearchResults" /></a> | <a href="#" onclick="javascript:SP_openWindow('<%=m_sContext%>/RpdcSearch/jsp/help.jsp', 'Aide', '700', '220','scrollbars=yes, resizable, alwaysRaised');"><fmt:message key="lookSilverpeasV5.Help" /></a>
    </div>
    </div>
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
		<center><br/><br/><fmt:message key="lookSilverpeasV5.loadingSpaces" /><br/><br/><img src="<%=m_sContext%>/admin/jsp/icons/silverpeasV5/inProgress.gif" alt="<fmt:message key="lookSilverpeasV5.loadingSpaces" />"/></center>
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
<form name="clipboardForm" action="<%=m_sContext+URLManager.getURL(URLManager.CMP_CLIPBOARD)%>Idle.jsp" method="post" target="IdleFrame">
<input type="hidden" name="message" value="SHOWCLIPBOARD"/>
</form>
<!-- Form below is used only to refresh this page according to external link (ie search engine, homepage,...) -->
<form name="privateDomainsForm" action="DomainsBarSilverpeasV5.jsp" method="post">
<input type="hidden" name ="component_id"/>
<input type="hidden" name ="privateDomain"/>
<input type="hidden" name ="privateSubDomain"/>
</form>
</body>
</html>