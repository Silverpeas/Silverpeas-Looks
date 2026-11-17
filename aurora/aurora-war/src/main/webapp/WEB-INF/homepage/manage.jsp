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
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="silverpeas.tags.viewGenerator" prefix="view" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<view:sp-page>
  <view:sp-head-part>
    <title>Modifier la page d’accueil</title>
    <view:link href="/look/jsp/css/homepage-content.css"/>
  </view:sp-head-part>
  <view:sp-body-part>
    <main class="homepage-content-editor">
      <h1>Zones libres de la page d’accueil</h1>
      <p>Les zones sont ajoutées après les blocs existants de chaque colonne. L’ordre concerne les zones libres entre elles.</p>
      <ul>
        <c:forEach items="${homepageWidgets}" var="widget">
          <c:url value="/AuroraHomepageContent" var="editUrl">
            <c:param name="action" value="editor"/>
            <c:param name="kind" value="${param.kind}"/>
            <c:param name="owner" value="${param.owner}"/>
            <c:param name="widget" value="${widget.id}"/>
            <c:param name="language" value="${homepageLanguage}"/>
          </c:url>
          <li><a href="<c:out value='${editUrl}'/>"><c:out value="${empty widget.title ? widget.id : widget.title}"/></a>
            — <c:out value="${widget.zone}"/> — ordre <c:out value="${widget.order}"/>
            — <c:out value="${widget.enabled ? 'activée' : 'désactivée'}"/>
          </li>
        </c:forEach>
      </ul>
      <c:url value="/AuroraHomepageContent" var="addUrl">
        <c:param name="action" value="editor"/>
        <c:param name="kind" value="${param.kind}"/>
        <c:param name="owner" value="${param.owner}"/>
        <c:param name="widget" value="${newWidgetId}"/>
        <c:param name="language" value="${homepageLanguage}"/>
      </c:url>
      <p><a href="<c:out value='${addUrl}'/>">Ajouter une zone libre</a></p>
      <c:url value="/look/jsp/Main.jsp" var="homeUrl"/>
      <p><a href="<c:out value='${homeUrl}'/>">Retour à la page d’accueil</a></p>
    </main>
  </view:sp-body-part>
</view:sp-page>
