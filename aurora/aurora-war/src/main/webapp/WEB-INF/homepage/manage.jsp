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
<c:url value="/look/jsp/Main.jsp" var="homeUrl"/>
<c:url value="/AuroraHomepageContent" var="manageUrl">
  <c:param name="action" value="manage"/>
  <c:param name="kind" value="${param.kind}"/>
  <c:param name="owner" value="${param.owner}"/>
  <c:param name="language" value="${homepageLanguage}"/>
</c:url>
<c:url value="/AuroraHomepageContent" var="addUrl">
  <c:param name="action" value="editor"/>
  <c:param name="kind" value="${param.kind}"/>
  <c:param name="owner" value="${param.owner}"/>
  <c:param name="widget" value="${newWidgetId}"/>
  <c:param name="language" value="${homepageLanguage}"/>
</c:url>
<c:url value="/AuroraHomepageContent" var="addNewsUrl">
  <c:param name="action" value="newsEditor"/>
  <c:param name="kind" value="${param.kind}"/>
  <c:param name="owner" value="${param.owner}"/>
  <c:param name="widget" value="${newNewsWidgetId}"/>
  <c:param name="language" value="${homepageLanguage}"/>
</c:url>
<c:url value="/AuroraHomepageContent" var="addPublicationsUrl">
  <c:param name="action" value="publicationsEditor"/>
  <c:param name="kind" value="${param.kind}"/>
  <c:param name="owner" value="${param.owner}"/>
  <c:param name="widget" value="${newPublicationsWidgetId}"/>
  <c:param name="language" value="${homepageLanguage}"/>
</c:url>
<c:url value="/AuroraHomepageContent" var="addEphemerisUrl">
  <c:param name="action" value="ephemerisEditor"/>
  <c:param name="kind" value="${param.kind}"/>
  <c:param name="owner" value="${param.owner}"/>
  <c:param name="widget" value="${newEphemerisWidgetId}"/>
  <c:param name="language" value="${homepageLanguage}"/>
</c:url>
<c:url value="/AuroraHomepageContent" var="addEventsUrl">
  <c:param name="action" value="eventsEditor"/>
  <c:param name="kind" value="${param.kind}"/>
  <c:param name="owner" value="${param.owner}"/>
  <c:param name="widget" value="${newEventsWidgetId}"/>
  <c:param name="language" value="${homepageLanguage}"/>
</c:url>
<c:url value="/AuroraHomepageContent" var="addRssUrl">
  <c:param name="action" value="rssEditor"/>
  <c:param name="kind" value="${param.kind}"/>
  <c:param name="owner" value="${param.owner}"/>
  <c:param name="widget" value="${newRssWidgetId}"/>
  <c:param name="language" value="${homepageLanguage}"/>
</c:url>
<c:url value="/AuroraHomepageContent" var="addFaqUrl">
  <c:param name="action" value="faqEditor"/>
  <c:param name="kind" value="${param.kind}"/>
  <c:param name="owner" value="${param.owner}"/>
  <c:param name="widget" value="${newFaqWidgetId}"/>
  <c:param name="language" value="${homepageLanguage}"/>
</c:url>
<c:url value="/AuroraHomepageContent" var="addUsersUrl">
  <c:param name="action" value="usersEditor"/>
  <c:param name="kind" value="${param.kind}"/>
  <c:param name="owner" value="${param.owner}"/>
  <c:param name="widget" value="${newUsersWidgetId}"/>
  <c:param name="language" value="${homepageLanguage}"/>
</c:url>
<c:url value="/AuroraHomepageContent" var="addWeatherUrl">
  <c:param name="action" value="weatherEditor"/>
  <c:param name="kind" value="${param.kind}"/>
  <c:param name="owner" value="${param.owner}"/>
  <c:param name="widget" value="${newWeatherWidgetId}"/>
  <c:param name="language" value="${homepageLanguage}"/>
</c:url>
<c:url value="/AuroraHomepageContent" var="addBookmarksUrl">
  <c:param name="action" value="bookmarksEditor"/>
  <c:param name="kind" value="${param.kind}"/>
  <c:param name="owner" value="${param.owner}"/>
  <c:param name="widget" value="${newBookmarksWidgetId}"/>
  <c:param name="language" value="${homepageLanguage}"/>
</c:url>
<c:url value="/AuroraHomepageContent" var="addSearchUrl">
  <c:param name="action" value="searchEditor"/>
  <c:param name="kind" value="${param.kind}"/>
  <c:param name="owner" value="${param.owner}"/>
  <c:param name="widget" value="${newSearchWidgetId}"/>
  <c:param name="language" value="${homepageLanguage}"/>
</c:url>
<c:url value="/AuroraHomepageContent" var="addSubscriptionsUrl">
  <c:param name="action" value="subscriptionsEditor"/>
  <c:param name="kind" value="${param.kind}"/>
  <c:param name="owner" value="${param.owner}"/>
  <c:param name="widget" value="${newSubscriptionsWidgetId}"/>
  <c:param name="language" value="${homepageLanguage}"/>
</c:url>
<c:url value="/AuroraHomepageContent" var="addShortcutsUrl">
  <c:param name="action" value="shortcutsEditor"/>
  <c:param name="kind" value="${param.kind}"/>
  <c:param name="owner" value="${param.owner}"/>
  <c:param name="widget" value="${newShortcutsWidgetId}"/>
  <c:param name="language" value="${homepageLanguage}"/>
</c:url>
<view:sp-page>
  <view:sp-head-part>
    <title>Modifier la page d’accueil</title>
    <view:link href="/look/jsp/css/homepage-content.css?version=15592-ui-v1"/>
  </view:sp-head-part>
  <view:sp-body-part cssClass="homepage-content-page">
    <view:browseBar>
      <view:browseBarElt label="Page d’accueil" link="${homeUrl}"/>
      <view:browseBarElt label="Widgets"/>
    </view:browseBar>
    <view:window>
      <view:frame>
        <main class="homepage-content-editor homepage-content-management">
          <h1>Modifier la page d’accueil</h1>
          <p class="homepage-introduction">Ajoutez des widgets et choisissez leur colonne, leur ordre et leur visibilité.</p>
          <view:buttonPane>
            <view:button label="Ajouter la météo" action="${addWeatherUrl}"/>
            <view:button label="Ajouter les favoris personnels" action="${addBookmarksUrl}"/>
            <view:button label="Ajouter une recherche" action="${addSearchUrl}"/>
            <view:button label="Ajouter des actualités" action="${addNewsUrl}"/>
            <view:button label="Ajouter les dernières publications" action="${addPublicationsUrl}"/>
            <view:button label="Ajouter les éphémérides" action="${addEphemerisUrl}"/>
            <view:button label="Ajouter les prochains événements" action="${addEventsUrl}"/>
            <view:button label="Ajouter les flux RSS" action="${addRssUrl}"/>
            <view:button label="Ajouter une FAQ" action="${addFaqUrl}"/>
            <view:button label="Ajouter les nouveaux utilisateurs" action="${addUsersUrl}"/>
            <view:button label="Ajouter les actualités des abonnements" action="${addSubscriptionsUrl}"/>
            <view:button label="Ajouter des raccourcis" action="${addShortcutsUrl}"/>
            <view:button label="Ajouter une zone libre" action="${addUrl}"/>
            <view:button label="Voir la page d’accueil" action="${homeUrl}" classes="homepage-secondary-action"/>
          </view:buttonPane>
          <c:choose>
            <c:when test="${empty homepageWidgets}">
              <view:board>
                <div class="homepage-empty-state">
                  <h2>Aucun widget pour le moment</h2>
                  <p>Utilisez « Ajouter une zone libre » pour créer votre premier contenu.</p>
                </div>
              </view:board>
            </c:when>
            <c:otherwise>
              <view:arrayPane var="homepageContentZones" routingAddress="${manageUrl}" numberLinesPerPage="20">
                <view:arrayColumn title="Widget" sortable="false"/>
                <view:arrayColumn title="Type" sortable="false"/>
                <view:arrayColumn title="Colonne" sortable="false"/>
                <view:arrayColumn title="Ordre" sortable="false"/>
                <view:arrayColumn title="Affichage" sortable="false"/>
                <c:forEach items="${homepageWidgets}" var="widget">
                  <c:url value="/AuroraHomepageContent" var="editUrl">
                    <c:param name="action" value="${widget.type == 'weather' ? 'weatherEditor' : widget.type == 'bookmarks' ? 'bookmarksEditor' : widget.type == 'search' ? 'searchEditor' : widget.type == 'news' ? 'newsEditor' : (widget.type == 'publications' ? 'publicationsEditor' : (widget.type == 'ephemeris' ? 'ephemerisEditor' : (widget.type == 'events' ? 'eventsEditor' : (widget.type == 'rss' ? 'rssEditor' : (widget.type == 'faq' ? 'faqEditor' : (widget.type == 'users' ? 'usersEditor' : (widget.type == 'subscriptions' ? 'subscriptionsEditor' : (widget.type == 'shortcuts' ? 'shortcutsEditor' : 'editor'))))))))}"/>
                    <c:param name="kind" value="${param.kind}"/>
                    <c:param name="owner" value="${param.owner}"/>
                    <c:param name="widget" value="${widget.id}"/>
                    <c:param name="language" value="${homepageLanguage}"/>
                  </c:url>
                  <c:set var="editLink"><a href="<c:out value='${editUrl}'/>"><c:out value="${empty widget.title ? 'Zone sans titre' : widget.title}"/></a></c:set>
                  <c:set var="displayStatus"><span class="homepage-status-badge ${widget.enabled ? 'homepage-status-visible' : 'homepage-status-hidden'}"><c:out value="${widget.enabled ? 'Visible' : 'Masquée'}"/></span></c:set>
                  <view:arrayLine id="${widget.id}">
                    <view:arrayCellText text="${editLink}"/>
                    <view:arrayCellText text="${widget.type == 'weather' ? 'Météo' : widget.type == 'bookmarks' ? 'Favoris personnels' : widget.type == 'search' ? 'Recherche' : widget.type == 'news' ? 'Actualités' : (widget.type == 'publications' ? 'Dernières publications' : (widget.type == 'ephemeris' ? 'Éphémérides' : (widget.type == 'events' ? 'Prochains événements' : (widget.type == 'rss' ? 'Flux RSS' : (widget.type == 'faq' ? 'FAQ' : (widget.type == 'users' ? 'Nouveaux utilisateurs' : (widget.type == 'subscriptions' ? 'Actualités des abonnements' : (widget.type == 'shortcuts' ? 'Raccourcis (home)' : 'Zone libre'))))))))}"/>
                    <view:arrayCellText text="${widget.zone == 'right' ? 'Droite' : 'Principale'}"/>
                    <view:arrayCellText text="${widget.order}"/>
                    <view:arrayCellText text="${displayStatus}"/>
                  </view:arrayLine>
                </c:forEach>
              </view:arrayPane>
            </c:otherwise>
          </c:choose>
          <p class="homepage-help">Les widgets partagent le même ordre dans chaque colonne, après les autres blocs existants. Les plus petits numéros s’affichent en premier. Les blocs historiques peuvent être modifiés dans cette liste.</p>
        </main>
      </view:frame>
    </view:window>
  </view:sp-body-part>
</view:sp-page>
