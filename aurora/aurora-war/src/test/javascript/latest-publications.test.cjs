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
const path = require('node:path');
const source = fs.readFileSync(path.resolve(__dirname, '../../main/webapp/look/jsp/js/silverpeas-latest-publications.js'), 'utf8');
function block(dataset, editing = false) {
  return {dataset, innerHTML: '', closest: () => editing, setAttribute() {}, removeAttribute() {}};
}
function harness(nodes, response = {ok: true, redirected: false, headers: {get: () => 'text/html'}, text: async () => '<ul>publications</ul>'}, environment = {}) {
  const requests = [];
  let mutation;
  const document = {
    currentScript: {src: 'https://host/silverpeas/look/jsp/js/silverpeas-latest-publications.js'},
    baseURI: 'https://host/silverpeas/Welcome',
    readyState: 'complete', documentElement: {}, body: {}, addEventListener() {},
    querySelectorAll(selector) { return selector === 'iframe' ? [] : nodes; }
  };
  if (Object.hasOwn(environment, 'currentScript')) document.currentScript = environment.currentScript;
  const context = vm.createContext({document, URL, URLSearchParams, Symbol,
    ...environment,
    console: {warn() {}},
    fetch: async (url, options) => {requests.push({url, options}); return response;},
    MutationObserver: class {constructor(callback) {mutation = callback;} observe() {}}
  });
  vm.runInContext(source, context);
  return {requests, context, mutate: () => mutation([{addedNodes: []}])};
}
const settle = () => new Promise(resolve => setImmediate(resolve));
test('loads independent blocks using the exact target options and fills the containers', async () => {
  const nodes = [block({spaceId: '123', limit: '5', includeSubspaces: 'false'}),
    block({spaceId: '456', limit: '9', includeSubspaces: 'true'})];
  const {requests, context} = harness(nodes);
  await settle();
  assert.equal(requests.length, 2);
  assert.equal(requests[0].url.pathname, '/silverpeas/LatestPublications');
  assert.equal(requests[0].url.searchParams.get('includeSubspaces'), 'false');
  assert.equal(requests[1].url.searchParams.get('spaceId'), '456');
  assert.equal(requests[0].options.credentials, 'same-origin');
  assert.equal(nodes[0].innerHTML, '<ul>publications</ul>');
  assert.equal(nodes[0].dataset.publicationsState, 'loaded');
  // Loading the script again from the parent layout must not fetch twice.
  vm.runInContext(source, context);
  await settle();
  assert.equal(requests.length, 2);
});
test('does not fetch for editor previews or invalid options', async () => {
  const nodes = [block({spaceId: '123', limit: '5', includeSubspaces: 'false'}, true),
    block({spaceId: '123', limit: '-1', includeSubspaces: 'false'})];
  const {requests} = harness(nodes);
  await settle();
  assert.equal(requests.length, 0);
  assert.equal(nodes[1].dataset.publicationsState, 'error');
});
test('rejects a login redirect without injecting its HTML', async () => {
  const node = block({spaceId: '123', limit: '5', includeSubspaces: 'false'});
  harness([node], {ok: true, redirected: true, headers: {get: () => 'text/html'}});
  await settle();
  assert.equal(node.dataset.publicationsState, 'error');
  assert.equal(node.innerHTML, '');
});
test('observes newly inserted content', async () => {
  const {context, requests} = harness([]);
  const node = block({spaceId: '1', limit: '2', includeSubspaces: 'true'});
  node.nodeType = 1;
  node.matches = selector => selector === '.silverpeas-latest-publications';
  node.querySelectorAll = () => [];
  // A second harness captures the observer callback on its own document.
  let callback;
  context.MutationObserver = class {constructor(fn) {callback = fn;} observe() {}};
  delete context.document[Symbol.for('silverpeas.latestPublications.observed')];
  vm.runInContext(source, context);
  callback([{addedNodes: [node]}]);
  await settle();
  assert.equal(requests.length, 1);
  assert.equal(node.dataset.publicationsState, 'loaded');
});

test('loads from AJAX fragments without an external currentScript URL', async () => {
  for (const currentScript of [null, {src: ''}]) {
    const node = block({spaceId: '123', limit: '5', includeSubspaces: 'false'});
    const {requests} = harness([node], undefined, {currentScript, webContext: '/custom-context'});
    await settle();
    assert.equal(requests[0].url.pathname, '/custom-context/LatestPublications');
    assert.equal(node.dataset.publicationsState, 'loaded');
  }
});
