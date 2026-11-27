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
import org.junit.jupiter.api.io.TempDir;
import org.silverpeas.looks.aurora.LookAuroraHelper;
import org.silverpeas.looks.aurora.NewsList;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.List;
import java.util.ConcurrentModificationException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HomepageWidgetsTest {
  @TempDir Path root;
  private final HomepagePage page = new HomepagePage("skins", "waves");

  private LookAuroraHelper helper() {
    var helper = mock(LookAuroraHelper.class);
    var settings = mock(org.silverpeas.looks.aurora.LookSettings.class);
    when(settings.isDisplayEphemeris()).thenReturn(true);
    when(helper.getLookSettings()).thenReturn(settings);
    when(settings.getNoBookmarksFragmentURL()).thenReturn("");
    when(helper.getDefaultSearchConfiguration()).thenReturn(
        new HomepageContentStore.SearchConfiguration("taxonomy", "", false, false, false));
    when(helper.getDefaultSubscriptionsConfiguration()).thenReturn(
        new HomepageContentStore.SubscriptionsConfiguration(4, true));
    when(settings.isDisplayLastSubscribedNews()).thenReturn(true);
    when(helper.getDefaultFaqConfiguration()).thenReturn(
        new HomepageContentStore.FaqConfiguration("questionReply12", 2, "random"));
    when(helper.getDefaultUsersConfiguration()).thenReturn(
        new HomepageContentStore.UsersConfiguration("12", 5, true, "title phone"));
    when(helper.getDefaultEventsConfiguration()).thenReturn(
        new HomepageContentStore.EventsConfiguration("almanach12", 3, true, false, true, true));
    when(helper.getDefaultRssConfiguration()).thenReturn(
        new HomepageContentStore.RssConfiguration("rssAgregator12", "tabs"));
    when(helper.getDefaultPublicationsConfiguration()).thenReturn(
        new HomepageContentStore.PublicationsConfiguration("WA12", 3, true));
    when(helper.getDefaultNewsConfiguration(anyInt())).thenReturn(
        new HomepageContentStore.NewsConfiguration("*", 10, false, "LIST", true, ""));
    return helper;
  }

  private HomepageContentStore.NewsConfiguration news(String revision) {
    return new HomepageContentStore.NewsConfiguration("*", 5, false, "LIST", true, revision);
  }

  @Test
  void spaceStorageIsFlatAndIsolatedFromOtherSpacesAndSkins() throws Exception {
    var store = new HomepageContentStore(root);
    var space = new HomepagePage("spaces", "1");
    assertEquals("WA1", space.owner());
    assertThrows(IllegalArgumentException.class, () -> new HomepagePage("spaces", "../WA1"));
    assertThrows(IllegalArgumentException.class, () -> new HomepagePage("spaces", "waves"));
    store.save(space, "intro", "fr", "Accueil", "Bonjour", "");
    assertTrue(Files.exists(root.resolve("spaces/WA1.properties")));
    assertTrue(Files.exists(root.resolve("spaces/WA1/widgets/intro/content-fr.html")));
    assertFalse(Files.exists(root.resolve("spaces/WA1/page.properties")));
    assertEquals("Bonjour", new HomepageContentStore(root).read(
        new HomepagePage("spaces", "WA1"), "intro", "fr").html());
    assertTrue(store.widgets(new HomepagePage("spaces", "WA2"), "fr").isEmpty());
    assertTrue(store.widgets(page, "fr").isEmpty());
  }

  @Test
  void spaceNewsAndPublicationsAlwaysUseTheirOwnSpace() throws Exception {
    var store = new HomepageContentStore(root);
    var space = new HomepagePage("spaces", "WA1");
    var helper = helper();
    var layout = new HomepageContentStore.Layout("main", 0, true);
    store.saveNewsWidget(space, "news-local", "fr", "Infos", layout, news(""), 0);
    store.savePublicationsWidget(space, "pubs", "fr", "Documents", layout,
        new HomepageContentStore.PublicationsConfiguration("WA999", 5, true), "");
    var list = mock(NewsList.class);
    when(helper.getNewsOfSpace("WA1", false, false, 5)).thenReturn(list);
    var widgets = HomepageWidgets.all(store, space, "fr", helper);
    assertEquals(list, HomepageWidgets.news(store, space, widgets, helper).get("news-local"));
    HomepageWidgets.publications(space, widgets, helper);
    verify(helper).getLatestPublications("WA1", 5);
    verify(helper, never()).getConfiguredNews(anyInt(), any());
    verify(helper, never()).getLatestPublications(eq("WA999"), anyInt());
    assertEquals("WA1", HomepageWidgets.publicationsWidget(store, space, "new-publications", "fr", helper)
        .publicationsConfiguration().spaceId());
  }

  @Test
  void emptyHomepageRemainsAccessibleWithoutConsultingHistoricalNews() {
    var helper = mock(LookAuroraHelper.class, CALLS_REAL_METHODS);
    assertTrue(helper.isUserCanDisplayMainHomePage());
    verify(helper, never()).getNews();
  }

  @Test
  void deletedHistoricalWidgetDoesNotReappear() throws Exception {
    var store = new HomepageContentStore(root);
    var page = new HomepagePage("skins", "waves");
    store.deleteWidget(page, "weather-legacy", "fr", "");
    store.save(page, "custom", "fr", "Title", "Body", "");
    store.deleteWidget(page, "custom", "fr", store.read(page, "custom", "fr").revision());
    var helper = helper();
    for (String language : List.of("fr", "en")) {
      assertTrue(HomepageWidgets.all(new HomepageContentStore(root), page, language, helper).stream()
          .noneMatch(widget -> List.of("weather-legacy", "custom").contains(widget.id())));
    }
  }

  @Test
  void newsAndHtmlSharePlacementAndSupportMoreThanThreeNewsInstances() throws Exception {
    var store = new HomepageContentStore(root);
    store.saveConfigured(page, "html-intro", "fr", "Introduction", "Bonjour", "",
        new HomepageContentStore.Layout("right", 3, true));
    for (int index = 1; index <= 5; index++) {
      store.saveNewsWidget(page, "news-" + index, "fr", "Actualités " + index,
          new HomepageContentStore.Layout(index == 5 ? "main" : "right", index, index != 4), news(""), 0);
    }
    var widgets = new HomepageContentStore(root).widgets(page, "fr");
    assertEquals(6, widgets.size());
    assertEquals(List.of("news-1", "news-2", "html-intro", "news-3", "news-4", "news-5"),
        widgets.stream().map(HomepageContentStore.Widget::getId).toList());
    assertEquals("html", widgets.get(2).type());
    assertEquals("news", widgets.get(3).type());
    assertFalse(widgets.get(4).isEnabled());
    assertEquals("main", widgets.get(5).getZone());
    assertEquals("Bonjour", store.read(page, "html-intro", "fr").html());
  }

  @Test
  void newsSettingsAndPlacementParticipateInTheSameRevision() throws Exception {
    var store = new HomepageContentStore(root);
    store.saveNewsWidget(page, "news-custom", "fr", "Infos",
        new HomepageContentStore.Layout("main", 1, true), news(""), 0);
    String revision = store.readWidget(page, "news-custom", "fr").content().revision();
    store.saveNewsWidget(page, "news-custom", "fr", "Infos",
        new HomepageContentStore.Layout("right", 2, false), news(revision), 0);
    assertThrows(ConcurrentModificationException.class, () -> store.saveNewsWidget(page,
        "news-custom", "fr", "Ancien", new HomepageContentStore.Layout("main", 1, true), news(revision), 0));
    assertEquals("right", store.readWidget(page, "news-custom", "fr").getZone());
    assertThrows(IllegalArgumentException.class, () -> store.save(page, "news-custom", "fr", "", "", ""));
  }

  @Test
  void emptyHomepageDoesNotImportHistoricalNewsOrReadSkinDefaults() throws Exception {
    var store = new HomepageContentStore(root);
    var helper = helper();
    assertTrue(HomepageWidgets.all(store, page, "fr", helper).isEmpty());
    assertFalse(Files.exists(root.resolve("skins")));
    Files.createDirectories(root.resolve("skins/waves"));
    Files.writeString(root.resolve("skins/waves/news-2.properties"), "source=delegated\nsize=4\n");
    assertTrue(HomepageWidgets.all(store, page, "fr", helper).isEmpty());
    var fresh = HomepageWidgets.newsWidget(store, page, "news-new", "fr", helper);
    assertEquals("*", fresh.newsConfiguration().source());
    assertEquals(0, fresh.legacyIndex());
    verify(helper, never()).getDefaultNewsConfiguration(anyInt());
    store.saveNewsWidget(page, fresh.id(), "fr", "Équipe",
        new HomepageContentStore.Layout("right", 1, false), news(""), 0);
    assertEquals(List.of("news-new"), HomepageWidgets.all(store, page, "fr", helper).stream()
        .map(HomepageContentStore.Widget::id).toList());
  }

  @Test
  void rendererSkipsHiddenWidgetsWithoutHistoricalFallback() throws Exception {
    var store = new HomepageContentStore(root);
    var helper = helper();
    var empty = new NewsList(List.of(), null);
    when(helper.getConfiguredNews(anyInt(), any())).thenReturn(empty);
    store.saveNewsWidget(page, "news-hidden", "fr", "Masqué",
        new HomepageContentStore.Layout("right", 1, false), news(""), 0);
    store.saveNewsWidget(page, "news-visible", "fr", "Visible",
        new HomepageContentStore.Layout("right", 2, true), news(""), 0);
    var widgets = HomepageWidgets.all(store, page, "fr", helper);
    var rendered = HomepageWidgets.news(store, page, widgets, helper);
    assertFalse(rendered.containsKey("news-hidden"));
    assertTrue(rendered.containsKey("news-visible"));
    verify(helper).getConfiguredNews(eq(1), notNull());
    assertEquals(1, rendered.size());
  }
  @Test
  void buttonSettingsDefaultToVisibleAndParticipateInRevision() throws Exception {
    var store = new HomepageContentStore(root);
    var layout = new HomepageContentStore.Layout("main", 1, true);
    store.saveNewsWidget(page, "news", "fr", "Infos", layout, news(""), 0);
    var first = store.readWidget(page, "news", "fr");
    assertTrue(first.newsConfiguration().showMoreNews());
    assertTrue(first.newsConfiguration().showAllNews());
    store.saveNewsWidget(page, "news", "fr", "Infos", layout,
        new HomepageContentStore.NewsConfiguration("*", 5, false, "LIST", true,
            first.content().revision(), false, true), 0);
    var changed = new HomepageContentStore(root).readWidget(page, "news", "fr");
    assertFalse(changed.newsConfiguration().showMoreNews());
    assertTrue(changed.newsConfiguration().showAllNews());
    assertNotEquals(first.content().revision(), changed.content().revision());
    assertThrows(ConcurrentModificationException.class, () -> store.saveNewsWidget(page,
        "news", "fr", "Infos", layout, news(first.content().revision()), 0));
  }

  @Test
  void publicationsReplaceLegacyWithoutDuplicatesAndSharePlacementAndRevision() throws Exception {
    var store = new HomepageContentStore(root);
    var helper = helper();
    var virtual = HomepageWidgets.publicationsWidget(store, page, "publications-legacy", "fr", helper);
    assertEquals("", virtual.publicationsConfiguration().spaceId());
    assertFalse(Files.exists(root.resolve("skins")));
    var layout = new HomepageContentStore.Layout("right", 2, true);
    var configuration = new HomepageContentStore.PublicationsConfiguration("WA42", 8, false);
    store.savePublicationsWidget(page, virtual.id(), "fr", "Documents", layout, configuration, "");
    var saved = new HomepageContentStore(root).readWidget(page, virtual.id(), "fr");
    assertEquals(configuration, saved.publicationsConfiguration());
    assertEquals("right", saved.getZone());
    assertEquals(1, HomepageWidgets.all(store, page, "fr", helper).stream()
        .filter(widget -> "publications".equals(widget.type())).count());
    store.savePublicationsWidget(page, "publications-hidden", "fr", "Masqué",
        new HomepageContentStore.Layout("main", 1, false), configuration, "");
    store.saveConfigured(page, "html", "fr", "Intro", "", "",
        new HomepageContentStore.Layout("right", 1, true));
    assertEquals(List.of("html", "publications-hidden", "publications-legacy"),
        store.widgets(page, "fr").stream().map(HomepageContentStore.Widget::getId).toList());
    HomepageWidgets.publications(HomepageWidgets.all(store, page, "fr", helper), helper);
    verify(helper, times(1)).getLatestPublications("WA42", 8);
    store.savePublicationsWidget(page, virtual.id(), "fr", "Documents", layout,
        new HomepageContentStore.PublicationsConfiguration("", 8, true), saved.content().revision());
    assertThrows(ConcurrentModificationException.class, () -> store.savePublicationsWidget(page,
        virtual.id(), "fr", "Documents", layout, configuration, saved.content().revision()));
    assertThrows(IllegalArgumentException.class, () -> store.save(page, virtual.id(), "fr", "", "", ""));
    assertThrows(IllegalArgumentException.class, () -> store.saveNewsWidget(page,
        virtual.id(), "fr", "", layout, news(""), 0));
    assertThrows(IllegalArgumentException.class, () -> store.savePublicationsWidget(page,
        "news-legacy-1", "fr", "", layout, configuration, ""));
    assertThrows(IllegalArgumentException.class, () ->
        new HomepageContentStore.PublicationsConfiguration("../x", 3, true));
    assertThrows(IllegalArgumentException.class, () ->
        new HomepageContentStore.PublicationsConfiguration("", 0, true));
  }

  @Test
  void existingNewsDefaultsToVisibleButtonsWithoutChangingItsRevision() throws Exception {
    var store = new HomepageContentStore(root);
    var folder = root.resolve("skins/waves");
    Files.createDirectories(folder);
    Files.writeString(folder.resolve("page.properties"), """
        widgets=news-old
        widget.news-old.type=news
        widget.news-old.title=Infos
        widget.news-old.source=*
        widget.news-old.size=5
        widget.news-old.importantOnly=false
        widget.news-old.rendering=LIST
        widget.news-old.legacyIndex=0
        """);
    String previousInput = "Infos" + new HomepageContentStore.Layout("main", 10, true)
        + String.join("\u0000", "*", "5", "false", "LIST", "0") + "\u0000";
    String previousRevision = java.util.HexFormat.of().formatHex(
        java.security.MessageDigest.getInstance("SHA-256").digest(
            previousInput.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    var widget = store.readWidget(page, "news-old", "fr");
    assertEquals(previousRevision, widget.content().revision());
    assertTrue(widget.newsConfiguration().showMoreNews());
    assertTrue(widget.newsConfiguration().showAllNews());
    store.saveNewsWidget(page, widget.id(), "fr", widget.getTitle(), widget.content().layout(),
        new HomepageContentStore.NewsConfiguration("*", 5, false, "LIST", true,
            previousRevision, false, false), 0);
    assertFalse(store.readWidget(page, widget.id(), "fr").newsConfiguration().showAllNews());
  }

  @Test
  void ephemerisKeepsThemeDefaultAndSharesPlacementWithoutDuplicateOrTypeConversion() throws Exception {
    var store = new HomepageContentStore(root);
    var helper = helper();
    when(helper.getLookSettings().isDisplayEphemeris()).thenReturn(false);
    var legacy = HomepageWidgets.ephemerisWidget(store, page, "ephemeris-legacy", "fr", helper);
    assertTrue(legacy.isEnabled());
    assertEquals("right", legacy.getZone());
    assertFalse(Files.exists(root.resolve("skins")));
    var layout = new HomepageContentStore.Layout("main", 15, true);
    store.saveEphemerisWidget(page, legacy.id(), "fr", "Aujourd’hui", layout, "");
    var saved = new HomepageContentStore(root).readWidget(page, legacy.id(), "fr");
    assertEquals("ephemeris", saved.type());
    assertEquals(layout, saved.content().layout());
    assertEquals("Aujourd’hui", saved.getTitle());
    assertEquals(1, HomepageWidgets.all(store, page, "fr", helper).stream()
        .filter(widget -> "ephemeris".equals(widget.type())).count());
    store.saveEphemerisWidget(page, "ephemeris-second", "fr", "Autre",
        new HomepageContentStore.Layout("right", 1, false), "");
    assertEquals(2, store.widgets(page, "fr").size());
    store.saveEphemerisWidget(page, legacy.id(), "fr", "Aujourd’hui",
        new HomepageContentStore.Layout("right", 2, false), saved.content().revision());
    assertThrows(ConcurrentModificationException.class, () -> store.saveEphemerisWidget(page,
        legacy.id(), "fr", "Ancien", layout, saved.content().revision()));
    assertThrows(IllegalArgumentException.class, () -> store.save(page, legacy.id(), "fr", "", "", ""));
    assertThrows(IllegalArgumentException.class, () -> store.saveNewsWidget(page, legacy.id(), "fr", "", layout, news(""), 0));
    assertThrows(IllegalArgumentException.class, () -> store.saveEphemerisWidget(page,
        "publications-legacy", "fr", "", layout, ""));
    assertThrows(IllegalArgumentException.class, () -> store.saveEphemerisWidget(page,
        "news-legacy-1", "fr", "", layout, ""));
    store.saveConfigured(page, "html", "fr", "", "", "", layout);
    assertThrows(IllegalArgumentException.class, () -> store.saveEphemerisWidget(page,
        "html", "fr", "", layout, ""));
  }

  @Test
  void eventsAndRssPreserveDefaultsPersistMultipleInstancesAndProtectRevisionsAndTypes() throws Exception {
    var store = new HomepageContentStore(root);
    var helper = helper();
    var events = HomepageWidgets.feedWidget(store, page, "events-legacy", "fr", helper, "events");
    var rss = HomepageWidgets.feedWidget(store, page, "rss-legacy", "fr", helper, "rss");
    assertEquals("", events.eventsConfiguration().source());
    assertEquals("aggregate", rss.rssConfiguration().rendering());
    assertFalse(Files.exists(root.resolve("skins")));
    var layout = new HomepageContentStore.Layout("right", 12, false);
    var configuration = new HomepageContentStore.EventsConfiguration("*", 5, false, true, false, false);
    store.saveFeedWidget(page, events.id(), "fr", "Agenda", layout, "events", configuration, null, "");
    var saved = new HomepageContentStore(root).readWidget(page, events.id(), "fr");
    assertEquals(configuration, saved.eventsConfiguration());
    assertEquals(layout, saved.content().layout());
    store.saveFeedWidget(page, "events-second", "fr", "Autre", layout, "events", configuration, null, "");
    assertEquals(2, HomepageWidgets.all(store, page, "fr", helper).stream()
        .filter(widget -> "events".equals(widget.type())).count());
    var changed = new HomepageContentStore.EventsConfiguration("almanach24", 2, true, false, true, true);
    store.saveFeedWidget(page, saved.id(), "fr", "Agenda", layout, "events", changed, null, saved.content().revision());
    assertNotEquals(saved.content().revision(), store.read(page, saved.id(), "fr").revision());
    assertThrows(ConcurrentModificationException.class, () -> store.saveFeedWidget(page, saved.id(), "fr",
        "Agenda", layout, "events", configuration, null, saved.content().revision()));
    assertThrows(IllegalArgumentException.class, () -> store.saveFeedWidget(page, null, "fr", "", layout,
        "events", configuration, null, ""));
    for (String mode : List.of("aggregate", "tabs", "carrousel")) {
      var rssConfiguration = new HomepageContentStore.RssConfiguration("rssAgregator12", mode);
      store.saveFeedWidget(page, "rss-" + mode, "fr", "Veille", layout, "rss", null, rssConfiguration, "");
      assertEquals(rssConfiguration, new HomepageContentStore(root).readWidget(page, "rss-" + mode, "fr").rssConfiguration());
    }
    assertThrows(IllegalArgumentException.class, () -> store.saveFeedWidget(page, "events-legacy", "fr",
        "", layout, "rss", null, rss.rssConfiguration(), ""));
    assertThrows(IllegalArgumentException.class, () -> store.saveFeedWidget(page, "publications-legacy", "fr",
        "", layout, "events", configuration, null, ""));
    for (String id : List.of("events-legacy", "rss-legacy")) {
      assertThrows(IllegalArgumentException.class, () -> store.save(page, id, "fr", "", "", ""));
      assertThrows(IllegalArgumentException.class, () -> store.saveEphemerisWidget(page, id, "fr", "", layout, ""));
      assertThrows(IllegalArgumentException.class, () -> store.saveNewsWidget(page, id, "fr", "", layout, news(""), 0));
      assertThrows(IllegalArgumentException.class, () -> store.savePublicationsWidget(page, id, "fr", "", layout,
          new HomepageContentStore.PublicationsConfiguration("", 3, true), ""));
    }
    store.saveConfigured(page, "html", "fr", "", "", "", layout);
    assertThrows(IllegalArgumentException.class, () -> store.saveFeedWidget(page, "html", "fr", "", layout,
        "events", configuration, null, ""));
    assertThrows(IllegalArgumentException.class, () -> new HomepageContentStore.EventsConfiguration("quickinfo1", 2, true, false, false, true));
    assertThrows(IllegalArgumentException.class, () -> new HomepageContentStore.EventsConfiguration("*", 0, true, false, false, true));
    assertThrows(IllegalArgumentException.class, () -> new HomepageContentStore.RssConfiguration("https://feed.invalid", "tabs"));
    assertThrows(IllegalArgumentException.class, () -> new HomepageContentStore.RssConfiguration("rssAgregator12", "unknown"));
  }

  @Test
  void communityWidgetsPreserveLegacyDefaultsAndPersistIndependentInstances() throws Exception {
    var store = new HomepageContentStore(root);
    var helper = helper();
    var layout = new HomepageContentStore.Layout("main", 12, false);
    var faq = HomepageWidgets.communityWidget(store, page, "faq-legacy", "fr", helper, "faq");
    var users = HomepageWidgets.communityWidget(store, page, "users-legacy", "fr", helper, "users");
    assertEquals(new HomepageContentStore.FaqConfiguration("", 1, "random"), faq.faqConfiguration());
    assertEquals(new HomepageContentStore.UsersConfiguration("", 5, true, "title phone fonction"), users.usersConfiguration());
    assertFalse(Files.exists(root.resolve("skins")));
    for (String id : List.of("faq-legacy", "faq-extra")) {
      store.saveCommunityWidget(page, id, "fr", "Questions", layout, "faq",
          new HomepageContentStore.FaqConfiguration("questionReply34", 3, "first"), null, "");
    }
    for (String id : List.of("users-legacy", "users-extra")) {
      store.saveCommunityWidget(page, id, "fr", "Bienvenue", layout, "users", null,
          new HomepageContentStore.UsersConfiguration("34", 4, false, "phone"), "");
    }
    var restored = new HomepageContentStore(root);
    var saved = restored.readWidget(page, "users-extra", "fr");
    assertEquals(new HomepageContentStore.UsersConfiguration("34", 4, false, "phone"), saved.usersConfiguration());
    assertEquals(new HomepageContentStore.FaqConfiguration("questionReply34", 3, "first"),
        restored.readWidget(page, "faq-extra", "fr").faqConfiguration());
    assertEquals(1, HomepageWidgets.all(store, page, "fr", helper).stream().filter(w -> w.id().equals("faq-legacy")).count());
    assertEquals(1, HomepageWidgets.all(store, page, "fr", helper).stream().filter(w -> w.id().equals("users-legacy")).count());
    HomepageWidgets.questions(restored.widgets(page, "fr"), helper);
    HomepageWidgets.users(restored.widgets(page, "fr"), helper);
    verify(helper, never()).getConfiguredQuestions(any());
    verify(helper, never()).getConfiguredNewUsers(any());
    var visible = new HomepageContentStore.Layout("right", 4, true);
    store.saveCommunityWidget(page, "faq-visible", "fr", "Questions", visible, "faq", faq.faqConfiguration(), null, "");
    store.saveCommunityWidget(page, "users-visible", "fr", "Bienvenue", visible, "users", null, users.usersConfiguration(), "");
    HomepageWidgets.questions(store.widgets(page, "fr"), helper);
    HomepageWidgets.users(store.widgets(page, "fr"), helper);
    verify(helper).getConfiguredQuestions(faq.faqConfiguration());
    verify(helper).getConfiguredNewUsers(users.usersConfiguration());
    store.saveCommunityWidget(page, saved.id(), "fr", "Bienvenue", layout, "users", null,
        new HomepageContentStore.UsersConfiguration("34", 4, true, "phone"), saved.content().revision());
    assertNotEquals(saved.content().revision(), store.read(page, saved.id(), "fr").revision());
    assertThrows(ConcurrentModificationException.class, () -> store.saveCommunityWidget(page, saved.id(), "fr",
        "Bienvenue", layout, "users", null, saved.usersConfiguration(), saved.content().revision()));
    for (String id : List.of("faq-legacy", "users-legacy")) {
      assertThrows(IllegalArgumentException.class, () -> store.save(page, id, "fr", "", "", ""));
      assertThrows(IllegalArgumentException.class, () -> store.saveEphemerisWidget(page, id, "fr", "", layout, ""));
      assertThrows(IllegalArgumentException.class, () -> store.saveNewsWidget(page, id, "fr", "", layout, news(""), 0));
      assertThrows(IllegalArgumentException.class, () -> store.saveFeedWidget(page, id, "fr", "", layout, "rss", null,
          new HomepageContentStore.RssConfiguration("", "tabs"), ""));
      assertThrows(IllegalArgumentException.class, () -> store.savePublicationsWidget(page, id, "fr", "", layout,
          new HomepageContentStore.PublicationsConfiguration("", 3, true), ""));
    }
    assertThrows(IllegalArgumentException.class, () -> store.saveCommunityWidget(page, "events-legacy", "fr", "", layout,
        "faq", faq.faqConfiguration(), null, ""));
    assertThrows(IllegalArgumentException.class, () -> store.saveCommunityWidget(page, "faq-legacy", "fr", "", layout,
        "users", null, users.usersConfiguration(), ""));
    assertThrows(IllegalArgumentException.class, () -> new HomepageContentStore.FaqConfiguration("questionreply12", 2, "random"));
    assertThrows(IllegalArgumentException.class, () -> new HomepageContentStore.FaqConfiguration("questionReply12", 0, "random"));
    assertThrows(IllegalArgumentException.class, () -> new HomepageContentStore.FaqConfiguration("", 1, "bad"));
    assertThrows(IllegalArgumentException.class, () -> new HomepageContentStore.FaqConfiguration("", 1, null));
    assertThrows(IllegalArgumentException.class, () -> new HomepageContentStore.UsersConfiguration("group12", 5, true, "phone"));
    assertThrows(IllegalArgumentException.class, () -> new HomepageContentStore.UsersConfiguration("", -1, true, "phone"));
    assertThrows(IllegalArgumentException.class, () -> new HomepageContentStore.UsersConfiguration("", 5, true, "phone\" onclick=bad"));
  }

  @Test
  void subscriptionsPersistIndependentSettingsAndReplaceLegacyWithoutFetchingHiddenContent() throws Exception {
    var store = new HomepageContentStore(root);
    var helper = helper();
    var legacy = HomepageWidgets.subscriptionsWidget(store, page, "subscriptions-legacy", "fr", helper);
    assertEquals(helper.getDefaultSubscriptionsConfiguration(), legacy.subscriptionsConfiguration());
    assertTrue(legacy.isEnabled());
    when(helper.getLookSettings().isDisplayLastSubscribedNews()).thenReturn(false);
    assertTrue(HomepageWidgets.subscriptionsWidget(store, page, legacy.id(), "fr", helper).isEnabled());
    assertFalse(Files.exists(root.resolve("skins")));
    var configuration = new HomepageContentStore.SubscriptionsConfiguration(7, false);
    var layout = new HomepageContentStore.Layout("right", 8, false);
    for (String id : List.of(legacy.id(), "subscriptions-extra")) {
      store.saveSubscriptionsWidget(page, id, "fr", "Mes abonnements", layout, configuration, "");
    }
    var restored = new HomepageContentStore(root);
    var saved = restored.readWidget(page, "subscriptions-extra", "fr");
    assertEquals(configuration, saved.subscriptionsConfiguration());
    assertEquals(layout, saved.content().layout());
    assertEquals(1, HomepageWidgets.all(store, page, "fr", helper).stream().filter(w -> w.id().equals(legacy.id())).count());
    HomepageWidgets.subscriptions(restored.widgets(page, "fr"), helper);
    verify(helper, never()).getConfiguredSubscriptions(any());
    store.saveSubscriptionsWidget(page, saved.id(), "fr", "Mes abonnements", layout,
        new HomepageContentStore.SubscriptionsConfiguration(8, true), saved.content().revision());
    assertNotEquals(saved.content().revision(), store.read(page, saved.id(), "fr").revision());
    assertThrows(ConcurrentModificationException.class, () -> store.saveSubscriptionsWidget(page, saved.id(), "fr",
        "", layout, configuration, saved.content().revision()));
    store.saveSubscriptionsWidget(page, "subscriptions-visible", "fr", "", new HomepageContentStore.Layout("main", 3, true), configuration, "");
    HomepageWidgets.subscriptions(store.widgets(page, "fr"), helper);
    verify(helper).getConfiguredSubscriptions(configuration);
    assertThrows(IllegalArgumentException.class, () -> store.save(page, legacy.id(), "fr", "", "", ""));
    assertThrows(IllegalArgumentException.class, () -> store.saveEphemerisWidget(page, legacy.id(), "fr", "", layout, ""));
    assertThrows(IllegalArgumentException.class, () -> store.saveNewsWidget(page, legacy.id(), "fr", "", layout, news(""), 0));
    assertThrows(IllegalArgumentException.class, () -> store.saveFeedWidget(page, legacy.id(), "fr", "", layout, "rss", null,
        new HomepageContentStore.RssConfiguration("", "tabs"), ""));
    assertThrows(IllegalArgumentException.class, () -> store.saveCommunityWidget(page, legacy.id(), "fr", "", layout,
        "faq", new HomepageContentStore.FaqConfiguration("", 1, "random"), null, ""));
    assertThrows(IllegalArgumentException.class, () -> store.savePublicationsWidget(page, legacy.id(), "fr", "", layout,
        new HomepageContentStore.PublicationsConfiguration("", 3, true), ""));
    for (String id : List.of("faq-legacy", "users-legacy", "events-legacy", "rss-legacy", "ephemeris-legacy", "publications-legacy", "news-legacy-1")) {
      assertThrows(IllegalArgumentException.class, () -> store.saveSubscriptionsWidget(page, id, "fr", "", layout, configuration, ""));
    }
    assertThrows(IllegalArgumentException.class, () -> new HomepageContentStore.SubscriptionsConfiguration(0, true));
    assertThrows(IllegalArgumentException.class, () -> new HomepageContentStore.SubscriptionsConfiguration(101, true));
  }


  @Test
  void homeShortcutsUseOnlyHomeDefaultsAndSaveIndependentOrderedInstances() throws Exception {
    var store = new HomepageContentStore(root);
    var helper = helper();
    var legacy = HomepageWidgets.shortcutsWidget(store, page, "shortcuts-legacy", "fr", helper);
    assertTrue(legacy.shortcutsConfiguration().entries().isEmpty());
    verify(helper, never()).getShortcuts("home");
    assertEquals("main", legacy.getZone());
    assertEquals(0, legacy.getOrder());
    assertFalse(Files.exists(root.resolve("skins")));
    verify(helper, never()).getToolsShortcuts();
    verify(helper, never()).getShortcuts("tool");
    var layout = new HomepageContentStore.Layout("right", 12, false);
    var first = new HomepageContentStore.HomeShortcut("A & B", "https://example.org/?a=1&b=2", "", "_blank");
    var second = new HomepageContentStore.HomeShortcut("Autre", "../other", "/weblib/icon.png", "bottomFrame");
    var config = new HomepageContentStore.ShortcutsConfiguration(List.of(first, second));
    store.saveShortcutsWidget(page, legacy.id(), "fr", "Liens", layout, config, "");
    store.saveShortcutsWidget(page, "shortcuts-other", "fr", "Autres liens", layout,
        new HomepageContentStore.ShortcutsConfiguration(List.of(second)), "");
    var reopened = new HomepageContentStore(root);
    var saved = reopened.readWidget(page, legacy.id(), "fr");
    assertEquals(config, saved.shortcutsConfiguration());
    assertEquals(layout, saved.content().layout());
    assertEquals(1, HomepageWidgets.all(store, page, "fr", helper).stream().filter(w -> w.id().equals(legacy.id())).count());
    assertEquals(List.of(second), reopened.readWidget(page, "shortcuts-other", "fr").shortcutsConfiguration().entries());
    store.saveShortcutsWidget(page, legacy.id(), "fr", "Liens", layout,
        new HomepageContentStore.ShortcutsConfiguration(List.of(second, first)), saved.content().revision());
    var reordered = store.readWidget(page, legacy.id(), "fr");
    assertNotEquals(saved.content().revision(), reordered.content().revision());
    assertEquals(List.of(second, first), reordered.shortcutsConfiguration().entries());
    assertThrows(ConcurrentModificationException.class, () -> store.saveShortcutsWidget(page, legacy.id(), "fr", "", layout,
        config, saved.content().revision()));
    store.saveShortcutsWidget(page, legacy.id(), "fr", "Liens", layout,
        new HomepageContentStore.ShortcutsConfiguration(List.of()), reordered.content().revision());
    assertTrue(reopened.readWidget(page, legacy.id(), "fr").shortcutsConfiguration().entries().isEmpty());
    assertFalse(Files.readString(root.resolve("skins/waves/page.properties")).contains("widget.shortcuts-legacy.entry."));
  }

  @Test
  void shortcutsRejectInvalidLinksAndCrossTypeConversionsIncludingReservedIds() throws Exception {
    var store = new HomepageContentStore(root);
    var layout = new HomepageContentStore.Layout("main", 0, true);
    var config = new HomepageContentStore.ShortcutsConfiguration(List.of());
    for (String id : List.of("shortcuts-legacy", "shortcuts-extra")) {
      store.saveShortcutsWidget(page, id, "fr", "", layout, config, "");
      assertThrows(IllegalArgumentException.class, () -> store.save(page, id, "fr", "", "", ""));
      assertThrows(IllegalArgumentException.class, () -> store.saveEphemerisWidget(page, id, "fr", "", layout, ""));
      assertThrows(IllegalArgumentException.class, () -> store.saveNewsWidget(page, id, "fr", "", layout, news(""), 0));
      assertThrows(IllegalArgumentException.class, () -> store.savePublicationsWidget(page, id, "fr", "", layout,
          new HomepageContentStore.PublicationsConfiguration("", 3, true), ""));
      assertThrows(IllegalArgumentException.class, () -> store.saveFeedWidget(page, id, "fr", "", layout, "rss", null,
          new HomepageContentStore.RssConfiguration("", "tabs"), ""));
      assertThrows(IllegalArgumentException.class, () -> store.saveCommunityWidget(page, id, "fr", "", layout, "faq",
          new HomepageContentStore.FaqConfiguration("", 1, "random"), null, ""));
      assertThrows(IllegalArgumentException.class, () -> store.saveSubscriptionsWidget(page, id, "fr", "", layout,
          new HomepageContentStore.SubscriptionsConfiguration(4, true), ""));
    }
    for (String id : List.of("subscriptions-legacy", "faq-legacy", "users-legacy", "events-legacy", "rss-legacy", "ephemeris-legacy", "publications-legacy", "news-legacy-1")) {
      assertThrows(IllegalArgumentException.class, () -> store.saveShortcutsWidget(page, id, "fr", "", layout, config, ""));
    }
    store.save(page, "html", "fr", "", "", "");
    assertThrows(IllegalArgumentException.class, () -> store.saveShortcutsWidget(page, "html", "fr", "", layout, config, ""));
    for (String url : List.of("", "javascript:alert(1)", "JaVaScRiPt:alert(1)", "data:text/html,hello", "vbscript:foo", "https://example.org/\nfoo")) {
      assertThrows(IllegalArgumentException.class, () -> new HomepageContentStore.HomeShortcut("Lien", url, "", ""));
    }
    assertThrows(IllegalArgumentException.class, () -> new HomepageContentStore.HomeShortcut("Lien", "/ok", "data:image/svg+xml,foo", ""));
    assertThrows(IllegalArgumentException.class, () -> new HomepageContentStore.HomeShortcut("Lien", "/ok", "", "bad target"));
    assertThrows(IllegalArgumentException.class, () -> new HomepageContentStore.ShortcutsConfiguration(java.util.Collections.nCopies(101,
        new HomepageContentStore.HomeShortcut("", "#", "", ""))));
    for (String url : List.of("https://example.org", "http://example.org", "/silverpeas/Main", "../Main", "#anchor", "mailto:contact@example.org")) {
      assertDoesNotThrow(() -> new HomepageContentStore.HomeShortcut("", url, "", "bottomFrame"));
    }
  }
  @Test
  void searchIsOneManagedTypeWithTwoModes() throws Exception {
    var store = new HomepageContentStore(root);
    var helper = helper();
    var configuration = new HomepageContentStore.SearchConfiguration("advanced", "document.xml", true, true, false);
    when(helper.getDefaultSearchConfiguration()).thenReturn(configuration);
    var initial = HomepageWidgets.all(store, page, "fr", helper);
    assertTrue(initial.isEmpty());
    var legacy = HomepageWidgets.searchWidget(store, page, "search-legacy", "fr", helper);
    assertTrue(legacy.isEnabled());
    assertEquals(new HomepageContentStore.SearchConfiguration("taxonomy", "", false, false, false), legacy.searchConfiguration());
    assertFalse(Files.exists(root.resolve("skins/waves/page.properties")));
    var layout = new HomepageContentStore.Layout("main", 4, false);
    store.saveSearchWidget(page, legacy.id(), "fr", "Recherche", layout, configuration, "");
    var reopened = new HomepageContentStore(root);
    var saved = reopened.readWidget(page, legacy.id(), "fr");
    assertEquals(configuration, saved.searchConfiguration());
    var taxonomy = new HomepageContentStore.SearchConfiguration("taxonomy", "", false, false, true);
    reopened.saveSearchWidget(page, legacy.id(), "fr", "Recherche", layout, taxonomy, saved.content().revision());
    assertNotEquals(saved.content().revision(), reopened.read(page, legacy.id(), "fr").revision());
    assertThrows(ConcurrentModificationException.class, () -> reopened.saveSearchWidget(page,
        legacy.id(), "fr", "", layout, configuration, saved.content().revision()));
    reopened.saveSearchWidget(page, "search-extra", "fr", "Autre recherche", layout, configuration, "");
    assertEquals(2, HomepageWidgets.all(reopened, page, "fr", helper).stream()
        .filter(w -> "search".equals(w.type())).count());
    assertEquals(taxonomy, reopened.readWidget(page, legacy.id(), "fr").searchConfiguration());
    assertThrows(IllegalArgumentException.class, () -> reopened.save(page, legacy.id(), "fr", "", "", ""));
    assertThrows(IllegalArgumentException.class, () -> reopened.saveSearchWidget(page, "faq-legacy", "fr", "", layout, taxonomy, ""));
    store.save(page, "html-test", "fr", "", "", "");
    assertThrows(IllegalArgumentException.class, () -> store.saveSearchWidget(page, "html-test", "fr", "", layout, taxonomy, ""));
    assertThrows(IllegalArgumentException.class, () -> store.saveShortcutsWidget(page, "search-legacy", "fr", "", layout,
        new HomepageContentStore.ShortcutsConfiguration(List.of()), ""));
    for (String template : List.of("../secret.xml", "/secret.xml", "https://test/form.xml", "bad.xml/other", "x..xml")) {
      assertThrows(IllegalArgumentException.class, () -> new HomepageContentStore.SearchConfiguration("advanced", template, false, false, false));
    }
    assertThrows(IllegalArgumentException.class, () -> new HomepageContentStore.SearchConfiguration("advanced", "", false, false, false));
    assertThrows(IllegalArgumentException.class, () -> new HomepageContentStore.SearchConfiguration("other", "", false, false, false));
  }

  @Test
  void bookmarksMigrateWithoutWritingAndKeepIndependentEmptyStateOptions() throws Exception {
    var store = new HomepageContentStore(root);
    var helper = helper();
    when(helper.getLookSettings().isDisplayBookmarksAreaWhenEmpty()).thenReturn(true);
    when(helper.getLookSettings().getNoBookmarksFragmentURL()).thenReturn("/silverpeas/empty.html");
    var legacy = HomepageWidgets.bookmarksWidget(store, page, "bookmarks-legacy", "fr", helper);
    assertEquals(new HomepageContentStore.BookmarksConfiguration(true, ""), legacy.bookmarksConfiguration());
    assertEquals("right", legacy.getZone());
    assertTrue(legacy.isEnabled());
    assertFalse(Files.exists(root.resolve("skins")));
    verify(helper, never()).getBookmarks();
    var layout = new HomepageContentStore.Layout("main", 5, true);
    var config = new HomepageContentStore.BookmarksConfiguration(false, "");
    store.saveBookmarksWidget(page, legacy.id(), "fr", "Mes favoris", layout, config, "");
    store.saveBookmarksWidget(page, "bookmarks-other", "fr", "Autres favoris", layout, legacy.bookmarksConfiguration(), "");
    var reopened = new HomepageContentStore(root);
    var saved = reopened.readWidget(page, legacy.id(), "fr");
    assertEquals(config, saved.bookmarksConfiguration());
    assertEquals(layout, saved.content().layout());
    assertEquals(1, HomepageWidgets.all(reopened, page, "fr", helper).stream().filter(w -> legacy.id().equals(w.id())).count());
    reopened.saveBookmarksWidget(page, legacy.id(), "fr", "Mes favoris", layout,
        new HomepageContentStore.BookmarksConfiguration(true, ""), saved.content().revision());
    var revision = reopened.read(page, legacy.id(), "fr").revision();
    assertNotEquals(saved.content().revision(), revision);
    reopened.saveBookmarksWidget(page, legacy.id(), "fr", "Mes favoris", layout,
        new HomepageContentStore.BookmarksConfiguration(true, "/new.html"), revision);
    assertNotEquals(revision, reopened.read(page, legacy.id(), "fr").revision());
    assertThrows(ConcurrentModificationException.class, () -> reopened.saveBookmarksWidget(page, legacy.id(), "fr", "", layout, config, revision));
    assertEquals(legacy.bookmarksConfiguration(), reopened.readWidget(page, "bookmarks-other", "fr").bookmarksConfiguration());
  }

  @Test
  void bookmarksRejectTypeChangesAndExecutableFragmentUrls() throws Exception {
    var store = new HomepageContentStore(root);
    var layout = new HomepageContentStore.Layout("right", 40, true);
    var config = new HomepageContentStore.BookmarksConfiguration(false, "");
    assertThrows(IllegalArgumentException.class, () -> store.save(page, "bookmarks-legacy", "fr", "", "", ""));
    store.saveBookmarksWidget(page, "bookmarks-legacy", "fr", "", layout, config, "");
    assertThrows(IllegalArgumentException.class, () -> store.saveEphemerisWidget(page, "bookmarks-legacy", "fr", "", layout, ""));
    assertThrows(IllegalArgumentException.class, () -> store.saveNewsWidget(page, "bookmarks-legacy", "fr", "", layout, news(""), 0));
    assertThrows(IllegalArgumentException.class, () -> store.saveSearchWidget(page, "bookmarks-legacy", "fr", "", layout,
        new HomepageContentStore.SearchConfiguration("taxonomy", "", false, false, false), ""));
    for (String id : List.of("search-legacy", "shortcuts-legacy", "subscriptions-legacy", "faq-legacy", "users-legacy", "events-legacy", "rss-legacy", "ephemeris-legacy", "publications-legacy", "news-legacy-1")) {
      assertThrows(IllegalArgumentException.class, () -> store.saveBookmarksWidget(page, id, "fr", "", layout, config, ""));
    }
    store.save(page, "html-test", "fr", "", "", "");
    assertThrows(IllegalArgumentException.class, () -> store.saveBookmarksWidget(page, "html-test", "fr", "", layout, config, ""));
    for (String url : List.of("javascript:alert(1)", "data:text/html,hello", "mailto:a@example.org", "https://example.org/\n")) {
      assertThrows(IllegalArgumentException.class, () -> new HomepageContentStore.BookmarksConfiguration(true, url));
    }
  }

  @Test
  void weatherStoresOnlyPlacementAndKeepsLegacyVisibilityWithoutDuplicatingIt() throws Exception {
    var store = new HomepageContentStore(root);
    var helper = helper();
    when(helper.getWeatherCities()).thenReturn(List.of(
        new org.silverpeas.looks.aurora.service.weather.City("2988507", "Paris")));
    var legacy = HomepageWidgets.weatherWidget(store, page, "weather-legacy", "fr", helper);
    assertTrue(legacy.isEnabled());
    assertFalse(Files.exists(root.resolve("skins")));
    var layout = new HomepageContentStore.Layout("main", 12, true);
    store.saveWeatherWidget(page, legacy.id(), "fr", "Météo", layout, "");
    var file = root.resolve("skins/waves/page.properties");
    Files.writeString(file, "\nwidget.weather-legacy.cities=2988507|Obsolete\n", java.nio.file.StandardOpenOption.APPEND);
    String revision = store.read(page, legacy.id(), "fr").revision();
    store.saveWeatherWidget(page, legacy.id(), "fr", "Météo", layout, revision);
    assertFalse(Files.readString(file).contains(".cities"));

    var saved = new HomepageContentStore(root).readWidget(page, legacy.id(), "fr");
    assertEquals("main", saved.getZone());
    assertEquals(1, HomepageWidgets.all(store, page, "fr", helper).stream()
        .filter(widget -> widget.id().equals(legacy.id())).count());
    store.saveWeatherWidget(page, "weather-second", "fr", "Sud", layout, "");
    store.saveWeatherWidget(page, legacy.id(), "fr", "Météo 2", layout, saved.content().revision());
    assertThrows(ConcurrentModificationException.class, () -> store.saveWeatherWidget(page,
        legacy.id(), "fr", "Météo", layout, saved.content().revision()));
    assertThrows(IllegalArgumentException.class, () -> store.saveConfigured(page,
        legacy.id(), "fr", "", "", "", layout));
    assertThrows(IllegalArgumentException.class, () -> store.saveEphemerisWidget(page,
        "weather-legacy", "fr", "", layout, ""));
    assertThrows(IllegalArgumentException.class, () -> store.saveWeatherWidget(page,
        "bookmarks-legacy", "fr", "", layout, ""));
    assertFalse(Files.readString(root.resolve("skins/waves/page.properties")).contains("Paris"));
    when(helper.getWeatherCities()).thenReturn(List.of());
    assertTrue(HomepageWidgets.weatherWidget(store, page, legacy.id(), "fr", helper).isEnabled());
    assertTrue(HomepageWidgets.weatherWidget(store, page, "weather-new", "fr", helper).isEnabled());
  }
}
