<%@page import="org.silverpeas.looks.aurora.LookAuroraHelper"%>
<%@ include file="../../admin/jsp/importFrameSet.jsp" %>

<%@ page import="com.stratelia.webactiv.beans.admin.SpaceInstLight"%>
<%@ page import="com.silverpeas.util.StringUtil"%>
<%@page import="java.util.List"%>

<%
String strGoToNew 	= (String) session.getAttribute("gotoNew");
String spaceId 		= request.getParameter("SpaceId");
String subSpaceId 	= request.getParameter("SubSpaceId");
String fromTopBar 	= request.getParameter("FromTopBar");
String componentId	= request.getParameter("ComponentId");
String login		= request.getParameter("Login");

String fromMySpace 	= request.getParameter("FromMySpace");

/*System.out.println("strGoToNew = "+strGoToNew);
System.out.println("spaceId = "+spaceId);
System.out.println("subSpaceId = "+subSpaceId);
System.out.println("fromTopBar = "+fromTopBar);
System.out.println("componentId = "+componentId);
System.out.println("login = "+login);
System.out.println("numRub = "+numRub);
System.out.println("fromMySpace = "+fromMySpace);*/

LookAuroraHelper helper = (LookAuroraHelper) session.getAttribute("Silverpeas_LookHelper");

int framesetWidth = helper.getSettings("domainsBarFramesetWidth", 260);

String paramsForDomainsBar = "";
if (fromTopBar != null && fromTopBar.equals("1")) {
	paramsForDomainsBar = (spaceId == null) ? "" : "?privateDomain="+spaceId+"&privateSubDomain="+subSpaceId+"&FromTopBar=1";
} else if (componentId != null) {
	paramsForDomainsBar = "?privateDomain=&component_id="+componentId;
} else {
	paramsForDomainsBar = "?privateDomain="+spaceId;
}

if ("1".equals(fromMySpace)) {
	paramsForDomainsBar += "&FromMySpace=1";
}

if (StringUtil.isDefined(strGoToNew) || StringUtil.isDefined(spaceId) || StringUtil.isDefined(subSpaceId) || StringUtil.isDefined(componentId)) {
  // ignore login page when try to access a direct resource
  login = null;
}

//Allow to force a page only on login and when user clicks on logo
boolean displayLoginHomepage = false;
String loginHomepage = helper.getLoginHomePage();
if (StringUtil.isDefined(loginHomepage) && StringUtil.isDefined(login)) {
  displayLoginHomepage = true; 
}

String frameURL = "";
if (displayLoginHomepage) {
	frameURL = loginHomepage;
} else if (strGoToNew==null) {
	if (StringUtil.isDefined(componentId)) {
		frameURL = URLManager.getApplicationURL()+URLManager.getURL(null, componentId)+"Main";
	} else {
		String homePage = helper.getSettings("defaultHomepage", "/dt");
		String param = "";
		if (spaceId != null && spaceId.length() >= 3){
		    param = "?SpaceId=" + spaceId;
		}
		frameURL = URLManager.getApplicationURL()+homePage+param;
	}
} else {
	frameURL = URLManager.getApplicationURL()+strGoToNew;
}

session.putValue("goto",null);
session.putValue("gotoNew", null);
session.putValue("RedirectToComponentId", null);
session.putValue("RedirectToSpaceId", null);

boolean hideMenu = "1".equals(fromTopBar) || "1".equals(login);

%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Frameset//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<script type="text/javascript">
var expandedCols = "<%=framesetWidth%>,*";
function hideFrame(){
  expandedCols = document.body.cols;
  document.body.cols = "10,*";
}

function showFrame() {
  document.body.cols = expandedCols;
}
</script>
</head>
<% if (hideMenu) { %>
	<frameset cols="*,100%">
		<frame src="DomainsBar.jsp<%=paramsForDomainsBar%>" marginwidth="0" marginheight="0" name="SpacesBar" frameborder="0" scrolling="no"/>
		<frame src="<%=frameURL%>" marginwidth="10" name="MyMain" marginheight="0" frameborder="0" scrolling="auto"/>
		<noframes></noframes>
	</frameset>
<% } else { %>
	<frameset cols="<%=framesetWidth%>,*">
		<frame src="DomainsBar.jsp<%=paramsForDomainsBar%>" name="SpacesBar" frameborder="0" scrolling="auto"/>
		<frame src="<%=frameURL%>" name="MyMain" marginheight="0" frameborder="0" scrolling="auto"/>
		<noframes></noframes>
  	</frameset>
<% } %>
</html>