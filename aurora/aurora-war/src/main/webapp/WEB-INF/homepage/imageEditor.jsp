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
    <title>Configurer l’image</title>
    <view:link href="/look/jsp/css/homepage-content.css?version=15592-editor-v3"/>
    <view:link href="/look/jsp/css/homepage-image.css?version=15592-image-v1"/>
    <view:script src="/look/jsp/js/homepage-image.js?version=15592-image-v1"/>
  </view:sp-head-part>
  <view:sp-body-part cssClass="homepage-content-page">
    <view:window><view:frame>
      <main class="homepage-content-editor">
        <h1>Configurer l’image</h1>
        <p id="news-context" class="homepage-introduction"></p>
        <form id="news-form" data-widget-type="image">
          <view:board>
            <fieldset class="homepage-settings">
              <legend>Contenu et affichage</legend>
              <div class="homepage-field">
                <label for="news-title">Nom du widget</label>
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
                <label for="image-file">Image</label>
                <input id="image-file" type="file" disabled accept="image/png,image/jpeg,image/gif,image/webp">
                <p>PNG, JPEG, GIF ou WebP, 5 Mo maximum.</p>
                <input id="image-name" type="hidden">
                <img id="image-preview" class="homepage-image-preview" alt="" hidden>
              </div>
              <div class="homepage-field">
                <label for="image-alt">Texte alternatif</label>
                <input id="image-alt" type="text" maxlength="200" aria-describedby="image-alt-help">
                <p id="image-alt-help">Décrivez l’image pour les personnes qui ne peuvent pas la voir. Laissez vide si elle est décorative.</p>
              </div>
              <div class="homepage-field">
                <label for="image-url">Lien (facultatif)</label>
                <input id="image-url" type="text" maxlength="2048" placeholder="https://…">
              </div>
              <div class="homepage-field">
                <label for="image-target">Ouvrir le lien dans</label>
                <select id="image-target"><option value="_self">La même fenêtre</option><option value="_blank">Un nouvel onglet</option><option value="_top">La fenêtre principale</option><option value="_parent">La fenêtre parente</option></select>
              </div>
              <label><input id="news-enabled" type="checkbox"> Afficher ce bloc</label>
            </fieldset>
          </view:board>
          <div class="homepage-actions">
            <button class="sp_button" id="news-save" type="submit" disabled>Enregistrer</button>
            <a class="simple-button" href="<c:out value='${manageUrl}'/>">Retour</a>
          </div>
          <p id="news-status" role="status" aria-live="polite"></p>
        </form>
      </main>
    </view:frame></view:window>
  </view:sp-body-part>
</view:sp-page>
