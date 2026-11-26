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
    <title>Modifier une zone libre</title>
    <view:link href="/look/jsp/css/homepage-content.css?version=15592-editor-v3"/>
    <view:includePlugin name="wysiwyg"/>
    <view:script src="/look/jsp/js/homepage-content.js?version=15592-image-toolbar-v2"/>
  </view:sp-head-part>
  <view:sp-body-part cssClass="homepage-content-page">
    <view:browseBar>
      <view:browseBarElt label="Page d’accueil" link="${homeUrl}"/>
      <view:browseBarElt label="Zones libres" link="${manageUrl}"/>
      <view:browseBarElt label="Édition"/>
    </view:browseBar>
    <view:window>
      <view:frame>
        <main class="homepage-content-editor" data-language="<c:out value='${homepageLanguage}'/>">
          <h1>Modifier une zone libre</h1>
          <p class="homepage-introduction">Rédigez votre contenu et choisissez son emplacement sur la page d’accueil.</p>
          <form id="homepage-content-form">
            <view:board>
              <fieldset class="homepage-settings">
                <legend>Présentation et affichage</legend>
                <div class="homepage-field homepage-title-field">
                  <label for="homepage-title">Titre de la zone</label>
                  <input id="homepage-title" maxlength="200" type="text" aria-describedby="homepage-title-help">
                  <small id="homepage-title-help">Facultatif. Il s’affiche au-dessus du contenu.</small>
                </div>
                <div class="homepage-placement-fields">
                  <div class="homepage-field">
                    <label for="homepage-zone">Colonne</label>
                    <select id="homepage-zone"><option value="main">Principale</option><option value="right">Droite</option></select>
                  </div>
                  <div class="homepage-field">
                    <label for="homepage-order">Ordre d’affichage</label>
                    <input id="homepage-order" type="number" min="0" max="10000" value="10" required aria-describedby="homepage-order-help">
                    <small id="homepage-order-help">Les plus petits numéros s’affichent en premier.</small>
                  </div>
                  <div class="homepage-field homepage-enabled-field">
                    <label for="homepage-enabled"><input id="homepage-enabled" type="checkbox" checked>Afficher cette zone</label>
                    <small>Décochez pour masquer la zone sans perdre son contenu.</small>
                  </div>
                </div>
              </fieldset>
            </view:board>
            <section class="homepage-writing" aria-labelledby="homepage-content-heading">
              <h2 id="homepage-content-heading"><label for="homepage-html">Contenu de la zone</label></h2>
              <textarea id="homepage-html" rows="16"></textarea>
              <input id="homepage-image" type="file" accept="image/png,image/jpeg,image/gif,image/webp" hidden>
            </section>
            <div class="homepage-actions">
              <button id="homepage-save" class="sp_button" type="submit" disabled>Enregistrer</button>
              <a id="homepage-manage" class="simple-button" href="<c:out value='${manageUrl}'/>">Retour</a>
            </div>
          </form>
          <p id="homepage-status" class="homepage-feedback" role="status" aria-live="polite"></p>
          <details class="homepage-preview-panel">
            <summary>Aperçu de la dernière version enregistrée</summary>
            <iframe id="homepage-preview" title="Aperçu du contenu enregistré" sandbox="allow-same-origin"></iframe>
          </details>
        </main>
      </view:frame>
    </view:window>
  </view:sp-body-part>
</view:sp-page>
