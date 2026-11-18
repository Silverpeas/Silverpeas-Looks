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
            "getDefaultRssConfiguration", "getRSSFeeds", "getConfiguredQuestions", "getConfiguredNewUsers",
            "getDefaultFaqConfiguration", "getDefaultUsersConfiguration", "getConfiguredSubscriptions",
            "getConfiguredSearchForm", "getDefaultSearchConfiguration", "getLastNewsSubscribed", "getDefaultSubscriptionsConfiguration" -> { return invocation.callRealMethod(); }
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
  @Test
  void faqRespectsReaderRightsAndSelectionSizeWithoutRepeatingQuestions() throws Exception {
    var organization = mock(OrganizationController.class);
    var helper = helper(organization);
    var manager = mock(org.silverpeas.components.questionreply.service.QuestionManager.class);
    try (var provider = mockStatic(org.silverpeas.components.questionreply.service.QuestionManagerProvider.class)) {
      provider.when(org.silverpeas.components.questionreply.service.QuestionManagerProvider::getQuestionManager).thenReturn(manager);
      assertNull(helper.getConfiguredQuestions(new HomepageContentStore.FaqConfiguration("questionReply12", 2, "random")));
      provider.verifyNoInteractions();
      when(organization.isComponentAvailableToUser("questionReply12", "42")).thenReturn(true);
      when(organization.getUserProfiles("42", "questionReply12")).thenReturn(new String[]{"writer"});
      var q1 = mock(org.silverpeas.components.questionreply.model.Question.class);
      var q2 = mock(org.silverpeas.components.questionreply.model.Question.class);
      var q3 = mock(org.silverpeas.components.questionreply.model.Question.class);
      when(manager.getQuestions("questionReply12")).thenReturn(List.of(q1, q2, q3));
      var first = helper.getConfiguredQuestions(new HomepageContentStore.FaqConfiguration("questionReply12", 2, "first"));
      assertEquals(List.of(q1, q2), first.getList());
      assertTrue(first.isCanAskAQuestion());
      var random = helper.getConfiguredQuestions(new HomepageContentStore.FaqConfiguration("questionReply12", 2, "random"));
      assertEquals(2, random.getList().size());
      assertEquals(2, new java.util.HashSet<>(random.getList()).size());
      assertTrue(List.of(q1, q2, q3).containsAll(random.getList()));
      when(organization.getUserProfiles("42", "questionReply12")).thenReturn(new String[]{"reader"});
      var all = helper.getConfiguredQuestions(new HomepageContentStore.FaqConfiguration("questionReply12", 10, "first"));
      assertEquals(List.of(q1, q2, q3), all.getList());
      assertFalse(all.isCanAskAQuestion());
      when(manager.getQuestions("questionReply12")).thenReturn(List.of());
      assertNull(helper.getConfiguredQuestions(new HomepageContentStore.FaqConfiguration("questionReply12", 2, "random")));
    }
  }

  @Test
  void newUsersSortByCreationAndHandleSmallEmptyAndDisabledLists() {
    var organization = mock(OrganizationController.class);
    var helper = helper(organization);
    var disabled = new HomepageContentStore.UsersConfiguration("", 0, true, "");
    assertNull(helper.getConfiguredNewUsers(disabled));
    verifyNoInteractions(organization);
    var older = new org.silverpeas.core.admin.user.model.UserDetail();
    older.setId("1");
    older.setCreationDate(new java.util.Date(1000));
    var newer = new org.silverpeas.core.admin.user.model.UserDetail();
    newer.setId("2");
    newer.setCreationDate(new java.util.Date(2000));
    when(organization.searchUsers(any())).thenReturn(new org.silverpeas.core.util.SilverpeasArrayList<>(List.of(older, newer)));
    var config = new HomepageContentStore.UsersConfiguration("34", 5, false, "phone title");
    var users = helper.getConfiguredNewUsers(config);
    assertEquals(List.of(newer, older), users.getUsers());
    assertFalse(users.isAvatar());
    assertEquals(List.of("phone", "title"), users.getFields());
    var criterion = org.mockito.ArgumentCaptor.forClass(org.silverpeas.core.admin.user.model.UserDetailsSearchCriteria.class);
    verify(organization).searchUsers(criterion.capture());
    assertArrayEquals(new String[]{"34"}, criterion.getValue().getCriterionOnGroupIds());
    assertTrue(java.util.Arrays.asList(criterion.getValue().getCriterionOnUserStatesToExclude())
        .containsAll(List.of(org.silverpeas.core.admin.user.constant.UserState.BLOCKED,
            org.silverpeas.core.admin.user.constant.UserState.DEACTIVATED,
            org.silverpeas.core.admin.user.constant.UserState.REMOVED)));
    assertEquals(List.of(newer), helper.getConfiguredNewUsers(
        new HomepageContentStore.UsersConfiguration("", 1, true, "")).getUsers());
    when(organization.searchUsers(any())).thenReturn(new org.silverpeas.core.util.SilverpeasArrayList<>(List.of(older)));
    assertEquals(List.of(older), helper.getConfiguredNewUsers(config).getUsers());
    when(organization.searchUsers(any())).thenReturn(new org.silverpeas.core.util.SilverpeasArrayList<>(List.of()));
    assertTrue(helper.getConfiguredNewUsers(config).getUsers().isEmpty());
  }

  @Test
  void subscriptionsUseTheReaderAndSkipLostAccessThenSortAndLimitVisibleNews() throws Exception {
    var organization = mock(OrganizationController.class);
    var helper = helper(organization);
    var quickinfo = mock(org.silverpeas.components.quickinfo.model.QuickInfoService.class);
    var field = LookAuroraHelper.class.getDeclaredField("quickInfoService");
    field.setAccessible(true);
    field.set(helper, quickinfo);
    var service = mock(org.silverpeas.core.subscription.SubscriptionService.class);
    var subscriptions = new org.silverpeas.core.subscription.util.SubscriptionList();
    subscriptions.add(new org.silverpeas.core.subscription.service.ComponentSubscription("42", "quickinfo12"));
    subscriptions.add(new org.silverpeas.core.subscription.service.ComponentSubscription("42", "quickinfo34"));
    subscriptions.add(new org.silverpeas.core.subscription.service.ComponentSubscription("42", "quickinfo12"));
    subscriptions.add(new org.silverpeas.core.subscription.service.ComponentSubscription("42", "almanach12"));
    when(service.getByUserSubscriber("42")).thenReturn(subscriptions);
    when(organization.isComponentAvailableToUser("quickinfo12", "42")).thenReturn(true);
    var older = mock(org.silverpeas.components.quickinfo.model.News.class);
    var newer = mock(org.silverpeas.components.quickinfo.model.News.class);
    when(older.getPublishDate()).thenReturn(new java.util.Date(1000));
    when(newer.getPublishDate()).thenReturn(new java.util.Date(2000));
    when(quickinfo.getVisibleNews("quickinfo12")).thenReturn(List.of(older, newer));
    try (var provider = mockStatic(org.silverpeas.core.subscription.SubscriptionServiceProvider.class)) {
      provider.when(org.silverpeas.core.subscription.SubscriptionServiceProvider::getSubscribeService).thenReturn(service);
      var result = helper.getConfiguredSubscriptions(new HomepageContentStore.SubscriptionsConfiguration(1, false));
      assertEquals(List.of(newer), result.stream().map(AuroraNews::getNews).toList());
      verify(service).getByUserSubscriber("42");
      verify(quickinfo).getVisibleNews("quickinfo12");
      verify(quickinfo, never()).getVisibleNews("quickinfo34");
      verify(quickinfo, never()).getVisibleNews("almanach12");
      when(service.getByUserSubscriber("42")).thenReturn(new org.silverpeas.core.subscription.util.SubscriptionList());
      assertTrue(helper.getLastNewsSubscribed().isEmpty());
      verifyNoMoreInteractions(quickinfo);
    }
  }

  @Test
  void searchDefaultsPreferTheConfiguredTemplateAndLoadAnIndependentEmptyForm() throws Exception {
    var helper = helper(mock(OrganizationController.class));
    var settings = mock(LookSettings.class);
    when(helper.getLookSettings()).thenReturn(settings);
    when(settings.isExtraSearchFieldPeriodUsed()).thenReturn(true);
    assertEquals("taxonomy", helper.getDefaultSearchConfiguration().mode());
    doReturn("document.xml").when(helper).getSettings("home.search.template", "");
    assertEquals(new HomepageContentStore.SearchConfiguration("advanced", "document.xml", false, true, false),
        helper.getDefaultSearchConfiguration());
    var manager = mock(org.silverpeas.core.contribution.template.publication.PublicationTemplateManager.class);
    var template = mock(org.silverpeas.core.contribution.template.publication.PublicationTemplateImpl.class);
    var form = mock(org.silverpeas.core.contribution.content.form.Form.class);
    var recordTemplate = mock(org.silverpeas.core.contribution.content.form.RecordTemplate.class);
    var record = mock(org.silverpeas.core.contribution.content.form.DataRecord.class);
    when(manager.loadPublicationTemplate("document.xml")).thenReturn(template);
    when(template.getSearchForm()).thenReturn(form);
    when(template.getSearchTemplate()).thenReturn(recordTemplate);
    when(recordTemplate.getEmptyRecord()).thenReturn(record);
    try (var provider = mockStatic(org.silverpeas.core.contribution.template.publication.PublicationTemplateManager.class)) {
      provider.when(org.silverpeas.core.contribution.template.publication.PublicationTemplateManager::getInstance).thenReturn(manager);
      assertSame(form, helper.getConfiguredSearchForm("document.xml"));
      verify(form).setData(record);
      assertNull(helper.getConfiguredSearchForm("missing.xml"));
      when(template.getSearchForm()).thenReturn(null);
      assertNull(helper.getConfiguredSearchForm("document.xml"));
    }
  }

}
