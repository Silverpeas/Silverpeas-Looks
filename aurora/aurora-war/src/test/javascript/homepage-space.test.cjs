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
  '../../main/webapp/look/jsp/js/homepage-space.js'), 'utf8');

test('space navigation uses platform navigation and preserves modified clicks', () => {
  let click;
  const opened = [];
  const loaded = [];
  const elements = [{id: 'feed1', dataset: {feedUrl: '/feed1'}}, {id: 'feed2', dataset: {feedUrl: '/feed2'}}];
  vm.runInNewContext(source, {
    whenSilverpeasReady(callback) { callback(); },
    document: {
      addEventListener(name, handler) { assert.equal(name, 'click'); click = handler; },
      querySelectorAll() { return elements; }
    },
    sp: {ajaxRequest(url) { return {loadTarget(target) { loaded.push([url, target]); }}; }},
    spWindow: {loadSpace(id) { opened.push(['space', id]); }, loadComponent(id) { opened.push(['component', id]); }}
  });
  assert.deepEqual(loaded, [['/feed1', '#feed1'], ['/feed2', '#feed2']]);
  for (const kind of ['space', 'component']) {
    let prevented = false;
    const target = {closest() { return {dataset: {navigationKind: kind, navigationId: 'item1'}}; }};
    click({target, button: 0, preventDefault() { prevented = true; }});
    assert.equal(prevented, true);
    assert.deepEqual(opened.at(-1), [kind, 'item1']);
    for (const modifiers of [{ctrlKey: true}, {metaKey: true}, {shiftKey: true}, {altKey: true}, {button: 1}]) {
      click({target, button: 0, ...modifiers, preventDefault() { assert.fail('Modified click intercepted'); }});
    }
  }
  click({target: {closest() { return null; }}});
  assert.equal(opened.length, 2);
});
