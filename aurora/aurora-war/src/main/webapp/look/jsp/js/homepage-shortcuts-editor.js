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
      const fieldset = document.createElement('fieldset');
      fieldset.className = 'homepage-shortcut-entry';
      const legend = document.createElement('legend');
      legend.textContent = 'Raccourci ' + (index + 1);
      fieldset.append(legend);
      const row = {inputs: {}};
      rows.push(row);
      const labels = {label: 'Libellé', url: 'Adresse du lien', iconUrl: 'Adresse de l’icône (facultative)',
        target: 'Cible (vide, _self, _blank, _top, _parent ou nom de fenêtre)'};
      keys.forEach(key => {
        const wrapper = document.createElement('div');
        wrapper.className = 'homepage-field';
        const label = document.createElement('label');
        const input = document.createElement('input');
        input.id = 'shortcut-' + index + '-' + key;
        label.htmlFor = input.id;
        label.textContent = labels[key];
        input.type = 'text';
        input.maxLength = key === 'label' ? 200 : key === 'target' ? 64 : 2048;
        input.required = key === 'url';
        input.value = entry[key] || '';
        row.inputs[key] = input;
        wrapper.append(label, input);
        fieldset.append(wrapper);
      });
      const image = document.createElement('input');
      image.type = 'file';
      image.accept = 'image/png,image/jpeg,image/gif,image/webp';
      image.id = 'shortcut-' + index + '-image';
      const imageLabel = document.createElement('label');
      imageLabel.htmlFor = image.id;
      imageLabel.textContent = 'Importer une image (5 Mo maximum)';
      const preview = document.createElement('img');
      preview.className = 'homepage-shortcut-preview';
      preview.alt = 'Aperçu de l’icône';
      const refreshPreview = () => {
        preview.hidden = !row.inputs.iconUrl.value;
        preview.src = row.inputs.iconUrl.value;
      };
      row.inputs.iconUrl.addEventListener('input', refreshPreview);
      refreshPreview();
      const message = document.createElement('p');
      message.setAttribute('role', 'status');
      image.addEventListener('change', async () => {
        if (!image.files.length) { return; }
        const fieldsets = rows.map(item => item.fieldset);
        fieldsets.forEach(item => { item.disabled = true; });
        document.getElementById('shortcut-add').disabled = true;
        message.textContent = 'Envoi de l’image…';
        try {
          row.inputs.iconUrl.value = await uploadImage(image.files[0]);
          refreshPreview();
          message.textContent = 'Image importée. Enregistrez le widget pour appliquer la modification.';
        } catch (error) { message.textContent = error.message; }
        finally {
          image.value = '';
          fieldsets.forEach(item => { item.disabled = false; });
          document.getElementById('shortcut-add').disabled = rows.length >= 100;
        }
      });
      row.fieldset = fieldset;
      fieldset.append(imageLabel, image, preview, message);
      const actions = document.createElement('div');
      actions.className = 'homepage-shortcut-actions';
      const button = (text, disabled, action) => {
        const element = document.createElement('button');
        element.type = 'button';
        element.className = 'sp_button';
        element.textContent = text;
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
      button('Monter', index === 0, () => move(-1));
      button('Descendre', index === entries.length - 1, () => move(1));
      button('Supprimer', false, () => {
        const entries = values();
        entries.splice(index, 1);
        load(entries);
        document.getElementById('shortcut-add').focus();
      });
      fieldset.append(actions);
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
