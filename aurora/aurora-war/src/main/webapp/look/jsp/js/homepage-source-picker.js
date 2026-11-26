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
window.AuroraHomepageSources = {
  async initialize(request) {
    for (const picker of document.querySelectorAll('.homepage-source-picker')) {
      const value = document.getElementById(picker.dataset.sourceField);
      const search = picker.querySelector('input');
      const suggestions = picker.querySelector('datalist');
      const selection = picker.querySelector('.homepage-source-selection');
      const status = picker.querySelector('small');
      const multiple = picker.dataset.sourceMultiple === 'true';
      let selected = value.value.trim().split(/\s+/).filter(Boolean);
      const render = () => {
        value.value = selected.join(' ');
        selection.replaceChildren();
        selected.forEach(id => {
          const item = sources.find(source => source.id === id ||
            picker.dataset.sourceType === 'spaces' && source.id.replace(/^WA/, '') === id.replace(/^WA/, ''));
          const chip = document.createElement('span');
          chip.className = 'homepage-source-chip';
          const label = document.createElement('span');
          label.textContent = item ? item.label : id;
          chip.append(label);
          const remove = document.createElement('button');
          remove.type = 'button';
          remove.textContent = '×';
          remove.setAttribute('aria-label', 'Retirer ' + label.textContent);
          remove.addEventListener('click', () => {
            selected = selected.filter(candidate => candidate !== id);
            render();
          });
          chip.append(remove);
          selection.append(chip);
        });
        suggestions.replaceChildren();
        sources.filter(source => !selected.includes(source.id)).forEach(source => {
          const option = document.createElement('option');
          option.value = source.label + ' (' + source.id + ')';
          suggestions.append(option);
        });
      };
      let sources = [];
      render();
      try {
        sources = (await request('sources', null, null, {sourceType: picker.dataset.sourceType})).sources;
        sources.sort((a, b) => a.label.localeCompare(b.label, 'fr'));
        render();
        if (!sources.length) { status.textContent = 'Aucune source accessible.'; }
      } catch (error) { status.textContent = error.message; }
      const choose = () => {
        const source = sources.find(item => item.label + ' (' + item.id + ')' === search.value);
        if (!source) {
          search.setCustomValidity(search.value ? 'Sélectionnez une source dans la liste.' : '');
          return;
        }
        selected = multiple ? [...new Set([...selected, source.id])] : [source.id];
        search.value = '';
        search.setCustomValidity('');
        render();
      };
      search.addEventListener('input', choose);
      search.addEventListener('change', choose);
      search.addEventListener('keydown', event => {
        if (event.key === 'Enter') { event.preventDefault(); choose(); }
      });
    }
  }
};
