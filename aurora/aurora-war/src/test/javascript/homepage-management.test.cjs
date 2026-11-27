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
const source = fs.readFileSync(
    'aurora/aurora-war/src/main/webapp/look/jsp/js/homepage-management.js', 'utf8');

function setup(responseStatus = 200, layout = false) {
  let click;
  let escape;
  const state = {calls: 0, reloads: 0};
  const handlers = {};
  const row = {dataset: {widget: 'weather-legacy', revision: 'abc', enabled: 'true', zone: 'right'}};
  const table = {querySelectorAll: () => [row],
    addEventListener: (name, callback) => { handlers[name] = callback; }};
  const status = {textContent: ''};
  const dialog = {
    open: false, showModal() { this.open = true; }, close() { this.open = false; },
    addEventListener: (name, callback) => { escape = callback; }
  };
  const filters = ['all', 'content', 'community', 'services'].map(category => ({
    dataset: {catalogFilter: category},
    setAttribute(name, value) { this[name] = value; },
    addEventListener(name, callback) { this.click = callback; }
  }));
  const tiles = ['content', 'community', 'services'].map(category => ({
    dataset: {category}, hidden: false
  }));
  const management = {
    dataset: {endpoint: '/sp/AuroraHomepageContent', csrf: 'token', kind: 'skins', owner: 'waves', language: 'fr'},
    contains: () => true,
    querySelectorAll: selector => selector === '[data-catalog-filter]' ? filters : tiles,
    addEventListener: (name, callback) => { click = callback; },
    querySelector: selector => selector === '.homepage-installed-table' ? (layout ? table : null)
        : selector === '.homepage-delete-dialog' ? dialog : status
  };
  vm.runInNewContext(source, {
    whenSilverpeasReady: callback => callback(),
    document: {querySelector: () => management}, URLSearchParams,
    window: {location: {reload: () => { state.reloads++; }}},
    XMLHttpRequest: class {
      constructor() { this.headers = {}; }
      open(method, url) {
        assert.equal(method, 'POST');
        assert.equal(url, management.dataset.endpoint);
      }
      setRequestHeader(name, value) { this.headers[name] = value; }
      send(body) {
        // Silverpeas enriches XMLHttpRequest.send to supply its session token.
        this.setRequestHeader('X-Silverpeas-Session', 'platform-token');
        state.calls++;
        assert.equal(this.headers['X-Silverpeas-Session'], 'platform-token');
        assert.equal(this.headers['X-Aurora-CSRF'], 'token');
        assert.equal(this.headers['Content-Type'], 'application/x-www-form-urlencoded;charset=UTF-8');
        const parameters = new URLSearchParams(body);
        if (layout) {
          assert.equal(parameters.get('action'), 'updateLayouts');
          assert.deepEqual(parameters.getAll('widgets'), ['weather-legacy']);
          assert.deepEqual(parameters.getAll('revisions'), ['abc']);
          assert.deepEqual(parameters.getAll('enabled'), ['false']);
        }
        for (const [name, value] of Object.entries({action: layout ? 'updateLayouts' : 'deleteWidget',
          widget: 'weather-legacy', revision: 'abc', kind: 'skins', owner: 'waves', language: 'fr'})) {
          if (!layout || !['widget', 'revision'].includes(name)) { assert.equal(parameters.get(name), value); }
        }
        if (responseStatus === 'network') { this.onerror(); }
        else if (responseStatus === 'timeout') { this.ontimeout(); }
        else { this.status = responseStatus; this.onload(); }
      }
    }
  });
  // Created after initialization, as with rows replaced by AJAX pagination.
  const button = {
    dataset: {widget: 'weather-legacy', revision: 'abc', kind: 'skins', owner: 'waves', language: 'fr'},
    matches: selector => selector === '.homepage-delete-widget'
  };
  const event = target => ({target: {closest: () => target}, preventDefault() {}});
  return {
    state, status, dialog, button, filters, tiles,
    toggle: () => click(event({matches: selector => selector === '.homepage-status-badge',
      closest: () => row})),
    open: () => click(event(button)),
    action: selector => click(event({matches: other => other === selector})),
    escape: () => { escape(); dialog.close(); }
  };
}

test('SVG click on a new row opens confirmation before sending the request', async () => {
  const ui = setup();
  await ui.open();
  assert.equal(ui.dialog.open, true);
  assert.equal(ui.state.calls, 0);
  await ui.action('[data-delete-confirm]');
  assert.equal(ui.dialog.open, false);
  assert.equal(ui.state.calls, 1);
  assert.equal(ui.state.reloads, 1);
});

for (const action of ['cancel', 'escape']) {
  test(action + ' does not delete the widget', async () => {
    const ui = setup();
    await ui.open();
    if (action === 'cancel') { await ui.action('[data-delete-cancel]'); }
    else { ui.escape(); }
    await ui.action('[data-delete-confirm]');
    assert.equal(ui.state.calls, 0);
    assert.equal(ui.dialog.open, false);
  });
}

for (const failure of [409, 403, 'network', 'timeout']) {
  test('failure ' + failure + ' displays an error and enables retry', async () => {
    const ui = setup(failure);
    await ui.open();
    await ui.action('[data-delete-confirm]');
    assert.equal(ui.state.calls, 1);
    assert.equal(ui.state.reloads, 0);
    assert.ok(ui.status.textContent);
    assert.equal(ui.button.disabled, false);
  });
}


test('category filters hide unrelated tiles and Tous restores the catalogue', () => {
  const ui = setup();
  for (const filter of ui.filters) {
    filter.click();
    for (const tile of ui.tiles) {
      assert.equal(tile.hidden, filter.dataset.catalogFilter !== 'all'
          && tile.dataset.category !== filter.dataset.catalogFilter);
    }
    assert.equal(ui.filters.filter(item => item['aria-pressed'] === 'true').length, 1);
    assert.equal(filter['aria-pressed'], 'true');
  }
  ui.filters[0].click();
  assert.ok(ui.tiles.every(tile => !tile.hidden));
});

for (const result of [200, 409, 403, 'network']) {
  test('visibility action persists with revision and handles ' + result, async () => {
    const ui = setup(result, true);
    await ui.toggle();
    assert.equal(ui.state.calls, 1);
    assert.equal(ui.state.reloads, result === 200 ? 1 : 0);
    if (result !== 200) { assert.ok(ui.status.textContent); }
  });
}

test('drag and keyboard reorder within the same column and persist the full list', async () => {
  for (const keyboard of [false, true]) {
    const handlers = {};
    let sent;
    const rows = ['a', 'b', 'c'].map((id, index) => ({
      dataset: {widget: id, revision: id + '-rev', enabled: 'true', zone: index === 2 ? 'right' : 'main'},
      classList: {add() {}, remove() {}}
    }));
    const table = {querySelectorAll: () => rows,
      addEventListener: (name, callback) => { handlers[name] = callback; }};
    const parent = {insertBefore(row, before) {
      rows.splice(rows.indexOf(row), 1);
      rows.splice(before ? rows.indexOf(before) : rows.length, 0, row);
    }};
    rows.forEach(row => { row.parentNode = parent;
      Object.defineProperty(row, 'nextSibling', {get: () => rows[rows.indexOf(row) + 1]});
    });
    const management = {
      dataset: {kind: 'spaces', owner: 'WA1', language: 'fr'},
      querySelectorAll: () => [], addEventListener() {},
      querySelector: selector => selector === '.homepage-installed-table' ? table
          : selector === '.homepage-delete-dialog' ? {addEventListener() {}} : {textContent: ''}
    };
    vm.runInNewContext(source, {
      whenSilverpeasReady: callback => callback(), document: {querySelector: () => management},
      URLSearchParams, window: {location: {reload() {}}},
      XMLHttpRequest: class {
        open() {} setRequestHeader() {}
        send(body) { sent = new URLSearchParams(body); this.status = 200; this.onload(); }
      }
    });
    const grip = {closest: () => rows[0]};
    const gripTarget = {closest: () => grip};
    if (keyboard) {
      await handlers.keydown({target: gripTarget, key: 'ArrowDown', preventDefault() {}});
    } else {
      handlers.dragstart({target: gripTarget, dataTransfer: {setData() {}}});
      const foreign = rows[2];
      await handlers.drop({target: {closest: () => foreign}, preventDefault() {}});
      assert.equal(sent, undefined, 'cross-column drop is ignored');
      const target = rows[1];
      await handlers.drop({target: {closest: () => target}, preventDefault() {}});
    }
    assert.deepEqual(sent.getAll('widgets'), ['b', 'a', 'c']);
    assert.deepEqual(sent.getAll('revisions'), ['b-rev', 'a-rev', 'c-rev']);
    assert.equal(sent.get('owner'), 'WA1');
  }
});
