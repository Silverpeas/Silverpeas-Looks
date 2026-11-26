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
/* Prototype #15592: image uploads and persistence belong to Aurora, not WebPages. */
// Silverpeas stamps XMLHttpRequest with its session CSRF token; fetch is not stamped.
function homepageContentRequest(target, options = {}) {
  return new Promise((resolve, reject) => {
    const xhr = new XMLHttpRequest();
    // A context-relative URL lets the platform recognize this as a Silverpeas request.
    xhr.open(options.method || 'GET', target.pathname + target.search, true);
    Object.entries(options.headers || {}).forEach(([name, value]) => xhr.setRequestHeader(name, value));
    let body = options.body;
    if (body instanceof URLSearchParams) {
      xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded;charset=UTF-8');
      body = body.toString();
    }
    xhr.onload = () => {
      if (xhr.status < 200 || xhr.status >= 300) {
        reject(new Error(xhr.status === 409
            ? 'Ce contenu a été modifié ailleurs. Rechargez la page avant de réessayer.'
            : `Opération impossible (${xhr.status}).`));
        return;
      }
      try {
        resolve(JSON.parse(xhr.responseText));
      } catch (error) {
        reject(new Error('Réponse du serveur invalide. Vérifiez votre session.'));
      }
    };
    xhr.onerror = () => reject(new Error('Impossible de joindre le serveur.'));
    xhr.ontimeout = () => reject(new Error('Le serveur ne répond pas. Réessayez.'));
    xhr.timeout = 60000;
    xhr.send(body || null);
  });
}

window.addEventListener('DOMContentLoaded', async () => {
  const params = new URLSearchParams(window.location.search);
  const endpoint = new URL(window.location.href);
  endpoint.search = '';
  const context = new URLSearchParams();
  ['kind', 'owner', 'widget', 'language'].forEach(key => context.set(key, params.get(key) || ''));
  if (!context.get('language')) {
    context.set('language', document.querySelector('main').dataset.language);
  }
  const url = action => {
    const target = new URL(endpoint);
    target.search = new URLSearchParams(context);
    target.searchParams.set('action', action);
    return target;
  };
  document.getElementById('homepage-manage').href = url('manage');
  const zone = document.getElementById('homepage-zone');
  const order = document.getElementById('homepage-order');
  const enabled = document.getElementById('homepage-enabled');
  const status = document.getElementById('homepage-status');
  const save = document.getElementById('homepage-save');
  const image = document.getElementById('homepage-image');
  const title = document.getElementById('homepage-title');
  let editor;
  let revision;
  let csrf;
  let busy = false;
  const request = homepageContentRequest;
  try {
    const content = await request(url('load'));
    revision = content.revision;
    csrf = content.csrf;
    title.value = content.title;
    zone.value = content.zone;
    order.value = content.order;
    enabled.checked = content.enabled;
    document.getElementById('homepage-html').value = content.html;
    editor = CKEDITOR.replace('homepage-html', {
      customConfig: '',
      language: 'fr',
      height: 300,
      extraPlugins: 'image,iframe',
      // Uploaded images are stored with this homepage widget.
      extraAllowedContent: 'img[!src,alt,width,height]{width,height};iframe[!src,title,width,height,allowfullscreen,sandbox,loading,referrerpolicy]',
      removePlugins: 'imagebank,filebank,uploadimage,uploadfile,uploadwidget,uploadfiles,allmedias',
      toolbar: [
        ['Bold', 'Italic', 'Underline', 'RemoveFormat'],
        ['NumberedList', 'BulletedList', 'Link', 'Unlink', 'Image', 'Iframe', 'Table'],
        ['Format', 'Source', 'Undo', 'Redo']
      ]
    });
    editor.on('instanceReady', () => {
      editor.addCommand('image', {
        exec: () => { if (!busy) image.click(); }
      });
      save.disabled = false;
    });
  } catch (error) {
    status.textContent = error.message;
    image.disabled = true;
  }
  document.getElementById('homepage-content-form').addEventListener('submit', async event => {
    event.preventDefault();
    if (!editor || busy || save.disabled) return;
    busy = true;
    save.disabled = true;
    image.disabled = true;
    try {
      const data = new URLSearchParams({html: editor.getData(), title: title.value, revision,
        zone: zone.value, order: order.value, enabled: String(enabled.checked)});
      const content = await request(url('save'), {
        method: 'POST', headers: {'X-Aurora-CSRF': csrf}, body: data
      });
      revision = content.revision;
      editor.setData(content.html);
      window.location.href = url('manage');
    } catch (error) {
      status.textContent = error.message;
    } finally {
      busy = false;
      save.disabled = false;
      image.disabled = false;
    }
  });
  image.addEventListener('change', async () => {
    if (!editor || busy || !image.files.length) return;
    busy = true;
    save.disabled = true;
    image.disabled = true;
    try {
      const data = new FormData();
      data.append('image', image.files[0]);
      const result = await request(url('upload'), {
        method: 'POST', headers: {'X-Aurora-CSRF': csrf}, body: data
      });
      const element = new CKEDITOR.dom.element('img');
      element.setAttribute('src', result.url);
      element.setAttribute('alt', image.files[0].name);
      editor.insertElement(element);
      status.textContent = '';
    } catch (error) {
      status.textContent = error.message;
    } finally {
      image.value = '';
      busy = false;
      save.disabled = false;
      image.disabled = false;
    }
  });
});
