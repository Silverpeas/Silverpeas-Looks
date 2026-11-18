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
const source = fs.readFileSync(path.join(__dirname, '../../main/webapp/look/jsp/js/homepage-weather.js'), 'utf8');
function setup() {
  const requests = [];
  const cookies = {};
  const widgets = ['one', 'two'].map(id => {
    const buttons = ['1', '2'].map(city => ({dataset: {city}, setAttribute(key, value) {this[key] = value;},
      addEventListener(event, callback) {this.click = callback;}}));
    const days = [0, 1].map(() => {
      const parts = Object.fromEntries(['.min', '.max', 'img', '.hour'].map(key => [key, {}]));
      return {parts, querySelector(key) {return parts[key];}};
    });
    const status = {};
    return {buttons, days, status, dataset: {widgetId: id, unavailable: 'Indisponible'},
      querySelectorAll(selector) {return selector === '[data-city]' ? buttons : days;},
      querySelector() {return status;}};
  });
  vm.runInNewContext(source, {whenSilverpeasReady: callback => callback(),
    document: {querySelectorAll: () => widgets}, window: {webContext: '/silverpeas', location: {protocol: 'https:'}},
    jQuery: {cookie(key, value) {if (value) {cookies[key] = value;} return cookies[key];}},
    spWeather: {translators: {test: {translate: data => data}}},
    sp: {ajaxRequest(url) {
      assert.equal(url, '/silverpeas/RWeatherService/');
      return {withParam(key, city) {
        assert.equal(key, 'city');
        return {send: () => new Promise((resolve, reject) => requests.push({city,
          success: (data, service = 'test') => resolve({getResponseHeader: () => service, responseAsJson: () => data}),
          failure: () => reject(new Error('offline'))}))};
      }};
    }}});
  return {widgets, requests, cookies};
}
function forecast(date, hour, min = 1) {
  return {date: {format: () => date, hours: () => hour}, temperature: {min, max: 10},
    description: '<clouds>', icon: '/meteo.png'};
}
const settle = () => new Promise(resolve => setImmediate(resolve));
test('instances keep city selection and remembered city independent; stale replies are ignored', async () => {
  const {widgets, requests, cookies} = setup();
  widgets[0].buttons[1].click();
  requests[0].success({api: 'daily', forecasts: [forecast('2026-01-01', 0)]});
  await settle();
  assert.equal(widgets[0].days[0].hidden, true);
  requests[2].success({api: 'daily', forecasts: [forecast('2026-01-01', 0, 7)]});
  await settle();
  assert.equal(widgets[0].days[0].parts['.min'].textContent, 'min 7°');
  assert.equal(widgets[1].days[0].hidden, true);
  assert.equal(cookies.Silverpeas_Intranet_LastVisitedCity_one, '2');
  assert.equal(cookies.Silverpeas_Intranet_LastVisitedCity_two, '1');
});
test('hourly forecasts find tomorrow across year boundary and hide missing second day', async () => {
  const {widgets, requests} = setup();
  requests[0].success({api: '3-hourly', forecasts: [forecast('2026-12-31', 23), forecast('2027-01-01', 8, 4)]});
  await settle();
  assert.equal(widgets[0].days[1].hidden, false);
  assert.equal(widgets[0].days[1].parts['.min'].textContent, 'min 4°');
  assert.equal(widgets[0].days[1].parts['.hour'].textContent, ' 8H');
  widgets[0].buttons[1].click();
  requests[2].success({api: 'daily', forecasts: [forecast('2027-01-01', 0)]});
  await settle();
  assert.equal(widgets[0].days[1].hidden, true);
});
test('failed and empty forecasts display a local unavailable message and can recover', async () => {
  const {widgets, requests} = setup();
  requests[0].failure();
  await settle();
  assert.equal(widgets[0].status.textContent, 'Indisponible');
  assert.equal(widgets[1].status.textContent, '');
  widgets[0].buttons[1].click();
  assert.equal(widgets[0].status.textContent, '');
  requests[2].success({forecasts: []});
  await settle();
  assert.equal(widgets[0].status.textContent, 'Indisponible');
});

test('unsupported service and malformed translated forecasts display unavailable', async () => {
  const {widgets, requests} = setup();
  requests[0].success({}, 'missing');
  await settle();
  assert.equal(widgets[0].status.textContent, 'Indisponible');
  requests[1].success({api: 'daily', forecasts: [{}]});
  await settle();
  assert.equal(widgets[1].status.textContent, 'Indisponible');
});
