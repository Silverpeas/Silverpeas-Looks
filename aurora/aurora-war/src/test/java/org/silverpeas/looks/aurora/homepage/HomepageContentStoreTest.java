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
}
