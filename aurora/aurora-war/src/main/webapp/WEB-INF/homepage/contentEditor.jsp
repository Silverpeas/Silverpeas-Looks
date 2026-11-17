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
<view:sp-page>
  <view:sp-head-part>
    <title>Contenu libre de la page d’accueil</title>
    <view:link href="/look/jsp/css/homepage-content.css"/>
    <view:includePlugin name="wysiwyg"/>
    <view:script src="/look/jsp/js/homepage-content.js?prototype=15592-layout-v1"/>
  </view:sp-head-part>
  <view:sp-body-part>
    <main class="homepage-content-editor" data-language="<c:out value='${homepageLanguage}'/>">
      <h1>Contenu libre de la page d’accueil</h1>
      <p id="homepage-context"></p>
      <form id="homepage-content-form">
        <label for="homepage-title">Titre</label>
        <input id="homepage-title" maxlength="200" type="text">
        <label for="homepage-zone">Colonne</label>
        <select id="homepage-zone"><option value="main">Principale</option><option value="right">Droite</option></select>
        <label for="homepage-order">Ordre parmi les zones libres</label>
        <input id="homepage-order" type="number" min="0" max="10000" value="10" required>
        <label for="homepage-enabled">Zone activée</label>
        <input id="homepage-enabled" type="checkbox" checked>
        <label for="homepage-html">Contenu</label>
        <textarea id="homepage-html" rows="16"></textarea>
        <label for="homepage-image">Insérer une image</label>
        <input id="homepage-image" type="file" accept="image/png,image/jpeg,image/gif,image/webp">
        <button id="homepage-save" type="submit" disabled>Enregistrer</button>
      </form>
      <p><a id="homepage-manage">Retour à la gestion</a></p>
      <p id="homepage-status" role="status" aria-live="polite"></p>
      <h2>Aperçu enregistré</h2>
      <iframe id="homepage-preview" title="Aperçu du contenu enregistré" sandbox="allow-same-origin"></iframe>
    </main>
  </view:sp-body-part>
</view:sp-page>
