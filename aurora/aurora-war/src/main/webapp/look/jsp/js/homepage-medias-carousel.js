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
window.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('.homepage-medias[data-display="carousel"]').forEach(widget => {
    const list = widget.querySelector('.homepage-medias-list');
    const controls = widget.querySelector('.homepage-medias-controls');
    if (!list || !controls) { return; }
    const buttons = controls.querySelectorAll('button');
    const refresh = () => {
      const maximum = list.scrollWidth - list.clientWidth;
      controls.hidden = maximum <= 1;
      buttons[0].disabled = list.scrollLeft <= 1;
      buttons[1].disabled = list.scrollLeft >= maximum - 1;
    };
    buttons.forEach(button => button.addEventListener('click', () => {
      list.scrollBy({left: Number(button.dataset.direction) * list.clientWidth * .85,
        behavior: window.matchMedia('(prefers-reduced-motion: reduce)').matches ? 'instant' : 'smooth'});
    }));
    list.addEventListener('scroll', refresh, {passive: true});
    new ResizeObserver(refresh).observe(list);
    refresh();
  });
});
