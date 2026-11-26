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
const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');
const path = require('node:path');
const source = fs.readFileSync(path.resolve(__dirname,
  '../../main/webapp/look/jsp/js/homepage-shortcuts-editor.js'), 'utf8');

function editor() {
  const elements = {};
  class Element {
    constructor(tag) { this.tag = tag; this.children = []; this.handlers = {}; this.value = ''; this.classList = {add() {}, remove() {}}; }
    setAttribute(name, value) { this[name] = value; }
    set id(value) { this._id = value; elements[value] = this; }
    get id() { return this._id; }
    append(...items) { this.children.push(...items); }
    replaceChildren() { this.children = []; }
    addEventListener(name, handler) { this.handlers[name] = handler; }
    focus() { this.focused = true; }
    click() { if (!this.disabled) { (this.handlers.click || this.onclick)(); } }
  }
  const document = {createElementNS: (ns, tag) => new Element(tag), createElement: tag => new Element(tag), getElementById: id => elements[id]};
  const container = document.createElement('tbody'); container.id = 'shortcut-entries';
  const add = document.createElement('button'); add.id = 'shortcut-add';
  const window = {};
  vm.runInNewContext(source, {window, document});
  return {api: window.AuroraHomeShortcuts, elements, container, add};
}
const link = label => ({label, url: '/docs/' + label, iconUrl: '', target: 'bottomFrame'});

test('home editor adds, edits, moves and removes entries while preserving unsaved values', () => {
  const {api, elements, container, add} = editor();
  api.load([link('A'), link('B')]);
  assert.equal(container.children.length, 2);
  elements['shortcut-0-label'].value = ' A & <title> ';
  container.children[0].children[0].children[0].handlers.keydown({key: 'ArrowDown', preventDefault() {}});
  assert.equal(elements['shortcut-0-label'].value, 'B');
  assert.equal(elements['shortcut-1-label'].value, 'A & <title>');
  assert.equal(elements['shortcut-1-target'].value, 'bottomFrame');
  container.children[1].children[0].children[0].handlers.keydown({key: 'ArrowUp', preventDefault() {}});
  assert.equal(elements['shortcut-0-label'].value, 'A & <title>');
  add.click();
  assert.equal(container.children.length, 3);
  assert.equal(elements['shortcut-2-url'].required, true);
  elements['shortcut-2-label'].value = 'C';
  elements['shortcut-2-url'].value = ' https://example.org/?a=1&b=2 ';
  container.children[1].children.at(-1).children[0].children[0].click();
  const body = new URLSearchParams(); api.serialize(body);
  assert.equal(body.get('count'), '2');
  assert.equal(body.get('entry.0.label'), 'A & <title>');
  assert.equal(body.get('entry.1.url'), 'https://example.org/?a=1&b=2');
  assert.equal(body.has('entry.2.url'), false);
});

test('home editor handles empty lists and enforces the maximum of one hundred links', () => {
  const {api, container, add} = editor();
  api.load([]);
  let body = new URLSearchParams(); api.serialize(body);
  assert.equal(body.get('count'), '0');
  assert.equal(add.disabled, false);
  api.load(Array.from({length: 100}, (_, i) => link(String(i))));
  assert.equal(add.disabled, true);
  add.click();
  assert.equal(container.children.length, 100);
  container.children[0].children.at(-1).children[0].children[0].click();
  assert.equal(add.disabled, false);
  add.click();
  assert.equal(container.children.length, 100);
  assert.equal(add.disabled, true);
});

 test('uploaded shortcut image is previewed and serialized, errors preserve the previous image', async () => {
  const {api, elements} = editor();
  let reject = false;
  api.load([link('Image')], async file => {
    assert.equal(file.name, 'icon.png');
    if (reject) { throw new Error('Envoi impossible'); }
    return '/silverpeas/AuroraHomepageContent?action=asset&name=image.png';
  });
  const image = elements['shortcut-0-image'];
  image.files = [{name: 'icon.png'}];
  await image.handlers.change();
  let body = new URLSearchParams(); api.serialize(body);
  assert.match(body.get('entry.0.iconUrl'), /action=asset/);
  reject = true;
  await image.handlers.change();
  body = new URLSearchParams(); api.serialize(body);
  assert.match(body.get('entry.0.iconUrl'), /action=asset/);
  assert.equal(elements['shortcut-add'].disabled, false);
});

test('table uses target choices and keeps existing icons and named frames without an icon URL field', () => {
  const {api, elements, container} = editor();
  api.load([{...link('Docs'), target: 'customFrame', iconUrl: '/weblib/docs.svg'}]);
  assert.equal(container.children[0].tag, 'tr');
  assert.equal(container.children[0].children.length, 6);
  const target = elements['shortcut-0-target'];
  assert.equal(target.tag, 'select');
  assert.ok(target.children.some(option => option.value === '_blank'));
  assert.ok(target.children.some(option => option.value === 'customFrame'));
  assert.equal(elements['shortcut-0-iconUrl'], undefined);
  const body = new URLSearchParams(); api.serialize(body);
  assert.equal(body.get('entry.0.iconUrl'), '/weblib/docs.svg');
  assert.equal(body.get('entry.0.target'), 'customFrame');
});

test('drag handle reorders rows and preserves unsaved values and icon URLs', () => {
  const {api, elements, container} = editor();
  api.load([{...link('A'), iconUrl: '/a.png'}, link('B'), link('C')]);
  elements['shortcut-0-label'].value = 'Edited A';
  const transfer = {setData() {}, setDragImage() {}};
  container.children[0].children[0].children[0].handlers.dragstart({dataTransfer: transfer});
  let accepted = false;
  container.children[2].handlers.dragover({dataTransfer: transfer, preventDefault() { accepted = true; }});
  assert.equal(accepted, true);
  container.children[2].handlers.drop({preventDefault() {}});
  const body = new URLSearchParams(); api.serialize(body);
  assert.equal(body.get('entry.0.label'), 'B');
  assert.equal(body.get('entry.1.label'), 'C');
  assert.equal(body.get('entry.2.label'), 'Edited A');
  assert.equal(body.get('entry.2.iconUrl'), '/a.png');
  assert.equal(container.children[2].children[0].children[0].focused, true);
});
