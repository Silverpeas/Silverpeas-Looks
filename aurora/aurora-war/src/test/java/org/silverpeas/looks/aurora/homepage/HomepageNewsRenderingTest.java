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
package org.silverpeas.looks.aurora.homepage;

import org.junit.jupiter.api.Test;
import org.silverpeas.looks.aurora.LookAuroraHelper;
import org.silverpeas.looks.aurora.NewsList;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HomepageNewsRenderingTest {
  private LookAuroraHelper helper() {
    var organization = mock(org.silverpeas.core.admin.service.OrganizationController.class);
    when(organization.isComponentAvailableToUser(eq("quickinfo12"), any())).thenReturn(true);
    return mock(LookAuroraHelper.class, invocation -> {
      String method = invocation.getMethod().getName();
      if (method.equals("getConfiguredNews")) {
        return invocation.callRealMethod();
      }
      if (method.equals("getSettings")) {
        return invocation.getArgument(1);
      }
      if (method.equals("getOrganisationController")) {
        return organization;
      }
      return org.mockito.Answers.RETURNS_DEFAULTS.answer(invocation);
    });
  }

  private void inject(Object target, String name, Object value) throws Exception {
    var field = LookAuroraHelper.class.getDeclaredField(name);
    field.setAccessible(true);
    field.set(target, value);
  }

  @Test
  void configuredApplicationsPassThroughUserAccessFiltering() throws Exception {
    var helper = helper();
    var newsService = mock(org.silverpeas.components.quickinfo.model.QuickInfoService.class);
    when(newsService.getVisibleNews("quickinfo12")).thenReturn(new ArrayList<>());
    inject(helper, "quickInfoService", newsService);
    inject(helper, "admin", mock(org.silverpeas.core.admin.service.Administration.class));
    var result = helper.getConfiguredNews(1, new HomepageContentStore.NewsConfiguration(
        "quickinfo12 quickinfo34", 5, true, "LIST", true, ""));
    assertEquals(NewsList.RenderingType.LIST, result.getRenderingType());
    verify(newsService).getVisibleNews("quickinfo12");
    verify(newsService, never()).getVisibleNews("quickinfo34");
  }

  @Test
  void hiddenBlockDoesNotRetrieveNews() {
    var helper = helper();
    assertTrue(helper.getConfiguredNews(1, new HomepageContentStore.NewsConfiguration(
        "*", 5, false, "CAROUSEL", false, "")).isEmpty());
    assertTrue(mockingDetails(helper).getInvocations().stream()
        .noneMatch(i -> i.getMethod().getName().startsWith("getNewsBy") ||
            i.getMethod().getName().equals("getDelegatedNews")));
  }
}
