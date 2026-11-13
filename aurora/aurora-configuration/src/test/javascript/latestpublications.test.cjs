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
const {test} = require('node:test');
const assert = require('node:assert/strict');
const vm = require('node:vm');
const fs = require('node:fs');
const path = require('node:path');
const base = path.resolve(__dirname, '../../main/config/data/web/weblib.war/ckeditor/latestpublications');
let plugin, dialog, widget;
const labels = {};
const editor = {
  lang: {latestpublications: labels},
  addContentsCss() {},
  widgets: {add(name, definition) { widget = definition; }}
};
const context = vm.createContext({
  CKEDITOR: {
    plugins: {add(name, definition) {plugin = definition;}, setLang(name, lang, values) {Object.assign(labels, values);}},
    dialog: {add(name, definition) {if (typeof definition === 'function') dialog = definition(editor);}}
  },
  alert() {}
});
for (const file of ['lang/fr.js', 'plugin.js', 'dialogs/latestpublications.js']) {
  vm.runInContext(fs.readFileSync(path.join(base, file), 'utf8'), context);
}
plugin.init.call({path: '/plugin/'}, editor);
function instance(attributes) {
  return {
    data: {...widget.defaults},
    element: {
      getAttribute(name) { return attributes[name]; },
      setAttributes(values) { Object.assign(attributes, values); },
      setText(value) { this.text = value; }
    },
    setData(key, value) {
      if (typeof key === 'object') Object.assign(this.data, key);
      else this.data[key] = value;
      widget.data.call(this);
    }
  };
}
test('reopens existing values, updates all attributes and removes the preview on output', () => {
  const attributes = {'data-space-id': '123', 'data-limit': '5', 'data-include-subspaces': 'false'};
  const item = instance(attributes);
  widget.init.call(item);
  assert.equal(item.data.includeSubspaces, false);
  assert.equal(item.data.spaceId, '123');
  item.setData('includeSubspaces', true);
  item.setData('limit', '12');
  assert.equal(attributes['data-include-subspaces'], 'true');
  assert.equal(attributes['data-limit'], '12');
  item.setData('includeSubspaces', false);
  assert.equal(attributes['data-include-subspaces'], 'false');
  const element = {children: [{value: item.element.text}], attributes};
  assert.equal(widget.downcast(element), element);
  assert.equal(element.children.length, 0);
  assert.equal(element.attributes['data-space-id'], '123');
});
test('accepts only the target div on reload', () => {
  assert.equal(widget.upcast({name: 'div', hasClass: () => true}), true);
  assert.equal(widget.upcast({name: 'span', hasClass: () => true}), false);
  assert.equal(widget.upcast({name: 'div', hasClass: () => false}), false);
});
test('validates both numeric fields before committing trimmed values', () => {
  for (const field of dialog.contents[0].elements.slice(0, 2)) {
    for (const value of ['', '0', '-1', '1.5', '1e3', '<img>', '9007199254740992']) {
      assert.equal(field.validate.call({getValue: () => value}), false, value);
    }
    assert.equal(field.validate.call({getValue: () => ' 123 '}), true);
    const item = instance({});
    field.commit.call({getValue: () => ' 123 '}, item);
    assert.equal(item.data[field.id], '123');
  }
});
test('checkbox setup and commit preserve boolean false', () => {
  const field = dialog.contents[0].elements[2];
  const item = instance({});
  let value;
  field.setup.call({setValue(v) {value = v;}}, item);
  assert.equal(value, false);
  field.commit.call({getValue: () => true}, item);
  assert.equal(item.data.includeSubspaces, true);
  field.commit.call({getValue: () => false}, item);
  assert.equal(item.data.includeSubspaces, false);
});

test('registers toolbar icons in standard and high resolution', () => {
  assert.equal(plugin.icons, 'latestpublications');
  assert.equal(plugin.hidpi, true);
  for (const [directory, size] of [['icons', 16], ['icons/hidpi', 32]]) {
    const png = fs.readFileSync(path.join(base, directory, 'latestpublications.png'));
    assert.equal(png.readUInt32BE(16), size);
    assert.equal(png.readUInt32BE(20), size);
  }
});
