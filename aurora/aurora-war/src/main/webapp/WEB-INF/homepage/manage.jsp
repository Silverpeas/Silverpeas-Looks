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
    <view:script src="/look/jsp/js/homepage-management.js?version=15592-catalog-v1"/>
    <title>Modifier la page d’accueil</title>
    <view:link href="/look/jsp/css/homepage-content.css?version=15592-catalog-v1"/>
  </view:sp-head-part>
  <view:sp-body-part cssClass="homepage-content-page">
    <view:browseBar>
      <view:browseBarElt label="Page d’accueil" link="${homeUrl}"/>
      <view:browseBarElt label="Widgets"/>
    </view:browseBar>
    <view:window>
      <view:frame>
        <main class="homepage-content-editor homepage-content-management" data-csrf="<c:out value='${homepageCsrf}'/>" data-endpoint="<c:url value='/AuroraHomepageContent'/>">
          <p role="status" class="homepage-delete-status"></p>
          <dialog class="homepage-delete-dialog" aria-labelledby="homepage-delete-title">
            <h2 id="homepage-delete-title">Supprimer ce widget ?</h2>
            <p>Le widget sera retiré de la page d’accueil dans toutes les langues.</p>
            <div class="homepage-delete-dialog-actions">
              <button type="button" data-delete-cancel>Annuler</button>
              <button type="button" data-delete-confirm>Supprimer</button>
            </div>
          </dialog>
          <h1>Modifier la page d’accueil</h1>
          <p class="homepage-introduction">Ajoutez des widgets et choisissez leur colonne, leur ordre et leur visibilité.</p>
          <p class="homepage-preview-link"><a href="<c:out value='${homeUrl}'/>">Voir la page d’accueil →</a></p>
          <div class="homepage-management-layout">
          <section class="homepage-installed" aria-labelledby="homepage-installed-title">
          <h2 id="homepage-installed-title">Widgets installés</h2>
          <p class="homepage-section-description">Organisez les widgets affichés sur votre page d’accueil.</p>
          <c:choose>
            <c:when test="${empty homepageWidgets}">
              <view:board>
                <div class="homepage-empty-state">
                  <h2>Aucun widget pour le moment</h2>
                  <p>Choisissez un widget dans le catalogue pour commencer.</p>
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
                <view:arrayColumn title="Actions" sortable="false"/>
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
                  <c:set var="deleteButton"><button type="button" class="homepage-delete-widget" aria-label="Supprimer le widget" title="Supprimer le widget" data-widget="<c:out value='${widget.id}'/>" data-revision="<c:out value='${widget.revision}'/>" data-kind="<c:out value='${param.kind}'/>" data-owner="<c:out value='${param.owner}'/>" data-language="<c:out value='${homepageLanguage}'/>"><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false"><path d="M3 6h18M9 6V4h6v2M5 6l1 14h12l1-14M10 10v6M14 10v6"/></svg></button></c:set>
                  <view:arrayLine id="${widget.id}">
                    <view:arrayCellText text="${editLink}"/>
                    <view:arrayCellText text="${widget.type == 'weather' ? 'Météo' : widget.type == 'bookmarks' ? 'Favoris personnels' : widget.type == 'search' ? 'Recherche' : widget.type == 'news' ? 'Actualités' : (widget.type == 'publications' ? 'Dernières publications' : (widget.type == 'ephemeris' ? 'Éphémérides' : (widget.type == 'events' ? 'Prochains événements' : (widget.type == 'rss' ? 'Flux RSS' : (widget.type == 'faq' ? 'FAQ' : (widget.type == 'users' ? 'Nouveaux utilisateurs' : (widget.type == 'subscriptions' ? 'Actualités des abonnements' : (widget.type == 'shortcuts' ? 'Raccourcis (home)' : 'Zone libre'))))))))}"/>
                    <view:arrayCellText text="${widget.zone == 'right' ? 'Droite' : 'Principale'}"/>
                    <view:arrayCellText text="${widget.order}"/>
                    <view:arrayCellText text="${displayStatus}"/>
                    <view:arrayCellText text="${deleteButton}"/>
                  </view:arrayLine>
                </c:forEach>
              </view:arrayPane>
            </c:otherwise>
          </c:choose>
          <p class="homepage-help">Les plus petits numéros s’affichent en premier dans chaque colonne.</p>
          </section>
          <aside class="homepage-catalog" aria-labelledby="homepage-catalog-title">
            <h2 id="homepage-catalog-title">Ajouter un widget</h2>
            <p class="homepage-section-description">Choisissez un widget pour le configurer et l’ajouter à la page.</p>
            <div class="homepage-catalog-filters" role="group" aria-label="Catégories de widgets">
              <button type="button" data-catalog-filter="all" aria-pressed="true">Tous</button>
              <button type="button" data-catalog-filter="content" aria-pressed="false">Contenus</button>
              <button type="button" data-catalog-filter="community" aria-pressed="false">Vie collective</button>
              <button type="button" data-catalog-filter="services" aria-pressed="false">Pratique</button>
            </div>
            <div class="homepage-catalog-grid">
              <a class="homepage-catalog-tile" data-category="content" href="<c:out value='${addUrl}'/>">
                <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false"><path d="M4 4h16v16H4z M8 8h8 M8 12h8 M8 16h4"/></svg>
                <span class="homepage-catalog-name">Zone libre</span>
                <span class="homepage-catalog-description">Composez un contenu avec du texte, des images et des liens.</span>
                <span class="homepage-catalog-add" aria-hidden="true">+</span>
              </a>
              <a class="homepage-catalog-tile" data-category="content" href="<c:out value='${addNewsUrl}'/>">
                <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false"><path d="M4 4h16v16H4z M8 8h3v4H8z M14 8h3 M14 12h3 M8 16h9"/></svg>
                <span class="homepage-catalog-name">Actualités</span>
                <span class="homepage-catalog-description">Mettez en avant les nouvelles de votre organisation.</span>
                <span class="homepage-catalog-add" aria-hidden="true">+</span>
              </a>
              <a class="homepage-catalog-tile" data-category="content" href="<c:out value='${addPublicationsUrl}'/>">
                <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false"><path d="M6 3h8l4 4v14H6z M14 3v5h4 M9 12h6 M9 16h6"/></svg>
                <span class="homepage-catalog-name">Dernières publications</span>
                <span class="homepage-catalog-description">Retrouvez les documents récemment publiés.</span>
                <span class="homepage-catalog-add" aria-hidden="true">+</span>
              </a>
              <a class="homepage-catalog-tile" data-category="content" href="<c:out value='${addRssUrl}'/>">
                <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false"><path d="M5 5a14 14 0 0 1 14 14 M5 11a8 8 0 0 1 8 8 M5 18h1v1H5z"/></svg>
                <span class="homepage-catalog-name">Flux RSS</span>
                <span class="homepage-catalog-description">Suivez les nouveautés de vos sources externes.</span>
                <span class="homepage-catalog-add" aria-hidden="true">+</span>
              </a>
              <a class="homepage-catalog-tile" data-category="content" href="<c:out value='${addSubscriptionsUrl}'/>">
                <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false"><path d="M6 16V9a6 6 0 0 1 12 0v7l2 2H4z M10 21h4"/></svg>
                <span class="homepage-catalog-name">Actualités des abonnements</span>
                <span class="homepage-catalog-description">Regroupez les nouveautés suivies par chaque utilisateur.</span>
                <span class="homepage-catalog-add" aria-hidden="true">+</span>
              </a>
              <a class="homepage-catalog-tile" data-category="community" href="<c:out value='${addEventsUrl}'/>">
                <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false"><path d="M4 5h16v16H4z M8 3v4 M16 3v4 M4 10h16 M8 14h3 M8 17h6"/></svg>
                <span class="homepage-catalog-name">Prochains événements</span>
                <span class="homepage-catalog-description">Affichez les rendez-vous à venir.</span>
                <span class="homepage-catalog-add" aria-hidden="true">+</span>
              </a>
              <a class="homepage-catalog-tile" data-category="community" href="<c:out value='${addUsersUrl}'/>">
                <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false"><path d="M8 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8 M2 21v-2a6 6 0 0 1 12 0v2 M19 8v6 M16 11h6"/></svg>
                <span class="homepage-catalog-name">Nouveaux utilisateurs</span>
                <span class="homepage-catalog-description">Présentez les nouveaux membres de votre communauté.</span>
                <span class="homepage-catalog-add" aria-hidden="true">+</span>
              </a>
              <a class="homepage-catalog-tile" data-category="community" href="<c:out value='${addFaqUrl}'/>">
                <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false"><path d="M3 4h18v14H9l-6 4z M9 9a3 3 0 1 1 5 2l-2 1v1 M12 15v1"/></svg>
                <span class="homepage-catalog-name">FAQ</span>
                <span class="homepage-catalog-description">Donnez accès aux questions et réponses utiles.</span>
                <span class="homepage-catalog-add" aria-hidden="true">+</span>
              </a>
              <a class="homepage-catalog-tile" data-category="services" href="<c:out value='${addShortcutsUrl}'/>">
                <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false"><path d="M9 15l6-6 M8 17l-1 1a4 4 0 0 1-6-6l4-4a4 4 0 0 1 6 0 M16 7l1-1a4 4 0 0 1 6 6l-4 4a4 4 0 0 1-6 0"/></svg>
                <span class="homepage-catalog-name">Raccourcis</span>
                <span class="homepage-catalog-description">Proposez des liens utiles sur la page d’accueil.</span>
                <span class="homepage-catalog-add" aria-hidden="true">+</span>
              </a>
              <a class="homepage-catalog-tile" data-category="services" href="<c:out value='${addSearchUrl}'/>">
                <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false"><path d="M10 3a7 7 0 1 0 0 14 7 7 0 0 0 0-14 M15 15l6 6"/></svg>
                <span class="homepage-catalog-name">Recherche</span>
                <span class="homepage-catalog-description">Recherchez par plan de classement ou avec un formulaire avancé.</span>
                <span class="homepage-catalog-add" aria-hidden="true">+</span>
              </a>
              <a class="homepage-catalog-tile" data-category="services" href="<c:out value='${addBookmarksUrl}'/>">
                <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false"><path d="M6 3h12v18l-6-4-6 4z"/></svg>
                <span class="homepage-catalog-name">Favoris personnels</span>
                <span class="homepage-catalog-description">Retrouvez les favoris propres à chaque utilisateur.</span>
                <span class="homepage-catalog-add" aria-hidden="true">+</span>
              </a>
              <a class="homepage-catalog-tile" data-category="services" href="<c:out value='${addEphemerisUrl}'/>">
                <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false"><path d="M12 3v3 M12 18v3 M3 12h3 M18 12h3 M5 5l2 2 M17 17l2 2 M5 19l2-2 M17 7l2-2 M12 8a4 4 0 1 0 0 8 4 4 0 0 0 0-8"/></svg>
                <span class="homepage-catalog-name">Éphémérides</span>
                <span class="homepage-catalog-description">Affichez les informations et les fêtes du jour.</span>
                <span class="homepage-catalog-add" aria-hidden="true">+</span>
              </a>
              <a class="homepage-catalog-tile" data-category="services" href="<c:out value='${addWeatherUrl}'/>">
                <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false"><path d="M8 16a5 5 0 1 1 9-4h1a4 4 0 0 1 0 8H7a4 4 0 1 1 1-8 M5 3v2 M1 7l2 1 M10 3l-1 2"/></svg>
                <span class="homepage-catalog-name">Météo</span>
                <span class="homepage-catalog-description">Consultez les prévisions des villes configurées.</span>
                <span class="homepage-catalog-add" aria-hidden="true">+</span>
              </a>
            </div>
          </aside>
          </div>
        </main>
      </view:frame>
    </view:window>
  </view:sp-body-part>
</view:sp-page>
