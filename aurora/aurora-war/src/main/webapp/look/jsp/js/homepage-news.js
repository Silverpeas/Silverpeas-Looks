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
  const query = new URLSearchParams(window.location.search);
  const index = query.get('index');
  const status = document.getElementById('news-status');
  const save = document.getElementById('news-save');
  const fields = {source: 'source', size: 'size', rendering: 'rendering',
    importantOnly: 'important', enabled: 'enabled'};
  const control = key => document.getElementById('news-' + fields[key]);
  const endpoint = action => {
    const url = new URL(window.location.href);
    url.searchParams.set('action', action);
    return url;
  };
  const request = (action, body, csrf) => new Promise((resolve, reject) => {
    const xhr = new XMLHttpRequest();
    const url = endpoint(action);
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
  const applications = document.getElementById('news-applications');
  const refreshSource = () => {
    const selected = control('source').value === 'applications';
    document.getElementById('news-applications-field').hidden = !selected;
    applications.required = selected;
  };
  control('source').addEventListener('change', refreshSource);
  let revision;
  let csrf;
  document.getElementById('news-context').textContent = 'Bloc d’actualités ' + index;
  try {
    const data = await request('loadNews');
    revision = data.revision;
    csrf = data.csrf;
    Object.keys(fields).forEach(key => {
      const input = control(key);
      if (input.type === 'checkbox') { input.checked = data[key]; }
      else { input.value = data[key]; }
    });
    if (data.source && !['*', 'delegated', 'taxonomy'].includes(data.source)) {
      control('source').value = 'applications';
      applications.value = data.source;
    }
    refreshSource();
    status.textContent = data.configured ? ''
      : 'Ce bloc utilise encore les réglages du thème. Enregistrer appliquera les valeurs ci-dessus.';
    save.disabled = false;
  } catch (error) { status.textContent = error.message; }
  document.getElementById('news-form').addEventListener('submit', async event => {
    event.preventDefault();
    if (save.disabled) { return; }
    save.disabled = true;
    const body = new URLSearchParams({revision});
    Object.keys(fields).forEach(key => {
      const input = control(key);
      body.set(key, input.type === 'checkbox' ? input.checked : input.value);
    });
    if (control('source').value === 'applications') {
      body.set('source', applications.value.trim().replace(/\s+/g, ' '));
    }
    try {
      const data = await request('saveNews', body, csrf);
      revision = data.revision;
      status.textContent = 'Configuration enregistrée. Rechargez la page d’accueil pour la voir.';
    } catch (error) { status.textContent = error.message; }
    finally { save.disabled = false; }
  });
});
