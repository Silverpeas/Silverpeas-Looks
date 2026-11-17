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
  '../../main/webapp/look/jsp/js/homepage-feeds.js'), 'utf8');

function collection(values) {
  return {
    length: values.length,
    first() { return collection(values.slice(0, 1)); },
    on(event, handler) { values.forEach(value => { value.handler = handler; }); return this; },
    addClass(name) { values.forEach(value => value.classes.add(name)); return this; },
    removeClass(name) { values.forEach(value => value.classes.delete(name)); return this; },
    toggleClass(name) { values.forEach(value => value.classes.has(name) ? value.classes.delete(name) : value.classes.add(name)); return this; },
    attr(name, value) { if (value === undefined) { return values[0]?.[name]; } return this; },
    prop(name, value) { if (value === undefined) { return values[0]?.[name]; } values.forEach(item => { item[name] = value; }); return this; },
    hide() { values.forEach(value => { value.visible = false; }); return this; },
    show() { values.forEach(value => { value.visible = true; }); return this; },
    toggle() { values.forEach(value => { value.visible = !value.visible; }); return this; },
    filter(selector) { return collection(values.filter(value => value.classes.has(selector.slice(1)))); },
    children() { return collection(values.map(value => value.description)); },
    closest() { return {length: values[0]?.isLink ? 1 : 0}; },
    responsiveSlides() { values.forEach(value => { value.calls++; }); return this; }
  };
}
function widget(mode) {
  return {dataset: {}, mode,
    slider: {calls: 0},
    links: [1, 2].map(id => ({'data-channel': 'channel_' + id, classes: new Set()})),
    items: [1, 2].map(id => ({classes: new Set(['channel_' + id]), visible: true, description: {hidden: true}}))};
}
function environment(widgets) {
  let mutated;
  let ready;
  let platformReady;
  const context = {window: {}, document: {body: {}, querySelectorAll: () => widgets},
    whenSilverpeasReady(callback) { platformReady = callback; },
    MutationObserver: class {constructor(callback) { mutated = callback; } observe() {}},
    $(value) {
      if (typeof value === 'function') { ready = value; return; }
      if (value.mode) { return {
        hasClass: mode => value.mode === mode,
        find(selector) {
          if (selector === '.homepage-rss-slider') { return collection([value.slider]); }
          if (selector === '.rss_type > a') { return collection(value.links); }
          if (['.item-channel', '.deploy-item'].includes(selector)) { return collection(value.items); }
          return collection([]);
        }
      }; }
      return collection([value]);
    }};
  vm.runInNewContext(source, context);
  return {start() { platformReady(); ready(); }, mutate() { mutated(); }, context};
}

test('RSS tabs and aggregate filters stay inside their own widget after asynchronous insertion', () => {
  const first = widget('rss_tab');
  const second = widget('aggregate');
  const widgets = [];
  const env = environment(widgets);
  env.start();
  widgets.push(first, second);
  env.mutate();
  assert.deepEqual(first.items.map(item => item.visible), [true, false]);
  assert.deepEqual(second.items.map(item => item.visible), [true, true]);
  first.links[1].handler.call(first.links[1], {preventDefault() {}});
  assert.deepEqual(first.items.map(item => item.visible), [false, true]);
  assert.deepEqual(second.items.map(item => item.visible), [true, true]);
  second.links[0].handler.call(second.links[0], {preventDefault() {}});
  assert.deepEqual(second.items.map(item => item.visible), [false, true]);
  assert.deepEqual(first.items.map(item => item.visible), [false, true]);
  first.items[0].handler.call(first.items[0], {target: {isLink: false}});
  assert.equal(first.items[0].description.hidden, false);
  first.items[0].handler.call(first.items[0], {target: {isLink: true}});
  assert.equal(first.items[0].description.hidden, false);
});

test('each RSS carousel initializes once and reloading the script does not install another observer', () => {
  const widgets = [widget('rss_carrousel'), widget('rss_carrousel')];
  const env = environment(widgets);
  env.start();
  env.mutate();
  vm.runInNewContext(source, env.context);
  env.mutate();
  assert.deepEqual(widgets.map(widget => widget.slider.calls), [1, 1]);
});
