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
package org.silverpeas.looks.aurora;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Unit tests on the size of the images of a list of news.
 *
 * @author mmoquillon
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class NewsListTest {

  private NewsList newsList;

  @BeforeEach
  void createEmptyNewsList() {
    newsList = new NewsList(Collections.emptyList(), null);
  }

  @Test
  void imageSizeWithWidthAndHeight() {
    newsList.setImageSize("800", "600");
    assertThat(newsList.getImageSize(), is("800x600"));
  }

  @Test
  void imageSizeWithWidthOnly() {
    newsList.setImageSize("800", "");
    assertThat(newsList.getImageSize(), is("800x"));
  }

  @Test
  void imageSizeWithHeightOnly() {
    newsList.setImageSize(null, "600");
    assertThat(newsList.getImageSize(), is("x600"));
  }

  @Test
  void aZeroHeightIsNoHeightConstraint() {
    newsList.setImageSize("800", "0");
    assertThat(newsList.getImageSize(), is("800x"));
  }

  @Test
  void aZeroWidthIsNoWidthConstraint() {
    newsList.setImageSize(" 0 ", "600");
    assertThat(newsList.getImageSize(), is("x600"));
  }

  @Test
  void noImageSizeWithoutAnyPositiveDimension() {
    newsList.setImageSize("0", "-1");
    assertThat(newsList.getImageSize(), is(""));
  }

  @Test
  void anInvalidDimensionIsIgnored() {
    newsList.setImageSize("800", "auto");
    assertThat(newsList.getImageSize(), is("800x"));
  }
}
