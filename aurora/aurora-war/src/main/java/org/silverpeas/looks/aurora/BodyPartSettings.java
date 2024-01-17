/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
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

/**
 * Created by Nicolas on 12/06/2017.
 */
public class BodyPartSettings {

  private String domainsBarParams;
  private String mainPartURL;
  private boolean hideMenu = false;

  public String getDomainsBarParams() {
    return domainsBarParams;
  }

  void setDomainsBarParams(final String domainsBarParams) {
    this.domainsBarParams = domainsBarParams;
  }

  public String getMainPartURL() {
    return mainPartURL;
  }

  void setMainPartURL(final String mainPartURL) {
    this.mainPartURL = mainPartURL;
  }

  public boolean isHideMenu() {
    return hideMenu;
  }

  void setHideMenu(final boolean hideMenu) {
    this.hideMenu = hideMenu;
  }
}