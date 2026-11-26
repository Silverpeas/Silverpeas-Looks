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
// Home shortcuts only. Toolbar shortcuts keep their separate configuration.
window.AuroraHomeShortcuts = (() => {
  const keys = ['label', 'url', 'iconUrl', 'target'];
  let rows = [];
  const values = () => rows.map(row => Object.fromEntries(keys.map(key => [key, row.inputs[key].value.trim()])));
  let uploadImage;
  const load = (entries, upload = uploadImage) => {
    uploadImage = upload;
    const container = document.getElementById('shortcut-entries');
    container.replaceChildren();
    rows = [];
    entries.forEach((entry, index) => {
      const fieldset = document.createElement('tr');
      const cell = () => {
        const element = document.createElement('td');
        fieldset.append(element);
        return element;
      };
      const row = {inputs: {}};
      rows.push(row);
      const labels = {label: 'Libellé', url: 'URL', target: 'Cible'};
      ['label', 'url', 'target'].forEach(key => {
        const wrapper = cell();
        const input = document.createElement(key === 'target' ? 'select' : 'input');
        input.id = 'shortcut-' + index + '-' + key;
        input.setAttribute('aria-label', labels[key] + ' du raccourci ' + (index + 1));
        if (key === 'target') {
          const options = new Map([
            ['', 'Par défaut'], ['_self', 'Même cadre'], ['_blank', 'Nouvel onglet'],
            ['bottomFrame', 'Zone de contenu Silverpeas'], ['_top', 'Fenêtre entière'],
            ['_parent', 'Cadre parent']
          ]);
          // Preserve named frames already configured, even when they are custom.
          if (entry.target && !options.has(entry.target)) { options.set(entry.target, entry.target); }
          options.forEach((text, value) => {
            const option = document.createElement('option');
            option.value = value;
            option.textContent = text;
            input.append(option);
          });
        } else {
          input.type = 'text';
          input.maxLength = key === 'label' ? 200 : 2048;
          input.required = key === 'url';
        }
        input.value = entry[key] || '';
        row.inputs[key] = input;
        wrapper.append(input);
      });
      // The stored URL is retained internally; only image import is exposed.
      row.inputs.iconUrl = {value: entry.iconUrl || ''};
      const iconCell = cell();
      const image = document.createElement('input');
      image.type = 'file';
      image.accept = 'image/png,image/jpeg,image/gif,image/webp';
      image.id = 'shortcut-' + index + '-image';
      image.hidden = true;
      const imageLabel = document.createElement('button');
      imageLabel.type = 'button';
      imageLabel.className = 'simple-button';
      imageLabel.textContent = 'Importer';
      imageLabel.setAttribute('aria-label', 'Importer l’icône du raccourci ' + (index + 1));
      imageLabel.addEventListener('click', () => image.click());
      const preview = document.createElement('img');
      preview.className = 'homepage-shortcut-preview';
      preview.alt = 'Aperçu de l’icône';
      const refreshPreview = () => {
        preview.hidden = !row.inputs.iconUrl.value;
        preview.src = row.inputs.iconUrl.value;
      };
      refreshPreview();
      const message = document.createElement('p');
      message.setAttribute('role', 'status');
      image.addEventListener('change', async () => {
        if (!image.files.length) { return; }
        const controls = rows.flatMap(item => item.controls);
        const disabled = controls.map(control => control.disabled);
        controls.forEach(control => { control.disabled = true; });
        document.getElementById('shortcut-add').disabled = true;
        message.textContent = 'Envoi de l’image…';
        try {
          row.inputs.iconUrl.value = await uploadImage(image.files[0]);
          refreshPreview();
          message.textContent = 'Image importée. Enregistrez le widget pour appliquer la modification.';
        } catch (error) { message.textContent = error.message; }
        finally {
          image.value = '';
          controls.forEach((control, index) => { control.disabled = disabled[index]; });
          document.getElementById('shortcut-add').disabled = rows.length >= 100;
        }
      });
      iconCell.className = 'homepage-shortcut-icon';
      iconCell.append(preview, imageLabel, image, message);
      const actions = document.createElement('div');
      actions.className = 'homepage-shortcut-actions';
      const buttons = [imageLabel, image];
      const button = (text, disabled, action, symbol) => {
        const element = document.createElement('button');
        element.type = 'button';
        element.className = 'simple-button';
        element.textContent = symbol;
        element.title = text;
        element.setAttribute('aria-label', text + ' le raccourci ' + (index + 1));
        buttons.push(element);
        element.disabled = disabled;
        element.addEventListener('click', action);
        actions.append(element);
      };
      const move = offset => {
        const entries = values();
        [entries[index], entries[index + offset]] = [entries[index + offset], entries[index]];
        load(entries);
        rows[index + offset].inputs.label.focus();
      };
      button('Monter', index === 0, () => move(-1), '↑');
      button('Descendre', index === entries.length - 1, () => move(1), '↓');
      button('Supprimer', false, () => {
        const entries = values();
        entries.splice(index, 1);
        load(entries);
        document.getElementById('shortcut-add').focus();
      }, '×');
      row.controls = [...Object.values(row.inputs).filter(input => input !== row.inputs.iconUrl), ...buttons];
      cell().append(actions);
      container.append(fieldset);
    });
    const add = document.getElementById('shortcut-add');
    add.disabled = rows.length >= 100;
    add.onclick = () => {
      if (rows.length >= 100) { return; }
      load([...values(), {label: '', url: '', iconUrl: '', target: ''}]);
      rows[rows.length - 1].inputs.label.focus();
    };
  };
  const serialize = body => {
    const entries = values();
    body.set('count', entries.length);
    entries.forEach((entry, index) => keys.forEach(key => body.set('entry.' + index + '.' + key, entry[key])));
  };
  return {load, serialize};
})();
