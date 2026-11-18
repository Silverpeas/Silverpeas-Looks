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
  const status = document.getElementById('news-status');
  const save = document.getElementById('news-save');
  const widgetType = document.getElementById('news-form').dataset.widgetType;
  const community = ['faq', 'users', 'subscriptions', 'shortcuts', 'search', 'bookmarks'].includes(widgetType);
  const configurationAction = prefix => prefix + widgetType[0].toUpperCase() + widgetType.slice(1);
  const feed = ['events', 'rss'].includes(widgetType);
  const ephemeris = widgetType === 'ephemeris';
  const commonFields = {title: 'title', zone: 'zone', order: 'order', enabled: 'enabled'};
  const publications = document.getElementById('news-space');
  const fields = widgetType === 'bookmarks' ? {...commonFields, showWhenEmpty: 'show-when-empty', emptyFragment: 'empty-fragment'} : widgetType === 'search' ? {...commonFields, mode: 'mode', template: 'template', labelsInside: 'labels-inside', period: 'period', space: 'space'} : widgetType === 'shortcuts' ? commonFields : widgetType === 'subscriptions' ? {...commonFields, size: 'size', showMore: 'show-more'} : community ? {...commonFields, size: 'size', ...(widgetType === 'faq'
    ? {source: 'source', selection: 'selection'} : {group: 'group', avatar: 'avatar', fields: 'fields'})} : feed ? {...commonFields, source: 'source', ...(widgetType === 'events'
    ? {maxDays: 'max-days', includeToday: 'include-today', personal: 'personal', importantOnly: 'important', showMore: 'show-more'}
    : {rendering: 'rendering'})} : ephemeris ? {title: 'title', zone: 'zone', order: 'order', enabled: 'enabled'} : publications ? {title: 'title', zone: 'zone', order: 'order', enabled: 'enabled',
    spaceId: 'space', size: 'size', showMore: 'show-more'} : {source: 'source', size: 'size', rendering: 'rendering',
    showMoreNews: 'show-more', showAllNews: 'show-all', importantOnly: 'important', enabled: 'enabled', title: 'title', zone: 'zone', order: 'order'};
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
  const refreshSearch = () => {
    if (widgetType !== 'search') { return; }
    const advanced = control('mode').value === 'advanced';
    document.getElementById('search-advanced-settings').hidden = !advanced;
    document.getElementById('search-taxonomy-settings').hidden = advanced;
    control('template').required = advanced;
  };
  if (widgetType === 'search') { control('mode').addEventListener('change', refreshSearch); }
  const refreshSource = () => {
    if (community || feed || ephemeris || publications) { return; }
    const selected = control('source').value === 'applications';
    document.getElementById('news-applications-field').hidden = !selected;
    applications.required = selected;
  };
  if (!community && !feed && !ephemeris && !publications) { control('source').addEventListener('change', refreshSource); }
  let revision;
  let csrf;
  document.getElementById('news-context').textContent = 'Choisissez le contenu et l’emplacement de ce widget.';
  try {
    const data = await request(community ? configurationAction('load') : feed ? (widgetType === 'events' ? 'loadEvents' : 'loadRss') : ephemeris ? 'loadEphemeris' : publications ? 'loadPublications' : 'loadNews');
    if (widgetType === 'shortcuts') { window.AuroraHomeShortcuts.load(data.entries); }
    revision = data.revision;
    csrf = data.csrf;
    Object.keys(fields).forEach(key => {
      const input = control(key);
      if (input.type === 'checkbox') { input.checked = data[key]; }
      else { input.value = data[key]; }
    });
    if (!community && !feed && !ephemeris && !publications && data.source && !['*', 'delegated', 'taxonomy'].includes(data.source)) {
      control('source').value = 'applications';
      applications.value = data.source;
    }
    refreshSource();
    refreshSearch();
    status.textContent = data.configured ? ''
      : 'Les modifications seront appliquées après enregistrement.';
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
    if (!community && !feed && !ephemeris && !publications && control('source').value === 'applications') {
      body.set('source', applications.value.trim().replace(/\s+/g, ' '));
    }
    if (widgetType === 'shortcuts') { window.AuroraHomeShortcuts.serialize(body); }
    if (widgetType === 'bookmarks') { body.set('emptyFragment', control('emptyFragment').value.trim()); }
    if (widgetType === 'search') { body.set('template', control('template').value.trim()); }
    if (community) {
      ['source', 'group', 'fields'].filter(key => fields[key]).forEach(key =>
        body.set(key, control(key).value.trim().replace(/\s+/g, ' ')));
    }
    if (feed) { body.set('source', control('source').value.trim().replace(/\s+/g, ' ')); }
    try {
      const data = await request(community ? configurationAction('save') : feed ? (widgetType === 'events' ? 'saveEvents' : 'saveRss') : ephemeris ? 'saveEphemeris' : publications ? 'savePublications' : 'saveNews', body, csrf);
      revision = data.revision;
      status.textContent = 'Configuration enregistrée. Rechargez la page d’accueil pour la voir.';
    } catch (error) { status.textContent = error.message; }
    finally { save.disabled = false; }
  });
});
