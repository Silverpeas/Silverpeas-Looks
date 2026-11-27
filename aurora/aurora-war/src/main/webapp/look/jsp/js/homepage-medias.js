/*
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
 */
window.addEventListener('DOMContentLoaded', async () => {
  const control = id => document.getElementById(id);
  const status = control('news-status');
  const save = control('news-save');
  const sourceType = control('media-source-type');
  const endpoint = action => {
    const url = new URL(window.location.href);
    url.searchParams.set('action', action);
    return url;
  };
  const request = (action, body, csrf, parameters) => new Promise((resolve, reject) => {
    const xhr = new XMLHttpRequest();
    const url = endpoint(action);
    Object.entries(parameters || {}).forEach(([key, value]) => url.searchParams.set(key, value));
    xhr.open(body ? 'POST' : 'GET', url.pathname + url.search);
    if (body) {
      xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded;charset=UTF-8');
      xhr.setRequestHeader('X-Aurora-CSRF', csrf);
    }
    xhr.onload = () => {
      if (xhr.status !== 200) {
        reject(new Error(xhr.status === 409 ? 'Ce bloc a été modifié ailleurs. Rechargez la page.'
          : `Opération impossible (${xhr.status}).`));
        return;
      }
      try { resolve(JSON.parse(xhr.responseText)); }
      catch (error) { reject(new Error('Réponse invalide. Vérifiez votre session.')); }
    };
    xhr.onerror = () => reject(new Error('Impossible de joindre le serveur.'));
    xhr.ontimeout = () => reject(new Error('Le serveur ne répond pas.'));
    xhr.timeout = 60000;
    xhr.send(body ? body.toString() : null);
  });
  const refreshSource = () => {
    ['spaces', 'gallery'].forEach(type => {
      const active = sourceType.value === type;
      control('media-' + type + '-field').hidden = !active;
      control('media-' + type + '-search').disabled = !active;
    });
  };
  sourceType.addEventListener('change', refreshSource);
  let revision;
  let csrf;
  control('news-context').textContent = 'Choisissez le contenu et l’emplacement de ce widget.';
  try {
    const data = await request('loadMedias');
    revision = data.revision;
    csrf = data.csrf;
    ['title', 'zone', 'order', 'size'].forEach(key => { control('news-' + key).value = data[key]; });
    control('news-enabled').checked = data.enabled;
    sourceType.value = data.sourceType;
    control('media-' + data.sourceType).value = data.source;
    await window.AuroraHomepageSources.initialize(request);
    refreshSource();
    save.disabled = false;
  } catch (error) { status.textContent = error.message; }
  control('news-form').addEventListener('submit', async event => {
    event.preventDefault();
    if (save.disabled) { return; }
    const source = control('media-' + sourceType.value).value;
    if (!source) { status.textContent = 'Sélectionnez un espace ou une galerie.'; return; }
    save.disabled = true;
    const body = new URLSearchParams({revision, sourceType: sourceType.value, source,
      enabled: control('news-enabled').checked});
    ['title', 'zone', 'order', 'size'].forEach(key => body.set(key, control('news-' + key).value));
    try {
      await request('saveMedias', body, csrf);
      window.location.href = endpoint('manage').toString();
    } catch (error) { status.textContent = error.message; }
    finally { save.disabled = false; }
  });
});
