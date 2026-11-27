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
const source = fs.readFileSync(path.resolve(__dirname, '../../main/webapp/look/jsp/js/homepage-medias.js'), 'utf8');
async function editor(kind = 'skins', fail = false) {
  const controls = {};
  const markup = fs.readFileSync(path.resolve(__dirname, '../../main/webapp/WEB-INF/homepage/mediasEditor.jsp'), 'utf8');
  for (const match of markup.matchAll(/id="([^"]+)"/g)) {
    controls[match[1]] = {value: '', checked: false, disabled: false, hidden: false, handlers: {},
      addEventListener(event, callback) { this.handlers[event] = callback; }};
  }
  const calls = [];
  const location = {href: `http://localhost/silverpeas/AuroraHomepageContent?action=mediasEditor&kind=${kind}&owner=WA1&widget=medias-1`};
  let start;
  class XHR {
    constructor() { this.headers = {}; }
    open(method, url) { this.method = method; this.url = url; }
    setRequestHeader(key, value) { this.headers[key] = value; }
    send(body) {
      calls.push({method: this.method, url: this.url, headers: this.headers, body});
      this.status = body && fail ? 409 : 200;
      this.responseText = JSON.stringify({sourceType: 'spaces', source: 'WA2', size: 6, title: 'Médias',
        zone: 'right', order: 2, enabled: true, revision: 'v1', csrf: 'token'});
      this.onload();
    }
  }
  vm.runInNewContext(source, {URL, URLSearchParams, XMLHttpRequest: XHR,
    document: {getElementById(id) { return controls[id]; }},
    window: {location, AuroraHomepageSources: {async initialize(request) {
      await request('sources', null, null, {sourceType: 'gallery'});
    }}, addEventListener(event, callback) { start = callback; }}});
  await start();
  return {controls, calls, location, submit: () => controls['news-form'].handlers.submit({preventDefault() {}})};
}
test('loads source and saves an explicitly selected gallery with CSRF on both homepage types', async () => {
  for (const kind of ['skins', 'spaces']) {
    const {controls, calls, location, submit} = await editor(kind);
    assert.equal(controls['media-spaces'].value, 'WA2');
    assert.equal(controls['media-gallery-field'].hidden, true);
    assert.match(calls[1].url, /sourceType=gallery/);
    controls['media-source-type'].value = 'gallery';
    controls['media-source-type'].handlers.change();
    assert.equal(controls['media-spaces-search'].disabled, true);
    assert.equal(controls['media-gallery-search'].disabled, false);
    controls['media-gallery'].value = 'gallery12';
    await submit();
    const saved = calls.at(-1);
    assert.equal(saved.method, 'POST');
    assert.equal(saved.headers['X-Aurora-CSRF'], 'token');
    const body = new URLSearchParams(saved.body);
    assert.equal(body.get('source'), 'gallery12');
    assert.equal(body.get('sourceType'), 'gallery');
    assert.equal(body.get('revision'), 'v1');
    assert.match(location.href, /action=manage/);
  }
});
test('requires a selection and keeps the form open on a revision conflict', async () => {
  const {controls, calls, location, submit} = await editor('spaces', true);
  controls['media-spaces'].value = '';
  await submit();
  assert.equal(calls.length, 2);
  assert.match(controls['news-status'].textContent, /Sélectionnez/);
  controls['media-spaces'].value = 'WA2';
  await submit();
  assert.match(controls['news-status'].textContent, /modifié ailleurs/);
  assert.match(location.href, /action=mediasEditor/);
  assert.equal(controls['news-save'].disabled, false);
});
