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
window.addEventListener('DOMContentLoaded', async () => {
  const params = new URLSearchParams(window.location.search);
  const endpoint = new URL(window.location.href);
  endpoint.search = '';
  const context = new URLSearchParams();
  ['kind', 'owner', 'widget', 'language'].forEach(key => context.set(key, params.get(key) || ''));
  const url = action => {
    const target = new URL(endpoint);
    target.search = new URLSearchParams(context);
    target.searchParams.set('action', action);
    return target;
  };
  const status = document.getElementById('homepage-status');
  const save = document.getElementById('homepage-save');
  const image = document.getElementById('homepage-image');
  const title = document.getElementById('homepage-title');
  const preview = document.getElementById('homepage-preview');
  document.getElementById('homepage-context').textContent =
      `${context.get('kind')} / ${context.get('owner')} / ${context.get('widget')} / ${context.get('language')}`;
  let editor;
  let revision;
  let csrf;
  let busy = false;
  const request = async (target, options = {}) => {
    const response = await fetch(target, {credentials: 'same-origin', ...options});
    if (!response.ok) {
      throw new Error(response.status === 409
          ? 'Ce contenu a été modifié ailleurs. Rechargez la page avant de réessayer.'
          : `Opération impossible (${response.status}).`);
    }
    return response.json();
  };
  const refreshPreview = () => {
    const target = url('view');
    target.searchParams.set('v', Date.now());
    preview.src = target;
  };
  try {
    const content = await request(url('load'));
    revision = content.revision;
    csrf = content.csrf;
    title.value = content.title;
    document.getElementById('homepage-html').value = content.html;
    editor = CKEDITOR.replace('homepage-html', {
      customConfig: '',
      language: 'fr',
      height: 300,
      removePlugins: 'imagebank,filebank,uploadimage,uploadfile,uploadwidget,uploadfiles,allmedias',
      toolbar: [
        ['Bold', 'Italic', 'Underline', 'RemoveFormat'],
        ['NumberedList', 'BulletedList', 'Link', 'Unlink', 'Table'],
        ['Format', 'Source', 'Undo', 'Redo']
      ]
    });
    editor.on('instanceReady', () => { save.disabled = false; });
    refreshPreview();
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
      const data = new URLSearchParams({html: editor.getData(), title: title.value, revision});
      const content = await request(url('save'), {
        method: 'POST', headers: {'X-Aurora-CSRF': csrf}, body: data
      });
      revision = content.revision;
      editor.setData(content.html);
      status.textContent = 'Contenu enregistré.';
      refreshPreview();
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
      status.textContent = 'Image insérée. Enregistrez le contenu pour conserver son emplacement.';
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
