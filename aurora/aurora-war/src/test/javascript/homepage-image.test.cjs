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
const path = require('node:path');
const vm = require('node:vm');
const source = fs.readFileSync(path.resolve(__dirname, '../../main/webapp/look/jsp/js/homepage-image.js'), 'utf8');
const markup = fs.readFileSync(path.resolve(__dirname, '../../main/webapp/WEB-INF/homepage/imageEditor.jsp'), 'utf8');
const name = '12345678-1234-1234-1234-123456789abc.png';

async function editor({image = '', loadStatus = 200, uploadStatus = 200, saveStatus = 200, kind = 'spaces'} = {}) {
  const controls = {};
  for (const [, id] of markup.matchAll(/id="([^"]+)"/g)) {
    controls[id] = {value: '', checked: false, disabled: ['news-save', 'image-file'].includes(id), files: [], handlers: {},
      type: id === 'news-enabled' ? 'checkbox' : 'text',
      addEventListener(event, callback) { this.handlers[event] = callback; }};
  }
  const calls = [];
  const location = {href: `http://localhost/silverpeas/AuroraHomepageContent?action=imageEditor&kind=${kind}&owner=WA1&widget=image-one&language=fr`};
  class FormData {
    constructor() { this.entries = {}; }
    append(key, value) { this.entries[key] = value; }
  }
  class XHR {
    constructor() { this.headers = {}; }
    open(method, url) { this.method = method; this.url = url; }
    setRequestHeader(key, value) { this.headers[key] = value; }
    send(body) {
      this.body = body;
      calls.push(this);
      const action = new URL(this.url, location.href).searchParams.get('action');
      this.status = action === 'loadImage' ? loadStatus : action === 'upload' ? uploadStatus : saveStatus;
      this.responseText = JSON.stringify(action === 'upload'
        ? {url: `/silverpeas/AuroraHomepageContent?action=asset&name=${name}`}
        : {image, alt: 'Un paysage', url: '/docs', target: '_blank', title: 'Illustration',
          zone: 'right', order: 4, enabled: true, revision: 'r1', csrf: 'token'});
      this.onload();
    }
  }
  let start;
  vm.runInNewContext(source, {URL, URLSearchParams, FormData, XMLHttpRequest: XHR,
    document: {getElementById: id => controls[id]},
    window: {location, addEventListener(event, callback) { start = callback; }}});
  await start();
  return {controls, calls, location, upload: async (size = 200) => {
    controls['image-file'].files = [{size, type: 'image/png'}];
    await controls['image-file'].handlers.change();
  }, submit: () => controls['news-form'].handlers.submit({preventDefault() {}})};
}

test('new image requires upload then saves scoped filename, layout, link, CSRF and revision and closes editor', async () => {
  const e = await editor();
  assert.equal(e.controls['news-save'].disabled, true);
  await e.submit();
  assert.equal(e.calls.length, 1);
  await e.upload();
  assert.equal(e.controls['image-name'].value, name);
  assert.equal(e.controls['news-save'].disabled, false);
  assert.match(e.controls['image-preview'].src, /kind=spaces&owner=WA1&widget=image-one/);
  assert.match(e.controls['image-preview'].src, /action=asset/);
  assert.equal(e.calls[1].headers['X-Aurora-CSRF'], 'token');
  assert.equal(e.calls[1].body.entries.image.size, 200);
  e.controls['news-enabled'].checked = false;
  await e.submit();
  const saved = e.calls.at(-1);
  const body = new URLSearchParams(saved.body);
  assert.match(saved.url, /action=saveImage/);
  assert.equal(saved.headers['X-Aurora-CSRF'], 'token');
  assert.equal(body.get('image'), name);
  assert.equal(body.get('alt'), 'Un paysage');
  assert.equal(body.get('target'), '_blank');
  assert.equal(body.get('zone'), 'right');
  assert.equal(body.get('enabled'), 'false');
  assert.equal(body.get('revision'), 'r1');
  assert.match(e.location.href, /action=manage/);
  assert.match(e.location.href, /kind=spaces/);
});

test('existing image can be edited on general homepage without uploading again', async () => {
  const e = await editor({image: name, kind: 'skins'});
  assert.equal(e.controls['image-preview'].hidden, false);
  assert.equal(e.controls['news-save'].disabled, false);
  await e.submit();
  assert.equal(e.calls.length, 2);
  assert.match(e.location.href, /kind=skins/);
});

test('oversize image is rejected locally and failed replacement keeps previous image', async () => {
  const e = await editor({image: name, uploadStatus: 413});
  await e.upload(5242881);
  assert.equal(e.calls.length, 1);
  assert.match(e.controls['news-status'].textContent, /5 Mo/);
  await e.upload();
  assert.equal(e.controls['image-name'].value, name);
  assert.equal(e.controls['news-save'].disabled, false);
  assert.match(e.controls['news-status'].textContent, /413/);
});

test('stale revision leaves form open and load failure prevents writes', async () => {
  const stale = await editor({image: name, saveStatus: 409});
  await stale.submit();
  assert.match(stale.controls['news-status'].textContent, /modifié ailleurs/);
  assert.match(stale.location.href, /action=imageEditor/);
  const failed = await editor({loadStatus: 403});
  await failed.upload();
  await failed.submit();
  assert.equal(failed.calls.length, 1);
  assert.equal(failed.controls['news-save'].disabled, true);
  assert.equal(failed.controls['image-file'].disabled, true);
});
