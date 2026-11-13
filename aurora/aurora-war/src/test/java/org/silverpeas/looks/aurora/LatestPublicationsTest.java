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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.core.web.look.LookSilverpeasV5Helper;
import org.silverpeas.core.web.look.PublicationHelper;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class LatestPublicationsTest {
  private LookAuroraHelper helper;
  private PublicationHelper publications;
  private Administration admin;

  @BeforeEach
  void setup() throws Exception {
    helper = mock(LookAuroraHelper.class, CALLS_REAL_METHODS);
    publications = mock(PublicationHelper.class);
    admin = mock(Administration.class);
    var field = LookAuroraHelper.class.getDeclaredField("admin");
    field.setAccessible(true);
    field.set(helper, admin);
    field = LookSilverpeasV5Helper.class.getDeclaredField("kmeliaTransversal");
    field.setAccessible(true);
    field.set(helper, publications);
    doReturn("").when(helper).getSettings(anyString(), anyString());
    doReturn("42").when(helper).getUserId();
    when(admin.getAllComponentIds("123")).thenReturn(new String[]{"kmelia1"});
    when(admin.getAllComponentIdsRecur("123")).thenReturn(new String[]{"kmelia1", "kmelia2"});
  }

  @Test
  void excludesDescendantApplicationsBeforeApplyingTheLimit() {
    var publication = mock(PublicationDetail.class);
    when(publication.getPK()).thenReturn(new PublicationPK("1", "kmelia1"));
    when(publications.getPublications(eq("123"), anyList(), eq(10))).thenReturn(List.of(publication));
    assertEquals(List.of(publication), helper.getLatestPublications("123", 5, false));
    verify(publications).getPublications(eq("123"), argThat(ids ->
        ids.contains("kmelia2") && !ids.contains("kmelia1")), eq(10));
  }

  @Test
  void keepsDescendantsForTheExistingHomepageBehaviour() {
    when(publications.getPublications(eq("123"), anyList(), eq(10))).thenReturn(List.of());
    assertTrue(helper.getLatestPublications("123", 5).isEmpty());
    verify(publications).getPublications(eq("123"), argThat(ids -> !ids.contains("kmelia2")), eq(10));
  }
  @Test
  void randomFaqSelectionUsesTheCurrentQuestionIdentifier() throws Exception {
    doReturn(2).when(helper).getSettings("home.faq.nb", 1);
    doReturn("random").when(helper).getSettings("home.faq.display", "random");
    var questions = java.util.stream.IntStream.range(1, 4).mapToObj(id -> {
      var question = mock(org.silverpeas.components.questionreply.model.Question.class);
      when(question.getId()).thenReturn(String.valueOf(id));
      return question;
    }).toList();
    var method = LookAuroraHelper.class.getDeclaredMethod("filterFAQsMatchingQuestions", Questions.class, List.class);
    method.setAccessible(true);
    var result = (Questions) method.invoke(helper, new Questions(), questions);
    assertEquals(2, result.getList().size());
    assertNotEquals(result.getList().get(0).getId(), result.getList().get(1).getId());
  }
}
