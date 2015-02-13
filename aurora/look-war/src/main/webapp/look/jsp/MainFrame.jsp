<%@page import="com.stratelia.webactiv.beans.admin.Admin"%>
<%@page import="org.silverpeas.looks.aurora.Heading"%>
<%@page import="org.silverpeas.looks.aurora.LookAuroraHelper"%>
<%@page import="com.stratelia.webactiv.beans.admin.SpaceInst"%>
<%
	response.setHeader( "Expires", "Tue, 21 Dec 1993 23:59:59 GMT" );
	response.setHeader( "Pragma", "no-cache" );
	response.setHeader( "Cache-control", "no-cache" );
	response.setHeader( "Last-Modified", "Fri, Jan 25 2099 23:59:59 GMT" );
	response.setStatus( HttpServletResponse.SC_CREATED );
%>
<%@ include file="../../admin/jsp/importFrameSet.jsp" %>

<%@ page import="com.silverpeas.util.StringUtil"%>
<%@page import="java.util.List"%>

<%
String			componentIdFromRedirect = (String) session.getAttribute("RedirectToComponentId");
String			spaceIdFromRedirect 	= (String) session.getAttribute("RedirectToSpaceId");
if (!StringUtil.isDefined(spaceIdFromRedirect)) {
	spaceIdFromRedirect 	= request.getParameter("RedirectToSpaceId");
}
String			attachmentId		 	= (String) session.getAttribute("RedirectToAttachmentId");
ResourceLocator generalMessage			= new ResourceLocator("com.stratelia.webactiv.multilang.generalMultilang", language);
String			topBarParams			= "";
String			frameBottomParams		= "";
boolean			login					= StringUtil.getBooleanValue(request.getParameter("Login"));

if (m_MainSessionCtrl == null) {
%>
	<script> 
		top.location="../../Login.jsp";
	</script>
<%
} else {	
  	LookAuroraHelper helper = (LookAuroraHelper) session.getAttribute("Silverpeas_LookHelper");
	if (helper == null) {
		helper = new LookAuroraHelper(session);
		helper.setMainFrame("MainFrame.jsp");
		
		session.setAttribute("Silverpeas_LookHelper", helper);
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
		
		String 	workSpace 	= "?SpaceId="+spaceId;
		frameBottomParams 	= workSpace;
	} else {
		helper.setComponentIdAndSpaceIds(null, null, componentIdFromRedirect);
		frameBottomParams 	= "?SpaceId=&ComponentId="+componentIdFromRedirect;
	}
	
	if (login) {
		frameBottomParams += "&Login=1";
	}
	
	if (!"MainFrame.jsp".equalsIgnoreCase(helper.getMainFrame())) {
		session.setAttribute("RedirectToSpaceId", spaceIdFromRedirect);
		%>
			<script type="text/javascript">
				top.location="<%=helper.getMainFrame()%>";
			</script>
		<%
	}
	
	List<SpaceInst> spaces = null;
  	if (StringUtil.isDefined(componentIdFromRedirect)) {
  		spaces = helper.getOrganizationController().getSpacePathToComponent(componentIdFromRedirect);
  	} else if (StringUtil.isDefined(spaceId)) {
  	  	spaces = helper.getOrganizationController().getSpacePath(spaceId);
  	}
  	if (spaces != null) {
  	  spaceId = Admin.SPACE_KEY_PREFIX+spaces.get(0).getId();
  	}
	
  	String bannerHeight = helper.getSettings("banner.height", "115");
  	String footerHeight = helper.getSettings("footer.height", "26");
  	String framesetRows = bannerHeight+",100%,*,*,*";
  	if (helper.displayPDCFrame()) {
        framesetRows = bannerHeight+",100%,"+footerHeight+",*,*,*";
  	}
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Frameset//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=generalMessage.getString("GML.popupTitle")%></title>
</head>
<% if (attachmentId != null) {
   	session.setAttribute("RedirectToAttachmentId", null);
%>
	<script type="text/javascript">
		SP_openWindow('<%=m_sContext%>/File/<%=attachmentId%>', 'Fichier', '800', '600', 'directories=0,menubar=1,toolbar=1,scrollbars=1,location=1,alwaysRaised');
	</script>
<% } %>

<frameset rows="<%=framesetRows%>">
  	<frame src="TopBar.jsp?Heading=<%=spaceId %>" name="topFrame" marginwidth="0" marginheight="0" scrolling="no" noresize="noresize" frameborder="0"/>
  	<frame src="frameBottom.jsp<%=frameBottomParams%>" name="bottomFrame" marginwidth="0" marginheight="0" scrolling="auto" noresize="noresize" frameborder="0" />
  	<frame src="../../clipboard/jsp/IdleSilverpeasV5.jsp" name="IdleFrame" marginwidth="0" marginheight="0" scrolling="no" noresize="noresize" frameborder="0"/>
	<frame src="javascript.htm" name="scriptFrame" marginwidth="0" marginheight="0" scrolling="no" noresize="noresize" frameborder="0"/>
	<frame src="<%=m_sContext%>/Ragenda/jsp/importCalendar" name="importFrame" marginwidth="0" marginheight="0" scrolling="no" noresize="noresize" frameborder="0"/>
	<noframes></noframes>
</frameset>
</html>
<% } %>