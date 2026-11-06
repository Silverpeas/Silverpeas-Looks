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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.looks.aurora.matomo;

import org.junit.jupiter.api.Test;
import org.silverpeas.components.blog.service.BlogService;
import org.silverpeas.components.quickinfo.model.News;
import org.silverpeas.components.quickinfo.model.QuickInfoService;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import jakarta.servlet.http.HttpServletRequest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests on the tracking of the news viewed in a QuickInfo instance: the news is looked for
 * in the instance targeted by the request.
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class TrackableContentProviderTest {

  private static final String QUICKINFO_ID = "quickinfo12";
  private static final String NEWS_ID = "news_1";

  @TestManagedMock
  private BlogService blogService;
  @TestManagedMock
  private QuickInfoService quickInfoService;
  @TestManagedMock
  private PublicationService publicationService;
  @TestedBean
  private TrackableContentProvider provider;

  @Test
  void aNewsViewedInItsInstanceIsTracked() {
    News news = mock(News.class);
    when(news.getTitle()).thenReturn("The news");
    when(news.getPermalink()).thenReturn("/silverpeas/Publication/128");
    when(quickInfoService.getNews(new ResourceReference(NEWS_ID, QUICKINFO_ID))).thenReturn(news);

    TrackableContent content = provider.get(aViewRequestOf(NEWS_ID, "/View"));

    assertThat(content.isContent(), is(true));
    assertThat(content.getContentType(), is("News"));
    assertThat(content.getContentId(), is(NEWS_ID));
    assertThat(content.getContentName(), is("The news"));
    assertThat(content.getPermalink(), is("/silverpeas/Publication/128"));
  }

  @Test
  void aNewsNotInTheViewedInstanceIsNotTracked() {
    TrackableContent content = provider.get(aViewRequestOf("news_2", "/View"));

    assertThat(content.isContent(), is(false));
  }

  @Test
  void aNewsViewedFromItsPublicationIsNotTrackedAsANews() {
    TrackableContent content = provider.get(aViewRequestOf("128", "/ViewOnly"));

    assertThat(content.isContent(), is(false));
  }

  private static HttpServletRequest aViewRequestOf(String newsId, String function) {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRequestURI()).thenReturn("/silverpeas/Rquickinfo/" + QUICKINFO_ID + function);
    when(request.getParameter("Id")).thenReturn(newsId);
    return request;
  }
}
