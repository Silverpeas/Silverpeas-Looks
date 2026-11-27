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
whenSilverpeasReady(function() {
  const management = document.querySelector('.homepage-content-management');
  if (!management) { return; }
  const filters = management.querySelectorAll('[data-catalog-filter]');
  const tiles = management.querySelectorAll('[data-category]');
  filters.forEach(function(filter) {
    filter.addEventListener('click', function() {
      filters.forEach(item => item.setAttribute('aria-pressed', String(item === filter)));
      tiles.forEach(function(tile) {
        tile.hidden = filter.dataset.catalogFilter !== 'all'
            && tile.dataset.category !== filter.dataset.catalogFilter;
      });
    });
  });
  const dialog = management.querySelector('.homepage-delete-dialog');
  const status = management.querySelector('.homepage-delete-status');
  let pendingButton;

  function deleteRequest(parameters) {
    // Silverpeas adds its session security token to XMLHttpRequest.send.
    return new Promise(function(resolve, reject) {
      const xhr = new XMLHttpRequest();
      xhr.open('POST', management.dataset.endpoint);
      xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded;charset=UTF-8');
      xhr.setRequestHeader('X-Aurora-CSRF', management.dataset.csrf);
      xhr.onload = () => resolve(xhr.status);
      xhr.onerror = () => reject(new Error('Network error'));
      xhr.ontimeout = () => reject(new Error('Request timeout'));
      xhr.timeout = 60000;
      xhr.send(parameters.toString());
    });
  }

  async function removeWidget(button) {
    button.disabled = true;
    status.textContent = '';
    const parameters = new URLSearchParams({action: 'deleteWidget'});
    ['widget', 'revision', 'kind', 'owner', 'language'].forEach(function(name) {
      parameters.set(name, button.dataset[name]);
    });
    try {
      const responseStatus = await deleteRequest(parameters);
      if (responseStatus !== 200) {
        status.textContent = responseStatus === 409
            ? 'Ce widget a été modifié. Rechargez la page avant de réessayer.'
            : 'La suppression a échoué. Rechargez la page avant de réessayer.';
        return;
      }
      window.location.reload();
    } catch (error) {
      status.textContent = 'La suppression a échoué. Vérifiez votre connexion.';
    } finally {
      button.disabled = false;
    }
  }

  const table = management.querySelector('.homepage-installed-table');
  let saving = false;
  let dragged;
  const rows = () => Array.from(table.querySelectorAll('tbody tr'));

  async function saveLayouts() {
    if (saving) { return; }
    saving = true;
    status.textContent = 'Enregistrement…';
    const parameters = new URLSearchParams({action: 'updateLayouts',
      kind: management.dataset.kind, owner: management.dataset.owner,
      language: management.dataset.language});
    rows().forEach(function(row) {
      parameters.append('widgets', row.dataset.widget);
      parameters.append('revisions', row.dataset.revision);
      parameters.append('enabled', row.dataset.enabled);
    });
    try {
      const result = await deleteRequest(parameters);
      if (result === 200) { window.location.reload(); return; }
      status.textContent = result === 409
          ? 'La liste a été modifiée. Rechargez la page avant de réessayer.'
          : 'Enregistrement impossible. Rechargez la page avant de réessayer.';
    } catch (error) {
      status.textContent = 'Enregistrement impossible. Vérifiez votre connexion et rechargez la page.';
    }
    // Keep editing locked after a failure: the displayed order may differ from storage.
  }

  if (table) {
    table.addEventListener('dragstart', function(event) {
      const grip = event.target.closest('.homepage-widget-grip');
      if (!grip || saving) { event.preventDefault(); return; }
      dragged = grip.closest('tr');
      dragged.classList.add('homepage-dragging');
      event.dataTransfer.effectAllowed = 'move';
      event.dataTransfer.setData('text/plain', dragged.dataset.widget);
    });
    table.addEventListener('dragover', function(event) {
      const target = event.target.closest('tbody tr');
      if (dragged && target && target.dataset.zone === dragged.dataset.zone && !saving) {
        event.preventDefault();
        event.dataTransfer.dropEffect = 'move';
      }
    });
    table.addEventListener('drop', async function(event) {
      const target = event.target.closest('tbody tr');
      if (!dragged || !target || dragged === target || saving
          || target.dataset.zone !== dragged.dataset.zone) { return; }
      event.preventDefault();
      const ordered = rows();
      target.parentNode.insertBefore(dragged,
          ordered.indexOf(dragged) < ordered.indexOf(target) ? target.nextSibling : target);
      await saveLayouts();
    });
    table.addEventListener('dragend', function() {
      if (dragged) { dragged.classList.remove('homepage-dragging'); }
      dragged = undefined;
    });
    table.addEventListener('keydown', async function(event) {
      const grip = event.target.closest('.homepage-widget-grip');
      if (!grip || saving || !['ArrowUp', 'ArrowDown'].includes(event.key)) { return; }
      event.preventDefault();
      const row = grip.closest('tr');
      const siblings = rows().filter(item => item.dataset.zone === row.dataset.zone);
      const index = siblings.indexOf(row);
      const target = siblings[index + (event.key === 'ArrowUp' ? -1 : 1)];
      if (!target) { return; }
      target.parentNode.insertBefore(row, event.key === 'ArrowDown' ? target.nextSibling : target);
      await saveLayouts();
    });
  }

  management.addEventListener('click', async function(event) {
    const button = event.target.closest('button');
    if (!button || !management.contains(button) || button.disabled) { return; }
    if (button.matches('.homepage-status-badge') && !saving) {
      const row = button.closest('tr');
      row.dataset.enabled = String(row.dataset.enabled !== 'true');
      await saveLayouts();
    } else if (button.matches('.homepage-delete-widget') && !saving) {
      event.preventDefault();
      pendingButton = button;
      dialog.showModal();
    } else if (button.matches('[data-delete-cancel]')) {
      pendingButton = undefined;
      dialog.close();
    } else if (button.matches('[data-delete-confirm]') && pendingButton) {
      const selected = pendingButton;
      pendingButton = undefined;
      dialog.close();
      await removeWidget(selected);
    }
  });
  dialog.addEventListener('cancel', function() {
    pendingButton = undefined;
  });
});
