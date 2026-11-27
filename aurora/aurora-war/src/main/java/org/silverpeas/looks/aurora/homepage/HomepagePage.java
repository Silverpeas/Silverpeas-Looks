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

/** The stable owner of a homepage, independent of widget titles and positions. */
public record HomepagePage(String kind, String owner) {
  public HomepagePage {
    if (!"skins".equals(kind) && !"spaces".equals(kind)) {
      throw new IllegalArgumentException("Unknown homepage kind");
    }
    if ("spaces".equals(kind)) {
      if (owner == null || !owner.matches("(?:WA)?[0-9]+")) {
        throw new IllegalArgumentException("Invalid space identifier");
      }
      owner = owner.startsWith("WA") ? owner : "WA" + owner;
    }
    validateIdentifier(owner);
  }

  public static void validateIdentifier(String value) {
    if (value == null || !value.matches("[a-zA-Z0-9][a-zA-Z0-9_-]{0,79}")) {
      throw new IllegalArgumentException("Invalid homepage identifier");
    }
  }
}
