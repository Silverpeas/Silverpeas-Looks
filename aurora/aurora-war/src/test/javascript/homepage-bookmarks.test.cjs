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
const source = name => fs.readFileSync(path.resolve(__dirname, '../../main/webapp/look/jsp/js/' + name), 'utf8');

test('mounts the existing personal links component with reader data', () => {
  const container = {dataset: {links: '[{"name":"Private <link>","url":"/Component/kmelia1"}]'}};
  let mounted;
  let data;
  vm.runInNewContext(source('homepage-bookmarks.js'), {
    whenSilverpeasReady: callback => callback(),
    document: {getElementById: id => id === 'mylinkspeas-widget' ? container : null},
    SpVue: {createApp(options) { data = options.data(); return {mount(target) { mounted = target; }}; }}
  });
  assert.equal(mounted, container);
  assert.equal(data.links[0].name, 'Private <link>');
});

test('loads the empty-state fragment only in its own container', () => {
  const empty = {dataset: {fragment: '/silverpeas/empty.html'}};
  const calls = [];
  vm.runInNewContext(source('homepage-bookmarks.js'), {
    whenSilverpeasReady: callback => callback(),
    document: {getElementById: id => id === 'homepage-bookmarks-empty' ? empty : null},
    sp: {ajaxRequest(url) { return {loadTarget(target) { calls.push({url, target}); }}; }}
  });
  assert.equal(calls[0].url, '/silverpeas/empty.html');
  assert.equal(calls[0].target, empty);
});

test('makes no request when no fragment or empty-state container is present', () => {
  for (const empty of [null, {dataset: {fragment: ''}}]) {
    vm.runInNewContext(source('homepage-bookmarks.js'), {
      whenSilverpeasReady: callback => callback(),
      document: {getElementById: id => id === 'homepage-bookmarks-empty' ? empty : null},
      sp: {ajaxRequest() { throw new Error('Unexpected fragment request'); }}
    });
  }
});
