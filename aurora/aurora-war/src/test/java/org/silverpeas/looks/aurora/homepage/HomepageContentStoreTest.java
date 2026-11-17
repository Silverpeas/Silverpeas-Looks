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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ConcurrentModificationException;

import static org.junit.jupiter.api.Assertions.*;

class HomepageContentStoreTest {
  @TempDir
  Path root;

  @Test
  void newsConfigurationIsPersistentIsolatedAndDoesNotCreateHtmlWidgets() throws Exception {
    var store = new HomepageContentStore(root);
    var page = new HomepagePage("skins", "waves");
    assertNull(store.readNews(page, 1));
    store.save(page, "welcome", "fr", "Bienvenue", "Bonjour", "");
    var configuration = new HomepageContentStore.NewsConfiguration("quickinfo12 quickinfo34",
        5, true, "LIST", false, "");
    store.saveNews(page, 1, configuration);
    var reopened = new HomepageContentStore(root);
    var saved = reopened.readNews(page, 1);
    assertEquals("quickinfo12 quickinfo34", saved.source());
    assertEquals(5, saved.size());
    assertTrue(saved.importantOnly());
    assertFalse(saved.enabled());
    assertEquals("LIST", saved.rendering());
    assertFalse(saved.revision().isBlank());
    assertNull(reopened.readNews(page, 2));
    assertNull(reopened.readNews(new HomepagePage("skins", "Aurora"), 1));
    assertEquals(1, reopened.widgets(page, "fr").size());
    assertThrows(ConcurrentModificationException.class, () -> store.saveNews(page, 1, configuration));
    store.saveNews(page, 1, new HomepageContentStore.NewsConfiguration("*", -1, false,
        "CAROUSEL", true, saved.revision()));
    assertEquals(-1, reopened.readNews(page, 1).size());
  }

  @Test
  void invalidNewsConfigurationIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> new HomepageContentStore.NewsConfiguration(
        "../quickinfo", 10, false, "LIST", true, ""));
    for (int size : new int[]{-2, 0, 101}) {
      assertThrows(IllegalArgumentException.class, () -> new HomepageContentStore.NewsConfiguration(
          "*", size, false, "LIST", true, ""));
    }
    assertThrows(IllegalArgumentException.class, () -> new HomepageContentStore.NewsConfiguration(
        "*", 10, false, "invalid", true, ""));
    assertThrows(IllegalArgumentException.class, () -> new HomepageContentStore(root)
        .readNews(new HomepagePage("skins", "waves"), 4));
  }

  @Test
  void contentSurvivesReopeningAndIsIsolatedByOwnerWidgetAndLanguage() throws Exception {
    var store = new HomepageContentStore(root);
    var page = new HomepagePage("skins", "waves");
    store.save(page, "welcome", "fr", "Bienvenue", "<p>Bonjour</p>", "");
    var reopened = new HomepageContentStore(root);
    assertEquals("<p>Bonjour</p>", reopened.read(page, "welcome", "fr").html());
    assertEquals("Bienvenue", reopened.read(page, "welcome", "fr").title());
    assertEquals("", reopened.read(page, "welcome", "en").html());
    assertEquals("", reopened.read(page, "other", "fr").html());
    assertEquals("", reopened.read(new HomepagePage("spaces", "WA123"), "welcome", "fr").html());
    assertTrue(Files.exists(root.resolve("skins/waves/page.properties")));
  }

  @Test
  void savingAnotherWidgetPreservesExistingPageProperties() throws Exception {
    var store = new HomepageContentStore(root);
    var page = new HomepagePage("spaces", "WA123");
    store.save(page, "first", "fr", "Premier", "un", "");
    store.save(page, "second", "fr", "Second", "deux", "");
    assertEquals("Premier", store.read(page, "first", "fr").title());
    assertEquals("Second", store.read(page, "second", "fr").title());
  }

  @Test
  void staleEditorCannotOverwriteAnotherSave() throws Exception {
    var store = new HomepageContentStore(root);
    var page = new HomepagePage("skins", "waves");
    store.save(page, "welcome", "fr", "Titre", "initial", "");
    var old = store.read(page, "welcome", "fr");
    store.save(page, "welcome", "fr", "Titre", "new", old.revision());
    assertThrows(ConcurrentModificationException.class,
        () -> store.save(page, "welcome", "fr", "Titre", "stale", old.revision()));
    assertEquals("new", store.read(page, "welcome", "fr").html());
  }

  @Test
  void rejectsTraversalAndSymbolicLinks() throws Exception {
    var store = new HomepageContentStore(root);
    assertThrows(IllegalArgumentException.class, () -> new HomepagePage("skins", "../private"));
    assertThrows(IllegalArgumentException.class, () -> new HomepagePage("other", "waves"));
    var page = new HomepagePage("skins", "waves");
    assertThrows(IllegalArgumentException.class, () -> store.read(page, "../escape", "fr"));
    Files.createDirectories(root.resolve("skins"));
    Path outside = Files.createTempDirectory(root, "outside");
    Files.createSymbolicLink(root.resolve("skins/waves"), outside);
    assertThrows(java.io.IOException.class, () -> store.read(page, "welcome", "fr"));
  }
  @Test
  void placementSortingAndConflictsSurviveReopening() throws Exception {
    var store = new HomepageContentStore(root);
    var page = new HomepagePage("skins", "Aurora");
    store.saveConfigured(page, "last", "fr", "Dernier", "trois", "",
        new HomepageContentStore.Layout("right", 30, false));
    store.saveConfigured(page, "second", "fr", "Second", "deux", "",
        new HomepageContentStore.Layout("main", 10, true));
    store.saveConfigured(page, "first", "fr", "Premier", "un", "",
        new HomepageContentStore.Layout("right", 10, true));
    var widgets = new HomepageContentStore(root).widgets(page, "fr");
    assertEquals(java.util.List.of("first", "second", "last"),
        widgets.stream().map(HomepageContentStore.Widget::getId).toList());
    assertFalse(widgets.get(2).isEnabled());
    assertEquals("right", widgets.get(0).getZone());
    assertEquals("un", widgets.get(0).getHtml());
    var old = store.read(page, "first", "fr");
    var layout = new HomepageContentStore.Layout("main", 42, false);
    store.saveConfigured(page, "first", "fr", old.title(), old.html(), old.revision(), layout);
    assertThrows(ConcurrentModificationException.class,
        () -> store.save(page, "first", "fr", "ancien", "ancien", old.revision()));
    var current = store.read(page, "first", "fr");
    store.save(page, "first", "fr", "nouveau", "nouveau", current.revision());
    assertEquals(layout, store.read(page, "first", "fr").layout());
    assertEquals("deux", store.read(page, "second", "fr").html());
  }

  @Test
  void emptyWidgetsAlsoHaveARevisionAndCannotBeCreatedTwice() throws Exception {
    var store = new HomepageContentStore(root);
    var page = new HomepagePage("skins", "Aurora");
    store.save(page, "empty", "fr", "", "", "");
    assertFalse(store.read(page, "empty", "fr").revision().isEmpty());
    assertThrows(ConcurrentModificationException.class,
        () -> store.save(page, "empty", "fr", "", "", ""));
  }

  @Test
  void rejectsInvalidPlacement() {
    assertThrows(IllegalArgumentException.class,
        () -> new HomepageContentStore.Layout("third", 10, true));
    assertThrows(IllegalArgumentException.class,
        () -> new HomepageContentStore.Layout("main", -1, true));
    assertThrows(IllegalArgumentException.class,
        () -> new HomepageContentStore.Layout("main", 10001, true));
  }

}
