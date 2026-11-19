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
  management.querySelectorAll('.homepage-delete-widget').forEach(function(button) {
    button.addEventListener('click', async function() {
      if (!window.confirm('Supprimer ce widget de la page d’accueil ?')) { return; }
      button.disabled = true;
      const status = management.querySelector('.homepage-delete-status');
      status.textContent = '';
      const parameters = new URLSearchParams({action: 'deleteWidget'});
      ['widget', 'revision', 'kind', 'owner', 'language'].forEach(function(name) {
        parameters.set(name, button.dataset[name]);
      });
      try {
        const response = await fetch(management.dataset.endpoint, {
          method: 'POST', credentials: 'same-origin',
          headers: {'X-Aurora-CSRF': management.dataset.csrf}, body: parameters
        });
        if (!response.ok) {
          status.textContent = response.status === 409
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
    });
  });
});
