<%@page import="com.silverpeas.cg11.look.SecondaryHomePageData"%>
<%@ include file="importFrameSet.jsp" %>

<%@ page import="com.stratelia.webactiv.beans.admin.SpaceInstLight"%>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@ page import="com.silverpeas.cg11.look.LookCG11Helper"%>
<%@ page import="java.util.List"%>
<%@page import="com.silverpeas.util.StringUtil"%>
<%@page import="com.silverpeas.look.Shortcut"%>
<%@page import="com.stratelia.webactiv.util.publication.model.PublicationDetail"%>
<%@page import="com.stratelia.webactiv.beans.admin.ComponentInst"%>

<%
LookCG11Helper 	helper 	= (LookCG11Helper) session.getAttribute("Silverpeas_LookHelper");

String spaceId = helper.getSubSpaceId();

SecondaryHomePageData data = helper.getProjectHomePageData(spaceId);

SpaceInstLight space = helper.getOrganizationController().getSpaceInstLightById(spaceId);

String label = space.getDescription();
if (!StringUtil.isDefined(label)) {
	label = space.getName();
}
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>Projet <%=space.getName()%></title>
<%
	out.println(gef.getLookStyleSheet());
%>
<!--[if lte IE 6]>
<link rel="stylesheet" type="text/css" href="/weblib/cg11/look/css/ie6.css"/>
<![endif]-->
</head>
<body id="rose" class="<%=helper.getCSSClassNames()%>">
<div id="page">
	<div id="contenu" class="direction">
		<div id="zone_texte_direction">
			<div id="titre_page">
				<div>
				<h1><%=label%></h1>
				</div>
			</div>
			<div id="texte_direction">
				<h2><%=helper.getString("lookCG11.heading.about") %></h2>
				<%= data.getDescription() %>
			</div>
		</div>
		<div id="zone_droite_large">
			<div id="zone_photo_droite">
				<img src="<%=data.getImage() %>" width="229" height="338" alt="image projet" class="image" />
				<img src="/weblib/cg11/look/images/masque_direction.png" width="176" height="203" alt="masque" class="masque" />
			</div>
			<div id="menu_droite_petit">
				<div id="titre_menu_droite_petit"><%=helper.getString("lookCG11.heading.lastpublications") %></div>
				<div id="liens_menu_droite_petit">
					<ul>
					<%
						List<PublicationDetail> publications = helper.getLatestPublications(spaceId, 7);
						for (PublicationDetail publication : publications) {
					%>
						<li><a href="<%=URLManager.getSimpleURL(URLManager.URL_PUBLI, publication.getPK().getId()) %>"><%=publication.getName() %></a></li>
					<% } %>
					</ul>
				</div>
				<div id="bas_menu_droite_petit"><img src="/weblib/cg11/look/images/bas_menu_droite_petit.png" width="180" height="29" alt="bas menu" /></div>
			</div>
		</div>
		<div class="clear"></div>
	</div>
</div>
</body>
</html>