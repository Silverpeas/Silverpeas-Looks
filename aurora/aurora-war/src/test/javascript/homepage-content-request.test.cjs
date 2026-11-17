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
const vm = require('node:vm');
const fs = require('node:fs');
const path = require('node:path');
const source = fs.readFileSync(path.resolve(__dirname,
    '../../main/webapp/look/jsp/js/homepage-content.js'), 'utf8');
function setup(status = 200, response = '{"url":"image.png"}') {
  let request;
  class PlatformXHR {
    constructor() { request = this; this.headers = {}; }
    open(method, url) { this.method = method; this.url = url; }
    setRequestHeader(name, value) { this.headers[name] = value; }
    send(body) {
      // Simulate the platform's XMLHttpRequest send interceptor.
      if (this.url.startsWith('/silverpeas')) this.setRequestHeader('X-STKN', 'platform-token');
      this.body = body;
      this.status = status;
      this.responseText = response;
      this.onload();
    }
  }
  const context = {XMLHttpRequest: PlatformXHR, URLSearchParams,
    window: {addEventListener() {}}};
  vm.createContext(context);
  vm.runInContext(source, context);
  return {send: context.homepageContentRequest, request: () => request};
}
test('image upload uses the platform interceptor and preserves multipart body', async () => {
  const api = setup();
  const multipart = {image: 'binary payload'};
  const result = await api.send(new URL('http://localhost:8000/silverpeas/AuroraHomepageContent?action=upload'),
      {method: 'POST', headers: {'X-Aurora-CSRF': 'widget-token'}, body: multipart});
  const xhr = api.request();
  assert.equal(xhr.url, '/silverpeas/AuroraHomepageContent?action=upload');
  assert.equal(xhr.headers['X-STKN'], 'platform-token');
  assert.equal(xhr.headers['X-Aurora-CSRF'], 'widget-token');
  assert.equal(xhr.headers['Content-Type'], undefined);
  assert.equal(xhr.body, multipart);
  assert.equal(result.url, 'image.png');
});
test('save sends an encoded form with both CSRF protections', async () => {
  const api = setup();
  await api.send(new URL('http://localhost/silverpeas/AuroraHomepageContent?action=save'),
      {method: 'POST', headers: {'X-Aurora-CSRF': 'widget-token'},
        body: new URLSearchParams({title: 'Été & hiver', html: '<p>Bonjour</p>'})});
  assert.match(api.request().headers['Content-Type'], /application\/x-www-form-urlencoded/);
  assert.equal(new URLSearchParams(api.request().body).get('title'), 'Été & hiver');
  assert.equal(api.request().headers['X-STKN'], 'platform-token');
});
test('conflicts and forbidden requests remain failures', async () => {
  for (const status of [403, 409]) {
    const api = setup(status);
    await assert.rejects(api.send(new URL('http://localhost/silverpeas/AuroraHomepageContent')));
  }
});
test('an expired session returning HTML is reported as an invalid response', async () => {
  const api = setup(200, '<html>Login</html>');
  await assert.rejects(api.send(new URL('http://localhost/silverpeas/AuroraHomepageContent')),
      /Vérifiez votre session/);
});

test('editor loads placement and submits it with the content revision', async () => {
  const elements = new Map();
  const editorJsp = fs.readFileSync(path.resolve(__dirname,
      '../../main/webapp/WEB-INF/homepage/contentEditor.jsp'), 'utf8');
  const ids = new Set([...editorJsp.matchAll(/id="([^"]+)"/g)].map(match => match[1]));
  ids.add('main');
  function element(id) {
    assert.ok(ids.has(id), `Missing editor control: ${id}`);
    if (!elements.has(id)) elements.set(id, {value: '', disabled: false,
      dataset: {language: 'fr'}, handlers: {}, addEventListener(name, callback) {
        this.handlers[name] = callback;
      }});
    return elements.get(id);
  }
  const requests = [];
  let ready;
  const editor = {on(name, callback) { callback(); }, getData() { return '<p>Modifié</p>'; },
    setData() {}};
  const context = {URL, URLSearchParams, document: {getElementById: element,
    querySelector() { return element('main'); }},
    CKEDITOR: {replace() { return editor; }},
    window: {location: {search: '?kind=skins&owner=Aurora&widget=test',
      href: 'http://localhost/silverpeas/AuroraHomepageContent?kind=skins&owner=Aurora&widget=test'},
      addEventListener(name, callback) { ready = callback; }}};
  vm.createContext(context);
  vm.runInContext(source, context);
  context.homepageContentRequest = async (url, options) => {
    requests.push({url, options});
    return {title: 'Titre', html: '<p>Original</p>', revision: 'revision-1', csrf: 'token',
      zone: 'right', order: 25, enabled: false};
  };
  await ready();
  assert.equal(element('homepage-zone').value, 'right');
  assert.equal(element('homepage-order').value, 25);
  assert.equal(element('homepage-enabled').checked, false);
  assert.equal(element('homepage-manage').href.searchParams.get('action'), 'manage');
  assert.equal(requests[0].url.searchParams.get('language'), 'fr');
  element('homepage-zone').value = 'main';
  element('homepage-order').value = '12';
  element('homepage-enabled').checked = true;
  await element('homepage-content-form').handlers.submit({preventDefault() {}});
  const save = requests[1];
  assert.equal(save.url.searchParams.get('action'), 'save');
  assert.equal(save.options.body.get('zone'), 'main');
  assert.equal(save.options.body.get('order'), '12');
  assert.equal(save.options.body.get('enabled'), 'true');
  assert.equal(save.options.body.get('revision'), 'revision-1');
  assert.equal(save.options.body.get('html'), '<p>Modifié</p>');
});

test('homepage keeps asynchronous legacy blocks and navigation', () => {
  const loads = [];
  let link;
  const context = {whenSilverpeasReady(callback) { callback(); },
    sp: {ajaxRequest(url) { return {loadTarget(target) { loads.push([url, target]); }}; }},
    StringUtil: {isDefined: value => Boolean(value)},
    spWindow: {loadLink(url) { link = url; }}, webContext: '/silverpeas'};
  vm.createContext(context);
  vm.runInContext(fs.readFileSync(path.resolve(__dirname,
      '../../main/webapp/look/jsp/js/homepage-main.js'), 'utf8'), context);
  assert.deepEqual(loads, [['parts/MainNextEventsPart.jsp', '#next-event-part'],
    ['parts/MainRSSPart.jsp', '#rss-part']]);
  context.changeBody('/example');
  assert.equal(link, '/silverpeas/example');
});
