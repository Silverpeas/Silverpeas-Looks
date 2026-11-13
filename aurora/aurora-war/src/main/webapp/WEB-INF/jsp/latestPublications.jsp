<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/look" prefix="viewTags" %>
<viewTags:displayPublications lookHelper="${sessionScope.Silverpeas_LookHelper}"
    publications="${requestScope.publications}" embedded="${true}"/>
