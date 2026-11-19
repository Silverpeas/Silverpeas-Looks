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

function setup(responseStatus = 200) {
  let click;
  let escape;
  const state = {calls: 0, reloads: 0};
  const status = {textContent: ''};
  const dialog = {
    open: false, showModal() { this.open = true; }, close() { this.open = false; },
    addEventListener: (name, callback) => { escape = callback; }
  };
  const management = {
    dataset: {endpoint: '/sp/AuroraHomepageContent', csrf: 'token'},
    contains: () => true,
    addEventListener: (name, callback) => { click = callback; },
    querySelector: selector => selector === '.homepage-delete-dialog' ? dialog : status
  };
  vm.runInNewContext(source, {
    whenSilverpeasReady: callback => callback(),
    document: {querySelector: () => management}, URLSearchParams,
    window: {location: {reload: () => { state.reloads++; }}},
    fetch: async (url, options) => {
      state.calls++;
      assert.equal(url, management.dataset.endpoint);
      assert.equal(options.method, 'POST');
      assert.equal(options.headers['X-Aurora-CSRF'], 'token');
      assert.equal(options.body.get('action'), 'deleteWidget');
      assert.equal(options.body.get('widget'), 'weather-legacy');
      assert.equal(options.body.get('revision'), 'abc');
      if (responseStatus === 'network') { throw Error('offline'); }
      return {ok: responseStatus === 200, status: responseStatus};
    }
  });
  // Created after initialization, as with rows replaced by AJAX pagination.
  const button = {
    dataset: {widget: 'weather-legacy', revision: 'abc', kind: 'skins', owner: 'waves', language: 'fr'},
    matches: selector => selector === '.homepage-delete-widget'
  };
  const event = target => ({target: {closest: () => target}, preventDefault() {}});
  return {
    state, status, dialog, button,
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

for (const failure of [409, 403, 'network']) {
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
