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
    when(helper.getDefaultNewsConfiguration(anyInt())).thenReturn(
        new HomepageContentStore.NewsConfiguration("*", 10, false, "LIST", true, ""));
    return helper;
  }

  private HomepageContentStore.NewsConfiguration news(String revision) {
    return new HomepageContentStore.NewsConfiguration("*", 5, false, "LIST", true, revision);
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
  void legacyNewsIsPresentedWithoutWritingOrDuplicatingIt() throws Exception {
    var store = new HomepageContentStore(root);
    var helper = helper();
    var initial = HomepageWidgets.all(store, page, "fr", helper);
    assertEquals(3, initial.size());
    assertFalse(Files.exists(root.resolve("skins")));
    store.saveNews(page, 2, new HomepageContentStore.NewsConfiguration("delegated", 4, false, "LIST", true, ""));
    var legacy = HomepageWidgets.newsWidget(store, page, "news-legacy-2", "fr", helper);
    assertEquals("delegated", legacy.newsConfiguration().source());
    store.saveNewsWidget(page, legacy.id(), "fr", "Équipe",
        new HomepageContentStore.Layout("right", 1, false), news(""), 2);
    var changed = HomepageWidgets.all(store, page, "fr", helper);
    assertEquals(3, changed.size());
    assertEquals("news-legacy-2", changed.getFirst().id());
    assertFalse(changed.getFirst().isEnabled());
    assertEquals("delegated", store.readNews(page, 2).source());
    assertThrows(IllegalArgumentException.class, () -> store.save(page, "news-legacy-1", "fr", "", "", ""));
  }

  @Test
  void rendererSkipsHiddenWidgetsAndKeepsLegacyFallbackForUnmodifiedBlocks() throws Exception {
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
    verify(helper).getConfiguredNews(1, null);
    verify(helper).getConfiguredNews(2, null);
    verify(helper).getConfiguredNews(3, null);
    assertEquals(4, rendered.size());
  }
}
