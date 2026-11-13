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
package org.silverpeas.looks.aurora.matomo;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.silverpeas.components.quickinfo.model.News;
import org.silverpeas.components.quickinfo.model.QuickInfoService;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class TrackableContentProviderTest {
  @Test
  void identifiesNewsWithinItsQuickInfoInstance() throws Exception {
    var provider = mock(TrackableContentProvider.class, CALLS_REAL_METHODS);
    var service = mock(QuickInfoService.class);
    var field = TrackableContentProvider.class.getDeclaredField("quickInfoService");
    field.setAccessible(true);
    field.set(provider, service);
    var request = mock(HttpServletRequest.class);
    when(request.getRequestURI()).thenReturn("/silverpeas/Rquickinfo/quickinfo12/View");
    when(request.getParameter("Id")).thenReturn("42");
    var news = mock(News.class);
    when(news.getTitle()).thenReturn("Actualité");
    when(news.getPermalink()).thenReturn("/silverpeas/Publication/42");
    when(service.getNews(new ResourceReference("42", "quickinfo12"))).thenReturn(news);
    var content = provider.get(request);
    assertEquals("Actualité", content.getContentName());
    assertEquals("/silverpeas/Publication/42", content.getPermalink());
    verify(service).getNews(new ResourceReference("42", "quickinfo12"));
  }
}
