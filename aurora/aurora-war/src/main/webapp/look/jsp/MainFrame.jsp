<%@page import="org.silverpeas.looks.aurora.LookAuroraHelper"%>
<%@ page import="org.silverpeas.core.util.StringUtil" %>
<%@ page import="org.silverpeas.core.util.ResourceLocator" %>
<%@ page import="org.silverpeas.core.util.LocalizationBundle" %>
<%@ page import="org.silverpeas.core.web.look.LookHelper" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/chat" prefix="chatTags" %>

<%
	response.setHeader( "Expires", "Tue, 21 Dec 1993 23:59:59 GMT" );
	response.setHeader( "Pragma", "no-cache" );
	response.setHeader( "Cache-control", "no-cache" );
	response.setHeader( "Last-Modified", "Fri, Jan 25 2099 23:59:59 GMT" );
	response.setStatus( HttpServletResponse.SC_CREATED );
%>
<%@ include file="../../admin/jsp/importFrameSet.jsp" %>


<%
String			componentIdFromRedirect = (String) session.getAttribute("RedirectToComponentId");
String			spaceIdFromRedirect 	= (String) session.getAttribute("RedirectToSpaceId");
if (!StringUtil.isDefined(spaceIdFromRedirect)) {
	spaceIdFromRedirect 	= request.getParameter("RedirectToSpaceId");
}
String			attachmentId		 	= (String) session.getAttribute("RedirectToAttachmentId");
LocalizationBundle generalMessage			= ResourceLocator.getGeneralLocalizationBundle(language);
StringBuilder			frameBottomParams		= new StringBuilder().append("{");
boolean			login					= StringUtil.getBooleanValue(request.getParameter("Login"));
String thisFrame = "/look/jsp/MainFrame.jsp";

if (m_MainSessionCtrl == null) { %>
  <script type="text/javascript">
    top.location = '<c:url value="/Login"/>';
  </script>
<% } else {
  LookHelper helper = LookHelper.getLookHelper(session);
	if (helper == null) {
	  helper = LookAuroraHelper.newLookHelper(session);
	  login = true;
	}
	
	boolean componentExists = false;
	if (StringUtil.isDefined(componentIdFromRedirect)) {
		componentExists = (organizationCtrl.getComponentInstLight(componentIdFromRedirect) != null);
	}
		
	String spaceId = "";
	if (!componentExists) {
		boolean spaceExists = false;
		if (StringUtil.isDefined(spaceIdFromRedirect)) {
			spaceExists = (organizationCtrl.getSpaceInstById(spaceIdFromRedirect) != null);
		}
		
		if (spaceExists) {
			spaceId = spaceIdFromRedirect;
		} else {
			if (helper != null && helper.getSpaceId() != null) {
				spaceId = helper.getSpaceId();
			}
		}
		helper.setSpaceIdAndSubSpaceId(spaceId);

    frameBottomParams.append("'SpaceId':'").append(spaceId).append("'");
	} else {
		helper.setComponentIdAndSpaceIds(null, null, componentIdFromRedirect);
    frameBottomParams.append("'SpaceId':''").append(",'ComponentId':'")
        .append(componentIdFromRedirect).append("'");
	}
	
	if (login) {
    frameBottomParams.append(",'Login':'1'");
	}
	
	if (!thisFrame.equalsIgnoreCase(helper.getMainFrame())) {
		session.setAttribute("RedirectToSpaceId", spaceIdFromRedirect);
		%>
			<script type="text/javascript">
				top.location="<%=helper.getMainFrame()%>";
			</script>
		<%
	}
	
  	String bannerHeight = helper.getSettings("banner.height", "115") + "px";
  	String footerHeight = helper.getSettings("footer.height", "26") + "px";
    if (!helper.displayPDCFrame()) {
      footerHeight = "0px";
    }

      String wallPaper = helper.getSpaceWallPaper();
      if (wallPaper == null) {
        wallPaper = gef.getIcon("banner.wallPaper");
      }
      if (wallPaper == null) {
        wallPaper = "imgDesign/bandeau.jpg";
      }

    %>

<c:set var="pdcActivated" value="<%=helper.displayPDCFrame()%>"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title><%=generalMessage.getString("GML.popupTitle")%></title>
  <view:link href="/look/jsp/css/aurora.css"/>
  <view:looknfeel/>
  <style type="text/css">
    body {
      margin: 0;
      padding: 0;
      border: none;
      overflow: hidden;
    }

    .hidden-part {
      margin: 0;
      padding: 0;
      border: none;
      display: none;
    }

    #sp-layout-main {
      width: 100%;
      display: flex;
      flex-wrap: wrap;
      flex-direction: column;
    }

    #sp-layout-header-part, #sp-layout-body-part, #sp-layout-footer-part {
      padding: 0;
      margin: 0;
      border: none;
    }

    #sp-layout-header-part {
      width: 100%;
      height: <%=bannerHeight%>;
      background-image: url(<%=wallPaper%>) !important;
    }

    #sp-layout-footer-part {
      width: 100%;
      height: <%=footerHeight%>;
    }

    #sp-layout-body-part {
      width: 100%;
      display: table;
    }
  </style>
  <meta name="viewport" content="initial-scale=1.0"/>
</head>
<body>
<% if (attachmentId != null) {
   	session.setAttribute("RedirectToAttachmentId", null);
%>
	<script type="text/javascript">
		SP_openWindow('<%=m_sContext%>/File/<%=attachmentId%>', 'Fichier', '800', '600', 'directories=0,menubar=1,toolbar=1,scrollbars=1,location=1,alwaysRaised');
	</script>
<% } %>

<div id="sp-layout-main">
  <div id="sp-layout-header-part"></div>
  <div id="sp-layout-body-part"></div>
  <div id="sp-layout-footer-part" style="display: none"></div>
</div>
<div class="hidden-part" style="height: 0">
  <iframe src="../../clipboard/jsp/Idle.jsp" name="IdleFrame" marginwidth="0" marginheight="0" scrolling="no" frameborder="0"></iframe>
  <iframe src="<c:url value='/Ragenda/jsp/importCalendar'/>" name="importFrame" marginwidth="0" marginheight="0" scrolling="no" frameborder="0"></iframe>
</div>

<view:progressMessage/>
<script type="text/javascript">
  (function() {
    initializeSilverpeasLayout(<%=frameBottomParams.append('}')%>);
    spLayout.getBody().ready(function() {
      spLayout.getBody().getContent().addEventListener('load', function(event) {
        sp.log.debug("This is just a demonstration: it is possible to listen to events ('load', 'show', 'hide') dispatched from each part of the layout");
        sp.log.debug("On footer part could also be listen to events: 'pdcload', 'pdcshow' and 'pdchide'");
        sp.log.debug("The condition here (please consult the code if you are reading from the browser console!) is to ensure that the listener will not be declared several times.");
        sp.log.debug("Indeed, because of ajax reloading and according to the location of the event listener attachment, same treatment could be performed several times");
        // notySuccess("Body content event well performed!");
      }, 'silverpeas-main-ready');
    });
  })();
</script>
<chatTags:silverChatInitialization/>
</body>
</html>
<% } %>