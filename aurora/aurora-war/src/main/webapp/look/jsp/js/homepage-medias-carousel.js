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
    const position = widget.querySelector('.homepage-medias-position');
    if (!list || !controls) { return; }
    const slides = Array.from(list.children);
    const buttons = controls.querySelectorAll('button');
    let current = 0;
    const show = index => {
      current = (index + slides.length) % slides.length;
      slides.forEach((slide, i) => { slide.hidden = i !== current; });
      if (position) { position.textContent = `${current + 1} / ${slides.length}`; }
    };
    if (!slides.length) { return; }
    controls.hidden = slides.length < 2;
    buttons.forEach(button => button.addEventListener('click', () => {
      show(current + Number(button.dataset.direction));
    }));
    list.addEventListener('keydown', event => {
      if (event.key === 'ArrowLeft' || event.key === 'ArrowRight') {
        event.preventDefault();
        show(current + (event.key === 'ArrowLeft' ? -1 : 1));
      }
    });
    let touchStart;
    list.addEventListener('touchstart', event => {
      touchStart = event.touches.length === 1 ? event.touches[0] : null;
    }, {passive: true});
    list.addEventListener('touchend', event => {
      if (!touchStart || !event.changedTouches.length) { return; }
      const dx = event.changedTouches[0].clientX - touchStart.clientX;
      const dy = event.changedTouches[0].clientY - touchStart.clientY;
      if (Math.abs(dx) > 50 && Math.abs(dx) > Math.abs(dy)) {
        show(current + (dx < 0 ? 1 : -1));
      }
      touchStart = null;
    }, {passive: true});
    show(0);
  });
});
