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
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="silverpeas.tags.viewGenerator" prefix="view" %>
<c:url value="/look/jsp/Main.jsp" var="homeUrl"/>
<c:url value="/AuroraHomepageContent" var="manageUrl">
  <c:param name="action" value="manage"/>
  <c:param name="kind" value="${param.kind}"/>
  <c:param name="owner" value="${param.owner}"/>
  <c:param name="language" value="${homepageLanguage}"/>
</c:url>
<view:sp-page>
  <view:sp-head-part>
    <title>Configurer les dernières publications</title>
    <view:link href="/look/jsp/css/homepage-content.css"/>
    <view:script src="/look/jsp/js/homepage-news.js?version=15592-search-v1"/>
  </view:sp-head-part>
  <view:sp-body-part cssClass="homepage-content-page">
    <view:window><view:frame>
      <main class="homepage-content-editor">
        <h1>Configurer les dernières publications</h1>
        <p id="news-context" class="homepage-introduction"></p>
        <form id="news-form">
          <view:board>
            <fieldset class="homepage-settings">
              <legend>Contenu et affichage</legend>
              <div class="homepage-field">
                <label for="news-title">Titre</label>
                <input id="news-title" type="text" maxlength="200">
              </div>
              <div class="homepage-placement-fields">
                <div class="homepage-field">
                  <label for="news-zone">Colonne</label>
                  <select id="news-zone"><option value="main">Principale</option><option value="right">Droite</option></select>
                </div>
                <div class="homepage-field">
                  <label for="news-order">Ordre</label>
                  <input id="news-order" type="number" min="0" max="10000" required>
                </div>
              </div>
              <div class="homepage-field">
                <label for="news-space">Espace source</label>
                <input id="news-space" maxlength="30" pattern="(WA)?[0-9]+">
                <small>Laissez vide pour tous les espaces accessibles. Sinon, indiquez l’identifiant de l’espace, par exemple WA12. Les droits du lecteur et les exclusions du thème restent appliqués.</small>
              </div>
              <div class="homepage-field">
                <label for="news-size">Nombre maximal de publications</label>
                <input id="news-size" type="number" min="1" max="100" required>
              </div>
              <label><input id="news-show-more" type="checkbox"> Afficher le lien vers toutes les dernières publications</label>
              <label><input id="news-enabled" type="checkbox"> Afficher ce bloc</label>
            </fieldset>
          </view:board>
          <div class="homepage-actions">
            <button class="sp_button" id="news-save" type="submit" disabled>Enregistrer</button>
            <a class="sp_button" href="<c:out value='${manageUrl}'/>">Retour à la gestion</a>
          </div>
          <p id="news-status" role="status" aria-live="polite"></p>
        </form>
      </main>
    </view:frame></view:window>
  </view:sp-body-part>
</view:sp-page>
