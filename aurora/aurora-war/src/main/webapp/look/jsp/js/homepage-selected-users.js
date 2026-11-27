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
window.AuroraSelectedUsers = {
  load(initial, request) {
    const input = document.getElementById('selected-users-search');
    const results = document.getElementById('selected-users-results');
    const list = document.getElementById('selected-users-list');
    const value = document.getElementById('news-user-ids');
    const status = document.getElementById('selected-users-status');
    let selected = initial || [];
    let timer;
    let sequence = 0;
    const close = () => { results.hidden = true; input.setAttribute('aria-expanded', 'false'); };
    const render = () => {
      value.value = selected.map(user => user.id).join(' ');
      list.replaceChildren();
      selected.forEach(user => {
        const item = document.createElement('li');
        const label = document.createElement('span');
        label.textContent = user.label;
        const remove = document.createElement('button');
        remove.type = 'button';
        remove.className = 'simple-button';
        remove.textContent = '×';
        remove.setAttribute('aria-label', 'Retirer ' + user.label);
        remove.addEventListener('click', () => {
          selected = selected.filter(person => person.id !== user.id);
          render();
          input.focus();
        });
        item.append(label, remove);
        list.append(item);
      });
    };
    input.addEventListener('input', () => {
      clearTimeout(timer);
      const current = ++sequence;
      close();
      status.textContent = '';
      const query = input.value.trim();
      if (query.length < 2) { return; }
      timer = setTimeout(async () => {
        try {
          const data = await request('selectableUsers', null, null, {q: query});
          if (current !== sequence) { return; }
          results.replaceChildren();
          data.users.filter(user => !selected.some(person => person.id === user.id)).forEach(user => {
            const item = document.createElement('li');
            const button = document.createElement('button');
            button.type = 'button';
            button.textContent = user.label;
            button.addEventListener('click', () => {
              if (selected.length >= 100) { status.textContent = '100 utilisateurs maximum.'; return; }
              selected.push(user);
              render();
              input.value = '';
              ++sequence;
              close();
              input.focus();
            });
            item.append(button);
            results.append(item);
          });
          results.hidden = !results.childElementCount;
          input.setAttribute('aria-expanded', String(!results.hidden));
          status.textContent = results.hidden ? 'Aucun autre utilisateur trouvé.' : '';
        } catch (error) { if (current === sequence) { status.textContent = error.message; } }
      }, 250);
    });
    input.addEventListener('keydown', event => {
      if (event.key === 'Escape') { ++sequence; close(); }
      if (event.key === 'ArrowDown' && !results.hidden) {
        event.preventDefault();
        results.querySelector('button')?.focus();
      }
    });
    results.addEventListener('keydown', event => {
      const buttons = Array.from(results.querySelectorAll('button'));
      const index = buttons.indexOf(document.activeElement);
      if (event.key === 'ArrowDown' || event.key === 'ArrowUp') {
        event.preventDefault();
        const next = index + (event.key === 'ArrowDown' ? 1 : -1);
        (buttons[next] || input).focus();
      }
      if (event.key === 'Escape') { ++sequence; close(); input.focus(); }
    });
    document.addEventListener('click', event => {
      if (!event.target.closest('.homepage-user-picker')) { ++sequence; close(); }
    });
    render();
  }
};
