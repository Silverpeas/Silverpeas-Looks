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
window.addEventListener('DOMContentLoaded', function() {
  document.querySelectorAll('.homepage-search-frame').forEach(function(frame) {
    let observer;
    const resize = function() {
      try {
        const body = frame.contentDocument.body;
        if (!body) { return; }
        frame.height = Math.max(80, Math.ceil(body.getBoundingClientRect().height)) + 16;
      } catch (error) {
        if (observer) { observer.disconnect(); }
      }
    };
    const connect = function() {
      if (observer) { observer.disconnect(); }
      try {
        const body = frame.contentDocument.body;
        if (!body) { return; }
        observer = new ResizeObserver(resize);
        observer.observe(body);
        resize();
      } catch (error) { /* The search result may navigate outside this document. */ }
    };
    frame.addEventListener('load', connect);
    if (frame.contentDocument && frame.contentDocument.readyState === 'complete') { connect(); }
  });
});
