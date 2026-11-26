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

import static org.junit.jupiter.api.Assertions.*;

class HomepageContentSanitizerTest {
  @Test
  void keepsEmbeddedPageAndImposesSandbox() {
    String html = HomepageContentSanitizer.sanitize(
        "<iframe src=\"https://www.youtube.com/embed/test\" title=\"Vidéo\" width=\"100%\" height=\"315\" sandbox=\"allow-same-origin\" onload=\"alert(1)\"></iframe>");
    assertTrue(html.contains("https://www.youtube.com/embed/test"));
    assertTrue(html.contains("title=\"Vidéo\""));
    assertTrue(html.contains("width=\"100%\""));
    assertTrue(html.contains("sandbox=\"allow-scripts allow-forms allow-popups\""));
    assertFalse(html.contains("allow-same-origin"));
    assertFalse(html.contains("onload"));
  }

  @Test
  void rejectsExecutableSourcesAndInlineDocuments() {
    for (String source : new String[]{"javascript:alert(1)", "data:text/html,test",
        "data:image/png;base64,AAAA", " data:text/html,test", "//example.org/embed"}) {
      String html = HomepageContentSanitizer.sanitize(
          "<iframe src=\"" + source + "\" srcdoc=\"test\"></iframe><script>alert(1)</script>");
      assertFalse(html.contains("src="), html);
      assertFalse(html.contains("srcdoc"), html);
      assertFalse(html.contains("<script"), html);
    }
  }

  @Test
  void keepsExistingFormattingAndImages() {
    String html = HomepageContentSanitizer.sanitize(
        "<p><strong>Bonjour</strong><img src=\"/silverpeas/image.png\" alt=\"Image\"><img src=\"data:image/png;base64,AAAA\"></p>");
    assertTrue(html.contains("<strong>Bonjour</strong>"));
    assertTrue(html.contains("/silverpeas/image.png"));
    assertTrue(html.contains("data:image/png;base64,AAAA"));
  }
}
