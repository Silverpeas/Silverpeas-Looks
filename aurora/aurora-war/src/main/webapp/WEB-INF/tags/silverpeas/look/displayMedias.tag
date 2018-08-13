<%--
  ~ Copyright (C) 2000 - 2018 Silverpeas
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
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/gallery" prefix="gallery" %>

<view:setConstant var="RESOLUTION" constant="org.silverpeas.components.gallery.constant.MediaResolution.LARGE"/>

<c:set var="lookHelper" value="${sessionScope['Silverpeas_LookHelper']}"/>
<view:setBundle bundle="${lookHelper.localizedBundle}"/>

<%@ attribute name="medias"
              required="true"
              type="java.util.List" %>

<fmt:message var="labelMedias" key="look.home.medias"/>

<c:if test="${not empty medias}">
  <div id="rssNews" class="feed-home secteur-container">
    <div class="header">
      <h4>${labelMedias}</h4>
    </div>
    <div class="portlet-content">
      <ul id="medias-slider">
        <c:forEach var="media" items="${medias}">
          <li>
            <a href="${media.permalink}" class="sp-permalink">
            <gallery:displayMediaInAlbumContent media="${media}" mediaResolution="${RESOLUTION}" isPortletDisplay="true"/>
            </a>
          </li>
        </c:forEach>
      </ul>
    </div>
  </div>

  <script type="text/javascript">
    $(document).ready(function() {
        $("#medias-slider").responsiveSlides({
          auto: true,
          pager: false,
          nav: false,
          speed: 500,
          pause: true,
          timeout: 6000
        });
    });
  </script>
</c:if>