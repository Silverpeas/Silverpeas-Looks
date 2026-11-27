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
  const form = document.getElementById('news-form');
  const save = document.getElementById('news-save');
  const status = document.getElementById('news-status');
  const file = document.getElementById('image-file');
  const preview = document.getElementById('image-preview');
  const fields = {title: 'news-title', zone: 'news-zone', order: 'news-order',
    enabled: 'news-enabled', image: 'image-name', alt: 'image-alt', url: 'image-url', target: 'image-target'};
  const control = key => document.getElementById(fields[key]);
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
      if (!(body instanceof FormData)) {
        xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded;charset=UTF-8');
      }
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
    xhr.send(body instanceof URLSearchParams ? body.toString() : body || null);
  });
  const refreshPreview = () => {
    preview.hidden = !control('image').value;
    if (!preview.hidden) {
      const url = endpoint('asset');
      url.searchParams.set('name', control('image').value);
      preview.src = url.toString();
    }
  };
  let revision;
  let csrf;
  let ready = false;
  let busy = false;
  document.getElementById('news-context').textContent = 'Choisissez une image et son emplacement.';
  try {
    const data = await request('loadImage');
    Object.keys(fields).forEach(key => {
      if (control(key).type === 'checkbox') { control(key).checked = data[key]; }
      else { control(key).value = data[key]; }
    });
    revision = data.revision;
    csrf = data.csrf;
    ready = true;
    file.disabled = false;
    refreshPreview();
    save.disabled = !control('image').value;
  } catch (error) { status.textContent = error.message; }
  file.addEventListener('change', async () => {
    if (!ready || busy || !file.files.length) { return; }
    busy = true;
    save.disabled = true;
    file.disabled = true;
    try {
      const image = file.files[0];
      if (image.size > 5242880) { throw new Error('L’image ne doit pas dépasser 5 Mo.'); }
      const body = new FormData();
      body.append('image', image);
      status.textContent = 'Import de l’image…';
      const data = await request('upload', body, csrf);
      const uploaded = new URL(data.url, window.location.href);
      const name = uploaded.searchParams.get('name');
      if (!/^[a-f0-9-]{36}\.png$/.test(name || '')) { throw new Error('Réponse invalide.'); }
      control('image').value = name;
      refreshPreview();
      status.textContent = 'Image importée. Enregistrez pour l’afficher sur la page.';
    } catch (error) { status.textContent = error.message; }
    finally {
      busy = false;
      file.disabled = false;
      file.value = '';
      save.disabled = !control('image').value;
    }
  });
  form.addEventListener('submit', async event => {
    event.preventDefault();
    if (!ready || busy || save.disabled) { return; }
    busy = true;
    save.disabled = true;
    file.disabled = true;
    const body = new URLSearchParams({revision});
    Object.keys(fields).forEach(key => body.set(key,
      control(key).type === 'checkbox' ? control(key).checked : control(key).value));
    try {
      await request('saveImage', body, csrf);
      window.location.href = endpoint('manage').toString();
    } catch (error) {
      status.textContent = error.message;
      busy = false;
      save.disabled = false;
      file.disabled = false;
    }
  });
});
