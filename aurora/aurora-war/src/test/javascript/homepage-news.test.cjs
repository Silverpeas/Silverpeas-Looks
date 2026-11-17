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
  '../../main/webapp/look/jsp/js/homepage-news.js'), 'utf8');
const jsp = fs.readFileSync(path.resolve(__dirname,
  '../../main/webapp/WEB-INF/homepage/newsEditor.jsp'), 'utf8');

async function editor(status = 200, configured = false) {
  const controls = {};
  for (const match of jsp.matchAll(/id="([^"]+)"/g)) {
    controls[match[1]] = {value: '', checked: false, disabled: false, handlers: {},
      type: ['news-important', 'news-enabled'].includes(match[1]) ? 'checkbox' : 'text',
      addEventListener(event, callback) { this.handlers[event] = callback; }};
  }
  const calls = [];
  let start;
  const data = {title: 'Infos', zone: 'right', order: 7, source: 'quickinfo12', size: 5, importantOnly: true,
    enabled: true, rendering: 'LIST', revision: 'first', csrf: 'token', configured};
  class XHR {
    constructor() { this.headers = {}; }
    open(method, url) { this.method = method; this.url = url; }
    setRequestHeader(key, value) { this.headers[key] = value; }
    send(body) {
      this.body = body;
      calls.push(this);
      this.status = this.method === 'POST' ? status : 200;
      this.responseText = JSON.stringify({...data, revision: this.method === 'POST' ? 'second' : 'first'});
      this.onload();
    }
  }
  vm.runInNewContext(source, {URL, URLSearchParams, XMLHttpRequest: XHR,
    document: {getElementById(id) { assert.ok(controls[id], id); return controls[id]; }},
    window: {location: {href: 'http://localhost/silverpeas/AuroraHomepageContent?action=newsEditor&kind=skins&owner=waves&widget=news-fourth',
      search: '?action=newsEditor&kind=skins&owner=waves&widget=news-fourth'},
      addEventListener(event, callback) { start = callback; }}});
  await start();
  return {controls, calls, submit: () => controls['news-form'].handlers.submit({preventDefault() {}})};
}

test('loads shared widget settings and saves application choices through session XHR with CSRF and revision', async () => {
  const {controls, calls, submit} = await editor();
  assert.equal(controls['news-source'].value, 'applications');
  assert.equal(controls['news-applications'].value, 'quickinfo12');
  assert.equal(controls['news-important'].checked, true);
  assert.equal(controls['news-applications-field'].hidden, false);
  assert.match(controls['news-status'].textContent, /après enregistrement/);
  controls['news-applications'].value = 'quickinfo12  quickinfo34';
  await submit();
  const saved = calls.at(-1);
  assert.equal(saved.headers['X-Aurora-CSRF'], 'token');
  assert.ok(saved.url.startsWith('/silverpeas/'));
  assert.match(saved.url, /widget=news-fourth/);
  const body = new URLSearchParams(saved.body);
  assert.equal(body.get('source'), 'quickinfo12 quickinfo34');
  assert.equal(body.get('revision'), 'first');
  assert.equal(body.get('rendering'), 'LIST');
  assert.equal(body.get('title'), 'Infos');
  assert.equal(body.get('zone'), 'right');
  assert.equal(body.get('order'), '7');
  await submit();
  assert.equal(new URLSearchParams(calls.at(-1).body).get('revision'), 'second');
});

test('hides application input when selecting all sources', async () => {
  const {controls, calls, submit} = await editor(200, true);
  controls['news-source'].value = '*';
  controls['news-source'].handlers.change();
  assert.equal(controls['news-applications-field'].hidden, true);
  assert.equal(controls['news-applications'].required, false);
  controls['news-enabled'].checked = false;
  await submit();
  const body = new URLSearchParams(calls.at(-1).body);
  assert.equal(body.get('source'), '*');
  assert.equal(body.get('enabled'), 'false');
});

test('reports conflict and keeps original revision', async () => {
  const {controls, calls, submit} = await editor(409);
  await submit();
  assert.match(controls['news-status'].textContent, /modifié ailleurs/);
  assert.equal(controls['news-save'].disabled, false);
  await submit();
  assert.equal(new URLSearchParams(calls.at(-1).body).get('revision'), 'first');
});


test('multiple carousels initialize once each after DOM readiness', () => {
  const source = fs.readFileSync(path.resolve(__dirname,
    '../../main/webapp/look/jsp/js/homepage-news-display.js'), 'utf8');
  const sliders = [0, 1].map(() => ({values: {}, calls: 0,
    data(key, value) { if (value !== undefined) this.values[key] = value; return this.values[key]; },
    responsiveSlides(options) { assert.equal(options.pager, true); this.calls++; }}));
  const pending = [];
  const jquery = target => {
    if (typeof target === 'function') { pending.push(target); return; }
    if (typeof target !== 'string') return target;
    assert.equal(target, '.homepage-news-carousel .rslides');
    return {each(callback) { sliders.forEach(slider => callback.call(slider)); }};
  };
  const context = {whenSilverpeasReady: callback => callback(), $: jquery};
  vm.runInNewContext(source, context);
  vm.runInNewContext(source, context);
  assert.equal(sliders[0].calls, 0);
  pending.forEach(callback => callback());
  assert.deepEqual(sliders.map(slider => slider.calls), [1, 1]);
});
