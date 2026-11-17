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
package org.silverpeas.looks.aurora;

import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.components.rssaggregator.service.RSSService;
import org.silverpeas.components.rssaggregator.service.RSSServiceProvider;
import org.silverpeas.looks.aurora.homepage.HomepageContentStore;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HomepageFeedRenderingTest {
  private LookAuroraHelper helper(OrganizationController organization) {
    return mock(LookAuroraHelper.class, invocation -> {
      switch (invocation.getMethod().getName()) {
        case "getConfiguredNextEvents", "getConfiguredRSSFeeds", "getDefaultEventsConfiguration",
            "getDefaultRssConfiguration", "getRSSFeeds" -> { return invocation.callRealMethod(); }
        case "getOrganisationController" -> { return organization; }
        case "getUserId" -> { return "42"; }
        case "getSettings" -> { return invocation.getArgument(1); }
        default -> { return org.mockito.Answers.RETURNS_DEFAULTS.answer(invocation); }
      }
    });
  }

  @Test
  void eventsFilterSourcesByReaderRightsAndPassConfiguredOptions() {
    var organization = mock(OrganizationController.class);
    when(organization.isComponentAvailableToUser("almanach12", "42")).thenReturn(true);
    var helper = helper(organization);
    helper.getConfiguredNextEvents(new HomepageContentStore.EventsConfiguration(
        "almanach12 almanach34", 5, false, false, true, true));
    verify(helper).getNextEvents(List.of("almanach12"), false, 5, true);
    verify(organization).isComponentAvailableToUser("almanach34", "42");
  }

  @Test
  void rssNeverFetchesAnUnavailableApplicationAndPreservesTheRequestedRendering() throws Exception {
    var organization = mock(OrganizationController.class);
    var helper = helper(organization);
    var service = mock(RSSService.class);
    try (var provider = mockStatic(RSSServiceProvider.class)) {
      provider.when(RSSServiceProvider::getRSSService).thenReturn(service);
      assertNull(helper.getConfiguredRSSFeeds(new HomepageContentStore.RssConfiguration("rssAgregator34", "tabs")));
      provider.verifyNoInteractions();
      when(organization.isComponentAvailableToUser("rssAgregator12", "42")).thenReturn(true);
      when(service.getApplicationItems("rssAgregator12", true)).thenReturn(List.of());
      when(service.getAllChannels("rssAgregator12")).thenReturn(List.of());
      var feeds = helper.getConfiguredRSSFeeds(new HomepageContentStore.RssConfiguration("rssAgregator12", "tabs"));
      assertTrue(feeds.isTabs());
      verify(service).getApplicationItems("rssAgregator12", true);
      verify(service).getAllChannels("rssAgregator12");
      verifyNoMoreInteractions(service);
    }
  }
}
