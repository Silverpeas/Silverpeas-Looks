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

test('taxonomy submits its own selected values and clears stale values', () => {
  let submit;
  let selection = {length: 1, flatten: () => '1,2,3'};
  const form = {dataset: {mode: 'taxonomy', labelsInside: 'true'},
    elements: {AxisValueCouples: {value: 'old'}}, addEventListener: (name, callback) => { submit = callback; }};
  const calls = [];
  vm.runInNewContext(source('homepage-search-frame.js'), {
    whenSilverpeasReady: callback => callback(), document: {getElementById: () => form},
    $: () => ({pdc(action, options) { calls.push([action, options]); return selection; }})
  });
  assert.equal(calls[0][0], 'used');
  assert.equal(calls[0][1].labelInsideSelect, true);
  submit();
  assert.equal(form.elements.AxisValueCouples.value, '1,2,3');
  selection = {length: 0};
  submit();
  assert.equal(form.elements.AxisValueCouples.value, '');
});

test('advanced forms do not initialize taxonomy', () => {
  vm.runInNewContext(source('homepage-search-frame.js'), {
    whenSilverpeasReady: callback => callback(),
    document: {getElementById: () => ({dataset: {mode: 'advanced'}})},
    $: () => { throw new Error('Unexpected taxonomy access'); }
  });
});

test('each search frame follows its own content height and reconnects on reload', () => {
  const frames = [100, 250].map(height => ({classList: {contains: () => false}, contentDocument: {readyState: 'complete', body: {getBoundingClientRect: () => ({height})}},
    addEventListener(name, callback) { this.loaded = callback; }}));
  const observers = [];
  class Observer {
    constructor(callback) { this.callback = callback; observers.push(this); }
    observe(body) { this.body = body; }
    disconnect() { this.disconnected = true; }
  }
  vm.runInNewContext(source('homepage-search.js'), {
    window: {addEventListener: (name, callback) => callback()},
    document: {querySelectorAll: () => frames}, ResizeObserver: Observer
  });
  // Already loaded frames connect immediately; subsequent loads replace the observer.
  assert.equal(frames[0].height, 116);
  assert.equal(frames[1].height, 266);
  assert.notEqual(observers[0].body, observers[1].body);
  frames[0].loaded();
  assert.equal(observers[0].disconnected, true);
});

test('hides an empty bookmarks frame without affecting another instance', () => {
  const frames = [true, false].map(hidden => ({
    classList: {contains: name => name === 'homepage-bookmarks-frame'}, parentElement: {},
    contentDocument: {readyState: 'complete', getElementById: () => hidden ? {} : null,
      body: {getBoundingClientRect: () => ({height: 120})}}, addEventListener() {}
  }));
  vm.runInNewContext(source('homepage-search.js'), {
    window: {addEventListener: (name, callback) => callback()},
    document: {querySelectorAll: () => frames},
    ResizeObserver: class { observe() {} disconnect() {} }
  });
  assert.equal(frames[0].parentElement.hidden, true);
  assert.equal(frames[1].parentElement.hidden, false);
});

 test('space taxonomy initializes with the current workspace', () => {
  let options;
  vm.runInNewContext(source('homepage-search-frame.js'), {
    whenSilverpeasReady: callback => callback(),
    document: {getElementById: () => ({dataset: {mode: 'taxonomy', spaceId: 'WA1'}, addEventListener() {}})},
    $: () => ({pdc(action, value) { options = value; }})
  });
  assert.equal(options.workspace, 'WA1');
});
