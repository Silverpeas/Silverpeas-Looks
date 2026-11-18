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
whenSilverpeasReady(() => {
  const loadForecast = (city, render, failure) => {
    sp.ajaxRequest(window.webContext + '/RWeatherService/').withParam('city', city).send()
      .then(response => {
        const translator = spWeather.translators[response.getResponseHeader('X-WeatherService')];
        if (!translator) { throw new Error('Unsupported weather service'); }
        return translator.translate(response.responseAsJson());
      }).then(render).catch(failure);
  };
  document.querySelectorAll('.homepage-weather').forEach(widget => {
    const buttons = [...widget.querySelectorAll('[data-city]')];
    const days = [...widget.querySelectorAll('.homepage-weather-day')];
    const status = widget.querySelector('.homepage-weather-status');
    const cookie = 'Silverpeas_Intranet_LastVisitedCity_' + widget.dataset.widgetId;
    let generation = 0;
    const load = button => {
      const current = ++generation;
      buttons.forEach(item => item.setAttribute('aria-pressed', String(item === button)));
      days.forEach(day => { day.hidden = true; });
      status.textContent = '';
      jQuery.cookie(cookie, button.dataset.city, {secure: window.location.protocol === 'https:'});
      const failed = () => {
        if (current === generation) { status.textContent = widget.dataset.unavailable; }
      };
      try {
        loadForecast(button.dataset.city, weather => {
          if (current !== generation) { return; }
          try {
            const forecasts = weather.forecasts;
            if (!forecasts || !forecasts.length) { failed(); return; }
            const hourly = (weather.api || '').includes('hourly');
            const firstDate = forecasts[0].date.format('YYYY-MM-DD');
            const tomorrow = hourly ? forecasts.findIndex((forecast, index) => index > 0 &&
              forecast.date.format('YYYY-MM-DD') !== firstDate && forecast.date.hours() >= 8) : 1;
            [0, tomorrow].forEach((index, dayIndex) => {
              const forecast = forecasts[index];
              if (!forecast) { return; }
              const day = days[dayIndex];
              day.querySelector('.min').textContent = 'min ' + forecast.temperature.min + '°';
              day.querySelector('.max').textContent = 'max ' + forecast.temperature.max + '°';
              const image = day.querySelector('img');
              image.alt = forecast.description || '';
              image.src = forecast.icon;
              day.querySelector('.hour').textContent = hourly ? ' ' + forecast.date.hours() + 'H' : '';
              day.hidden = false;
            });
          } catch (error) { failed(); }
        }, failed);
      } catch (error) { failed(); }
    };
    buttons.forEach(button => button.addEventListener('click', () => load(button)));
    const saved = jQuery.cookie(cookie) || (widget.dataset.widgetId === 'weather-legacy'
      ? jQuery.cookie('Silverpeas_Intranet_LastVisitedCity') : null);
    const first = buttons.find(button => button.dataset.city === saved) || buttons[0];
    if (first) { load(first); }
  });
});
