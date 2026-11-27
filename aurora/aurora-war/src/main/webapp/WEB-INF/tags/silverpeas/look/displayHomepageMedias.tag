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
<%@ tag pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="silverpeas.tags.viewGenerator" prefix="view" %>
<%@ attribute name="medias" required="true" type="java.util.List" %>
<%@ attribute name="title" required="true" type="java.lang.String" %>
<%@ attribute name="display" required="true" type="java.lang.String" %>
<%@ attribute name="id" required="true" type="java.lang.String" %>
<view:setConstant var="homepageMediaResolution" constant="org.silverpeas.components.gallery.constant.MediaResolution.PREVIEW"/>
<section class="secteur-container homepage-medias" data-display="${display}" id="<c:out value='${id}'/>">
  <h4><c:out value="${title}"/></h4>
  <c:choose>
    <c:when test="${empty medias}"><p>Aucun média à afficher.</p></c:when>
    <c:otherwise>
      <c:if test="${display == 'carousel'}">
        <div class="homepage-medias-controls" hidden>
          <button type="button" class="simple-button" data-direction="-1" aria-label="Médias précédents">&#8592;</button>
          <button type="button" class="simple-button" data-direction="1" aria-label="Médias suivants">&#8594;</button>
        </div>
      </c:if>
      <ul class="homepage-medias-list" tabindex="${display == 'carousel' ? '0' : '-1'}" aria-label="Derniers médias">
        <c:forEach var="media" items="${medias}">
          <li><a href="<c:out value='${media.permalink}'/>">
            <img src="<c:out value='${media.getApplicationThumbnailUrl(homepageMediaResolution)}'/>" alt="" loading="lazy">
            <span><c:out value="${media.title}"/></span>
          </a></li>
        </c:forEach>
      </ul>
    </c:otherwise>
  </c:choose>
</section>
