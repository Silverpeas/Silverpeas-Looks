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
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
(function() {
  if (window.auroraHomepageFeeds) { return; }
  const initialize = () => {
    document.querySelectorAll('.homepage-rss').forEach(element => {
      if (element.dataset.initialized) { return; }
      element.dataset.initialized = 'true';
      const root = $(element);
      if (root.hasClass('rss_carrousel')) {
        root.find('.homepage-rss-slider').responsiveSlides({auto: true, pager: false,
          nav: false, speed: 500, pause: true, timeout: 6000});
      }
      root.find('.description-item-rssNews a').attr('target', '_blank');
      root.find('.deploy-item').on('click', function(event) {
        if ($(event.target).closest('a').length) { return; }
        const item = $(this);
        const description = item.children('.itemDeploy');
        description.prop('hidden', !description.prop('hidden'));
        item.toggleClass('open');
      });
      const items = root.find('.item-channel');
      const links = root.find('.rss_type > a');
      const selectTab = link => {
        links.removeClass('selected');
        link.addClass('selected');
        items.hide().filter('.' + link.attr('data-channel')).show();
      };
      links.on('click', function(event) {
        event.preventDefault();
        const link = $(this);
        if (root.hasClass('rss_tab')) { selectTab(link); }
        else {
          items.filter('.' + link.attr('data-channel')).toggle();
          link.toggleClass('off');
        }
      });
      if (root.hasClass('rss_tab') && links.length) { selectTab(links.first()); }
    });
  };
  window.auroraHomepageFeeds = {initialize};
  whenSilverpeasReady(function() {
    $(function() {
      initialize();
      new MutationObserver(initialize).observe(document.body, {childList: true, subtree: true});
    });
  });
})();
