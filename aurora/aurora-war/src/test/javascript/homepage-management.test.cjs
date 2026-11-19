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

for (const scenario of ['cancel', 'success', 'conflict', 'network']) {
  test(scenario, async () => {
    let click;
    let calls = 0;
    let reloads = 0;
    const status = {textContent: ''};
    const button = {
      dataset: {widget: 'weather-legacy', revision: 'abc', kind: 'skins', owner: 'waves', language: 'fr'},
      addEventListener: (name, callback) => { click = callback; }
    };
    const management = {
      dataset: {endpoint: '/sp/AuroraHomepageContent', csrf: 'token'},
      querySelectorAll: () => [button], querySelector: () => status
    };
    vm.runInNewContext(source, {
      whenSilverpeasReady: callback => callback(),
      document: {querySelector: () => management}, URLSearchParams,
      window: {
        confirm: () => scenario !== 'cancel', location: {reload: () => { reloads++; }}
      },
      fetch: async (url, options) => {
        calls++;
        assert.equal(url, management.dataset.endpoint);
        assert.equal(options.method, 'POST');
        assert.equal(options.headers['X-Aurora-CSRF'], 'token');
        assert.equal(options.body.get('action'), 'deleteWidget');
        for (const [key, value] of Object.entries(button.dataset)) {
          assert.equal(options.body.get(key), value);
        }
        if (scenario === 'network') { throw Error('offline'); }
        return {ok: scenario === 'success', status: 409};
      }
    });
    await click();
    assert.equal(calls, scenario === 'cancel' ? 0 : 1);
    assert.equal(reloads, scenario === 'success' ? 1 : 0);
    if (['conflict', 'network'].includes(scenario)) {
      assert.ok(status.textContent);
      assert.equal(button.disabled, false);
    }
  });
}
