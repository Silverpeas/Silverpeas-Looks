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

import java.util.regex.Pattern;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

import static org.owasp.html.Sanitizers.*;

/** HTML policy for free zones, including sandboxed embedded pages. */
final class HomepageContentSanitizer {
  private static final Pattern INLINE_IMAGE = Pattern.compile(
      "(?i)^data:image/(?:png|jpeg|gif|webp);base64,[a-z0-9+/]+={0,2}$");
  private static final Pattern DIMENSION = Pattern.compile("[0-9]+%?");
  private static final PolicyFactory IMAGES = new HtmlPolicyBuilder()
      .allowUrlProtocols("http", "https", "data")
      .allowElements("img")
      .allowAttributes("alt").onElements("img")
      .allowAttributes("src").matching((element, attribute, value) ->
          value.startsWith("data:") && !INLINE_IMAGE.matcher(value).matches() ? null : value)
      .onElements("img")
      .allowAttributes("border", "height", "width").matching(Pattern.compile("[0-9]+"))
      .onElements("img").toFactory();
  private static final PolicyFactory IFRAMES = new HtmlPolicyBuilder()
      .allowUrlProtocols("http", "https")
      .allowElements((element, attributes) -> {
        // Always impose these restrictions, including on HTML pasted in Source mode.
        attributes.add("sandbox");
        attributes.add("allow-scripts allow-forms allow-popups");
        attributes.add("loading");
        attributes.add("lazy");
        attributes.add("referrerpolicy");
        attributes.add("no-referrer");
        return element;
      }, "iframe")
      .allowAttributes("src").matching((element, attribute, value) -> {
        String source = value.strip();
        return source.matches("(?i)^https?://[^\\s]+$")
            || source.startsWith("/") && !source.startsWith("//") ? source : null;
      }).onElements("iframe")
      .allowAttributes("title").onElements("iframe")
      .allowAttributes("width", "height").matching(DIMENSION).onElements("iframe")
      .allowAttributes("allowfullscreen").onElements("iframe")
      .toFactory();
  private static final PolicyFactory POLICY = FORMATTING.and(BLOCKS).and(LINKS).and(STYLES)
      .and(TABLES).and(IMAGES).and(IFRAMES);

  private HomepageContentSanitizer() {
  }

  static String sanitize(String html) {
    return POLICY.sanitize(html);
  }
}
