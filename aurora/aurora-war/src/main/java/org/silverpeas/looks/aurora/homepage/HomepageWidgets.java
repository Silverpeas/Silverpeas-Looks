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

import org.silverpeas.looks.aurora.LookAuroraHelper;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.looks.aurora.NewsList;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Shared placement and rendering model for all homepage widget types. */
public final class HomepageWidgets {
  private HomepageWidgets() { }

  public static int legacyIndex(String id) {
    return id != null && id.matches("news-legacy-[1-3]")
        ? Integer.parseInt(id.substring(id.length() - 1)) : 0;
  }

  public static HomepageContentStore.Widget newsWidget(HomepageContentStore store,
      HomepagePage page, String id, String language, LookAuroraHelper helper) throws IOException {
    var saved = store.readWidget(page, id, language);
    if (!saved.content().revision().isEmpty()) {
      if (!"news".equals(saved.type())) {
        throw new IllegalArgumentException("Expected a news widget");
      }
      return saved;
    }
    int index = legacyIndex(id);
    var legacy = index == 0 ? null : store.readNews(page, index);
    var news = legacy != null ? legacy : index == 0
        ? new HomepageContentStore.NewsConfiguration("*", 10, false, "CAROUSEL", true, "")
        : helper.getDefaultNewsConfiguration(index);
    var content = new HomepageContentStore.Content(index == 0 ? "Actualités" : "Actualités " + index,
        "", "", new HomepageContentStore.Layout("main", index == 0 ? 10 : index * 10, news.enabled()));
    return new HomepageContentStore.Widget(id, content, "news", news, index);
  }

  public static HomepageContentStore.Widget publicationsWidget(HomepageContentStore store,
      HomepagePage page, String id, String language, LookAuroraHelper helper) throws IOException {
    var saved = store.readWidget(page, id, language);
    if (!saved.content().revision().isEmpty()) {
      if (!"publications".equals(saved.type())) {
        throw new IllegalArgumentException("Expected a publications widget");
      }
      return saved;
    }
    boolean legacy = "publications-legacy".equals(id);
    var configuration = legacy ? helper.getDefaultPublicationsConfiguration()
        : new HomepageContentStore.PublicationsConfiguration("", 3, true);
    return new HomepageContentStore.Widget(id,
        new HomepageContentStore.Content("Dernières publications", "", "",
            new HomepageContentStore.Layout("main", 40, true)),
        "publications", null, 0, configuration);
  }

  public static Map<String, List<PublicationDetail>>
      publications(List<HomepageContentStore.Widget> widgets, LookAuroraHelper helper) {
    Map<String, List<PublicationDetail>> result = new LinkedHashMap<>();
    for (var widget : widgets) {
      if ("publications".equals(widget.type()) && widget.isEnabled()) {
        var configuration = widget.publicationsConfiguration();
        result.put(widget.id(), helper.getLatestPublications(
            configuration.spaceId().isEmpty() ? null : configuration.spaceId(), configuration.size()));
      }
    }
    return result;
  }

  public static HomepageContentStore.Widget ephemerisWidget(HomepageContentStore store,
      HomepagePage page, String id, String language, LookAuroraHelper helper) throws IOException {
    var saved = store.readWidget(page, id, language);
    if (!saved.content().revision().isEmpty()) {
      if (!"ephemeris".equals(saved.type())) {
        throw new IllegalArgumentException("Expected an ephemeris widget");
      }
      return saved;
    }
    boolean enabled = !"ephemeris-legacy".equals(id) || helper.getLookSettings().isDisplayEphemeris();
    return new HomepageContentStore.Widget(id,
        new HomepageContentStore.Content("Éphémérides", "", "",
            new HomepageContentStore.Layout("right", 0, enabled)), "ephemeris", null, 0);
  }

  public static List<HomepageContentStore.Widget> all(HomepageContentStore store,
      HomepagePage page, String language, LookAuroraHelper helper) throws IOException {
    var widgets = new ArrayList<>(store.widgets(page, language));
    if ("skins".equals(page.kind())) {
      for (int index = 1; index <= 3; index++) {
        String id = "news-legacy-" + index;
        if (widgets.stream().noneMatch(widget -> widget.id().equals(id))) {
          widgets.add(newsWidget(store, page, id, language, helper));
        }
      }
    }
    if ("skins".equals(page.kind()) &&
        widgets.stream().noneMatch(widget -> widget.id().equals("publications-legacy"))) {
      widgets.add(publicationsWidget(store, page, "publications-legacy", language, helper));
    }
    if ("skins".equals(page.kind()) &&
        widgets.stream().noneMatch(widget -> widget.id().equals("ephemeris-legacy"))) {
      widgets.add(ephemerisWidget(store, page, "ephemeris-legacy", language, helper));
    }
    widgets.sort(Comparator.comparingInt(HomepageContentStore.Widget::getOrder)
        .thenComparing(HomepageContentStore.Widget::getId));
    return List.copyOf(widgets);
  }

  public static Map<String, NewsList> news(HomepageContentStore store, HomepagePage page,
      List<HomepageContentStore.Widget> widgets, LookAuroraHelper helper) throws IOException {
    Map<String, NewsList> news = new LinkedHashMap<>();
    for (var widget : widgets) {
      if ("news".equals(widget.type()) && widget.isEnabled()) {
        int index = widget.legacyIndex() == 0 ? 1 : widget.legacyIndex();
        var configuration = widget.legacyIndex() != 0 && widget.content().revision().isEmpty()
            ? store.readNews(page, index) : widget.newsConfiguration();
        news.put(widget.id(), helper.getConfiguredNews(index, configuration));
      }
    }
    return news;
  }
}
