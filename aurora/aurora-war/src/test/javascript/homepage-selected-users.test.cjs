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

test('picker searches, adds and removes choices without duplicating users', async () => {
  class Element {
    constructor() { this.children = []; this.handlers = {}; this.value = ''; this.attributes = {}; }
    append(...items) { this.children.push(...items); }
    replaceChildren() { this.children = []; }
    addEventListener(type, callback) { this.handlers[type] = callback; }
    setAttribute(key, value) { this.attributes[key] = value; }
    get childElementCount() { return this.children.length; }
    focus() {}
  }
  const elements = {};
  for (const id of ['selected-users-search', 'selected-users-results', 'selected-users-list',
    'news-user-ids', 'selected-users-status']) { elements[id] = new Element(); }
  const document = {getElementById: id => elements[id], createElement: () => new Element(), addEventListener() {}};
  let runTimer;
  let query;
  const window = {};
  vm.runInNewContext(fs.readFileSync(path.resolve(__dirname,
    '../../main/webapp/look/jsp/js/homepage-selected-users.js'), 'utf8'),
  {window, document, clearTimeout() {}, setTimeout(callback) { runTimer = callback; }});
  window.AuroraSelectedUsers.load([{id: '1', label: 'Alice'}], async (action, body, token, parameters) => {
    assert.equal(action, 'selectableUsers');
    query = parameters.q;
    return {users: [{id: '1', label: 'Alice'}, {id: '2', label: '<Bob>'}]};
  });
  const input = elements['selected-users-search'];
  input.value = 'Bo';
  input.handlers.input();
  await runTimer();
  assert.equal(query, 'Bo');
  assert.equal(elements['selected-users-results'].children.length, 1);
  const choice = elements['selected-users-results'].children[0].children[0];
  assert.equal(choice.textContent, '<Bob>');
  choice.handlers.click();
  assert.equal(elements['news-user-ids'].value, '1 2');
  assert.equal(elements['selected-users-results'].hidden, true);
  elements['selected-users-list'].children[0].children[1].handlers.click();
  assert.equal(elements['news-user-ids'].value, '2');
});
