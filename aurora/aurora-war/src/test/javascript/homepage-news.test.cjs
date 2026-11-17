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

async function editor(status = 200, configured = false, publications = false, ephemeris = false, feedType = '') {
  const controls = {};
  const markup = feedType ? fs.readFileSync(path.resolve(__dirname,
    '../../main/webapp/WEB-INF/homepage/' + feedType + 'Editor.jsp'), 'utf8') : ephemeris ? fs.readFileSync(path.resolve(__dirname,
    '../../main/webapp/WEB-INF/homepage/ephemerisEditor.jsp'), 'utf8') : publications ? fs.readFileSync(path.resolve(__dirname,
    '../../main/webapp/WEB-INF/homepage/publicationsEditor.jsp'), 'utf8') : jsp;
  for (const match of markup.matchAll(/id="([^"]+)"/g)) {
    controls[match[1]] = {value: '', checked: false, disabled: false, handlers: {},
      dataset: {widgetType: feedType || (ephemeris ? 'ephemeris' : '')},
      type: ['news-important', 'news-enabled', 'news-show-more', 'news-show-all', 'news-personal', 'news-include-today'].includes(match[1]) ? 'checkbox' : 'text',
      addEventListener(event, callback) { this.handlers[event] = callback; }};
  }
  const calls = [];
  let start;
  const data = {maxDays: 4, personal: true, includeToday: false, title: 'Infos', zone: 'right', order: 7, source: 'quickinfo12', size: 5, importantOnly: true,
    showMoreNews: true, showAllNews: false, spaceId: 'WA12', showMore: true, enabled: true, rendering: 'LIST', revision: 'first', csrf: 'token', configured};
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
    document: {getElementById(id) { return controls[id] || null; }},
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

test('news buttons are independent and persist explicit hidden values', async () => {
  const {controls, calls, submit} = await editor();
  assert.equal(controls['news-show-more'].checked, true);
  assert.equal(controls['news-show-all'].checked, false);
  controls['news-show-more'].checked = false;
  controls['news-show-all'].checked = true;
  await submit();
  const body = new URLSearchParams(calls.at(-1).body);
  assert.equal(body.get('showMoreNews'), 'false');
  assert.equal(body.get('showAllNews'), 'true');
});

test('publications use the shared editor transport and placement with their own settings', async () => {
  const {controls, calls, submit} = await editor(200, true, true);
  assert.match(calls[0].url, /action=loadPublications/);
  assert.equal(controls['news-space'].value, 'WA12');
  assert.equal(controls['news-zone'].value, 'right');
  controls['news-space'].value = '';
  controls['news-size'].value = 8;
  controls['news-show-more'].checked = false;
  await submit();
  assert.match(calls.at(-1).url, /action=savePublications/);
  const body = new URLSearchParams(calls.at(-1).body);
  assert.equal(body.get('spaceId'), '');
  assert.equal(body.get('size'), '8');
  assert.equal(body.get('showMore'), 'false');
  assert.equal(body.get('zone'), 'right');
  assert.equal(body.get('revision'), 'first');
});

test('ephemeris editor saves common placement and visibility through protected transport', async () => {
  const {controls, calls, submit} = await editor(200, false, false, true);
  assert.match(calls[0].url, /action=loadEphemeris/);
  controls['news-title'].value = 'Aujourd’hui';
  controls['news-zone'].value = 'main';
  controls['news-order'].value = 18;
  controls['news-enabled'].checked = false;
  await submit();
  const saved = calls.at(-1);
  assert.match(saved.url, /action=saveEphemeris/);
  assert.equal(saved.headers['X-Aurora-CSRF'], 'token');
  const body = new URLSearchParams(saved.body);
  assert.equal(body.get('title'), 'Aujourd’hui');
  assert.equal(body.get('zone'), 'main');
  assert.equal(body.get('order'), '18');
  assert.equal(body.get('enabled'), 'false');
  assert.equal(body.get('revision'), 'first');
  assert.equal(body.has('source'), false);
});

for (const type of ['events', 'rss']) {
  test(type + ' editor saves source, placement and specific settings with CSRF and revision', async () => {
    const {controls, calls, submit} = await editor(200, true, false, false, type);
    assert.equal(controls['news-save'].disabled, false);
    controls['news-source'].value = type === 'events' ? ' almanach12   almanach34 ' : ' rssaggregator12 ';
    controls['news-zone'].value = 'main';
    controls['news-enabled'].checked = false;
    if (type === 'events') {
      assert.equal(controls['news-personal'].checked, true);
      assert.equal(controls['news-include-today'].checked, false);
      controls['news-max-days'].value = 5;
    } else { controls['news-rendering'].value = 'tabs'; }
    await submit();
    const saved = calls.at(-1);
    assert.match(saved.url, new RegExp('action=save' + (type === 'events' ? 'Events' : 'Rss')));
    assert.equal(saved.headers['X-Aurora-CSRF'], 'token');
    const body = new URLSearchParams(saved.body);
    assert.equal(body.get('revision'), 'first');
    assert.equal(body.get('zone'), 'main');
    assert.equal(body.get('enabled'), 'false');
    assert.equal(body.get('source'), type === 'events' ? 'almanach12 almanach34' : 'rssaggregator12');
    if (type === 'events') {
      assert.equal(body.get('maxDays'), '5');
      assert.equal(body.get('personal'), 'true');
      assert.equal(body.get('includeToday'), 'false');
    } else { assert.equal(body.get('rendering'), 'tabs'); }
  });
}
