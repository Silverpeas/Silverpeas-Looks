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
const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');
class Element {
  constructor() { this.children = []; this.events = {}; this.value = ''; }
  append(child) { this.children.push(child); }
  replaceChildren() { this.children = []; }
  setAttribute() {}
  setCustomValidity(message) { this.error = message; }
  addEventListener(type, listener) { this.events[type] = listener; }
}
async function test(multiple) {
  const field = new Element(); field.value = 'quickinfo1';
  const input = new Element(), list = new Element(), chips = new Element(), status = new Element();
  const picker = {dataset: {sourceField: 'value', sourceType: 'quickinfo', sourceMultiple: String(multiple)},
    querySelector: selector => ({input, datalist: list, '.homepage-source-selection': chips, small: status})[selector]};
  const context = {window: {}, document: {querySelectorAll: () => [picker], getElementById: () => field,
    createElement: () => new Element()}};
  vm.runInNewContext(fs.readFileSync(__dirname + '/../../main/webapp/look/jsp/js/homepage-source-picker.js', 'utf8'), context);
  await context.window.AuroraHomepageSources.initialize(async (action, body, csrf, params) => {
    assert.equal(action, 'sources'); assert.equal(params.sourceType, 'quickinfo');
    return {sources: [{id: 'quickinfo1', label: 'Première'}, {id: 'quickinfo2', label: '<Application>'}]};
  });
  assert.equal(chips.children[0].children[0].textContent, 'Première');
  input.value = 'inconnue'; input.events.input();
  assert.ok(input.error); assert.equal(field.value, 'quickinfo1');
  input.value = '<Application> (quickinfo2)'; input.events.input();
  assert.equal(field.value, multiple ? 'quickinfo1 quickinfo2' : 'quickinfo2');
  assert.equal(input.error, '');
  chips.children[chips.children.length - 1].children[1].events.click();
  assert.equal(field.value, multiple ? 'quickinfo1' : '');
}
(async () => { await test(true); await test(false); console.log('Source picker: multiple selection, single selection, validation, labels and removal passed.'); })().catch(error => { console.error(error); process.exitCode = 1; });
