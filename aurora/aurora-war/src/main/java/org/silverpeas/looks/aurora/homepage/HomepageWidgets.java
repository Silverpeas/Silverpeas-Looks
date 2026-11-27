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

  public static HomepageContentStore.Widget imageWidget(HomepageContentStore store,
      HomepagePage page, String id, String language) throws IOException {
    var saved = store.readWidget(page, id, language);
    if (!saved.content().revision().isEmpty()) {
      if (!"image".equals(saved.type())) {
        throw new IllegalArgumentException("Expected an image widget");
      }
      return saved;
    }
    return new HomepageContentStore.Widget(id,
        new HomepageContentStore.Content("Image", "", "",
            new HomepageContentStore.Layout("main", 10, true)),
        "image", null, 0, null, null, null, null, null, null, null, null, null, null,
        new HomepageContentStore.ImageConfiguration("", "", "", "_self"));
  }

  public static HomepageContentStore.Widget navigationWidget(HomepageContentStore store,
      HomepagePage page, String id, String language) throws IOException {
    if (!"spaces".equals(page.kind())) {
      throw new IllegalArgumentException("Navigation is only available for spaces");
    }
    var saved = store.readWidget(page, id, language);
    if (!saved.content().revision().isEmpty()) {
      if (!"navigation".equals(saved.type())) {
        throw new IllegalArgumentException("Expected a navigation widget");
      }
      return saved;
    }
    return new HomepageContentStore.Widget(id,
        new HomepageContentStore.Content("Navigation", "", "",
            new HomepageContentStore.Layout("main", 10, true)),
        "navigation", null, 0, null, null, null, null, null, null, null, null, null,
        new HomepageContentStore.NavigationConfiguration(true, true));
  }

  public record NavigationItem(String id, String label, String description, boolean space, String icon) {
    public String getId() { return id; }
    public String getLabel() { return label; }
    public String getDescription() { return description; }
    public boolean isSpace() { return space; }
    public String getIcon() { return icon; }
  }

  public static Map<String, List<NavigationItem>> navigation(HomepagePage page,
      List<HomepageContentStore.Widget> widgets, LookAuroraHelper helper) {
    Map<String, List<NavigationItem>> result = new LinkedHashMap<>();
    if (!"spaces".equals(page.kind())) { return result; }
    var organization = org.silverpeas.core.admin.service.OrganizationController.get();
    for (var widget : widgets) {
      if (!widget.isEnabled() || !"navigation".equals(widget.type())) { continue; }
      var items = new ArrayList<NavigationItem>();
      var configuration = widget.navigationConfiguration();
      if (configuration.subspaces()) {
        java.util.Arrays.stream(organization.getAllowedSubSpaceIds(helper.getUserId(), page.owner()))
            .map(organization::getSpaceInstLightById).filter(java.util.Objects::nonNull)
            .forEach(space -> items.add(new NavigationItem(space.getId(),
                space.getName(helper.getLanguage()), space.getDescription(helper.getLanguage()), true, "")));
      }
      if (configuration.apps()) {
        java.util.Arrays.stream(organization.getAvailCompoIdsAtRoot(page.owner(), helper.getUserId()))
            .map(organization::getComponentInstLight).filter(java.util.Objects::nonNull)
            .filter(app -> !app.isHidden()).forEach(app -> items.add(new NavigationItem(app.getId(),
                app.getLabel(helper.getLanguage()), app.getDescription(helper.getLanguage()), false, app.getIcon(true))));
      }
      result.put(widget.id(), List.copyOf(items));
    }
    return result;
  }

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
    var news = new HomepageContentStore.NewsConfiguration("*", 10, false, "CAROUSEL", true, "");
    var content = new HomepageContentStore.Content("Actualités", "", "",
        new HomepageContentStore.Layout("main", 10, true));
    return new HomepageContentStore.Widget(id, content, "news", news, 0);
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
    var configuration = new HomepageContentStore.PublicationsConfiguration(
        "spaces".equals(page.kind()) ? page.owner() : "", 3, true);
    return new HomepageContentStore.Widget(id,
        new HomepageContentStore.Content("Dernières publications", "", "",
            new HomepageContentStore.Layout("main", 40, true)),
        "publications", null, 0, configuration);
  }

  public static Map<String, List<PublicationDetail>> publications(HomepagePage page,
      List<HomepageContentStore.Widget> widgets, LookAuroraHelper helper) {
    if (!"spaces".equals(page.kind())) {
      return publications(widgets, helper);
    }
    Map<String, List<PublicationDetail>> result = new LinkedHashMap<>();
    widgets.stream().filter(w -> w.isEnabled() && "publications".equals(w.type()))
        .forEach(w -> result.put(w.id(), helper.getLatestPublications(page.owner(),
            w.publicationsConfiguration().size())));
    return result;
  }

  public static org.silverpeas.looks.aurora.NextEvents events(HomepagePage page,
      HomepageContentStore.EventsConfiguration configuration, LookAuroraHelper helper) {
    if ("spaces".equals(page.kind())) {
      var organization = org.silverpeas.core.admin.service.OrganizationController.get();
      String sources = java.util.Arrays.stream(
          organization.getAvailCompoIdsAtRoot(page.owner(), helper.getUserId()))
          .filter(id -> {
            var app = organization.getComponentInstLight(id);
            return app != null && "almanach".equals(app.getName());
          }).collect(java.util.stream.Collectors.joining(" "));
      configuration = new HomepageContentStore.EventsConfiguration(sources,
          configuration.maxDays(), configuration.includeToday(), false,
          configuration.importantOnly(), configuration.showMore());
    }
    return helper.getConfiguredNextEvents(configuration);
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
    return new HomepageContentStore.Widget(id,
        new HomepageContentStore.Content("Éphémérides", "", "",
            new HomepageContentStore.Layout("right", 0, true)), "ephemeris", null, 0);
  }

  public static HomepageContentStore.Widget feedWidget(HomepageContentStore store,
      HomepagePage page, String id, String language, LookAuroraHelper helper, String type)
      throws IOException {
    if (!List.of("events", "rss").contains(type)) {
      throw new IllegalArgumentException("Invalid feed type");
    }
    var saved = store.readWidget(page, id, language);
    if (!saved.content().revision().isEmpty()) {
      if (!type.equals(saved.type())) {
        throw new IllegalArgumentException("Unexpected widget type");
      }
      return saved;
    }
    boolean events = "events".equals(type);
    return new HomepageContentStore.Widget(id,
        new HomepageContentStore.Content(events ? "Prochains événements" : "Flux RSS", "", "",
            new HomepageContentStore.Layout(events ? "right" : "main", events ? 10 : 50, true)),
        type, null, 0, null, events ? new HomepageContentStore.EventsConfiguration("", 2, true, false, true, true) : null,
        events ? null : new HomepageContentStore.RssConfiguration("", "aggregate"));
  }

  public static HomepageContentStore.Widget communityWidget(HomepageContentStore store,
      HomepagePage page, String id, String language, LookAuroraHelper helper, String type)
      throws IOException {
    if (!List.of("faq", "users").contains(type)) {
      throw new IllegalArgumentException("Invalid community type");
    }
    var saved = store.readWidget(page, id, language);
    if (!saved.content().revision().isEmpty()) {
      if (!type.equals(saved.type())) {
        throw new IllegalArgumentException("Unexpected widget type");
      }
      return saved;
    }
    boolean faq = "faq".equals(type);
    var users = faq ? null : new HomepageContentStore.UsersConfiguration("", 5, true, "title phone fonction");
    return new HomepageContentStore.Widget(id,
        new HomepageContentStore.Content(faq ? "FAQ" : "Nouveaux utilisateurs", "", "",
            new HomepageContentStore.Layout(faq ? "right" : "main", faq ? 20 : 60,
                faq || users.size() > 0)),
        type, null, 0, null, null, null,
        faq ? new HomepageContentStore.FaqConfiguration("", 1, "random") : null, users);
  }

  public static Map<String, org.silverpeas.looks.aurora.Questions> questions(
      List<HomepageContentStore.Widget> widgets, LookAuroraHelper helper) {
    Map<String, org.silverpeas.looks.aurora.Questions> result = new LinkedHashMap<>();
    widgets.stream().filter(widget -> widget.isEnabled() && "faq".equals(widget.type()))
        .forEach(widget -> result.put(widget.id(), helper.getConfiguredQuestions(widget.faqConfiguration())));
    return result;
  }

  public static Map<String, org.silverpeas.looks.aurora.NewUsersList> users(
      List<HomepageContentStore.Widget> widgets, LookAuroraHelper helper) {
    Map<String, org.silverpeas.looks.aurora.NewUsersList> result = new LinkedHashMap<>();
    widgets.stream().filter(widget -> widget.isEnabled() && "users".equals(widget.type()))
        .forEach(widget -> result.put(widget.id(), helper.getConfiguredNewUsers(widget.usersConfiguration())));
    return result;
  }

  public static HomepageContentStore.Widget weatherWidget(HomepageContentStore store,
      HomepagePage page, String id, String language, LookAuroraHelper helper) throws IOException {
    var saved = store.readWidget(page, id, language);
    if (!saved.content().revision().isEmpty()) {
      if (!"weather".equals(saved.type())) { throw new IllegalArgumentException("Expected a weather widget"); }
      return saved;
    }
    return new HomepageContentStore.Widget(id,
        new HomepageContentStore.Content("Météo", "", "",
            new HomepageContentStore.Layout("right", 5, true)),
        "weather", null, 0);
  }

  public static HomepageContentStore.Widget bookmarksWidget(HomepageContentStore store,
      HomepagePage page, String id, String language, LookAuroraHelper helper) throws IOException {
    var saved = store.readWidget(page, id, language);
    if (!saved.content().revision().isEmpty()) {
      if (!"bookmarks".equals(saved.type())) {
        throw new IllegalArgumentException("Expected a bookmarks widget");
      }
      return saved;
    }
    var configuration = new HomepageContentStore.BookmarksConfiguration(true, "");
    return new HomepageContentStore.Widget(id,
        new HomepageContentStore.Content("Favoris personnels", "", "",
            new HomepageContentStore.Layout("right", 40, true)),
        "bookmarks", null, 0, null, null, null, null, null, null, null, null, configuration);
  }

  public static HomepageContentStore.Widget searchWidget(HomepageContentStore store,
      HomepagePage page, String id, String language, LookAuroraHelper helper) throws IOException {
    var saved = store.readWidget(page, id, language);
    if (!saved.content().revision().isEmpty()) {
      if (!"search".equals(saved.type())) {
        throw new IllegalArgumentException("Expected a search widget");
      }
      return saved;
    }
    var configuration = new HomepageContentStore.SearchConfiguration("taxonomy", "", false, false, false);
    return new HomepageContentStore.Widget(id,
        new HomepageContentStore.Content("Recherche", "", "",
            new HomepageContentStore.Layout("right", 30, true)),
        "search", null, 0, null, null, null, null, null, null, null, configuration);
  }

  public static HomepageContentStore.Widget shortcutsWidget(HomepageContentStore store,
      HomepagePage page, String id, String language, LookAuroraHelper helper) throws IOException {
    var saved = store.readWidget(page, id, language);
    if (!saved.content().revision().isEmpty()) {
      if (!"shortcuts".equals(saved.type())) {
        throw new IllegalArgumentException("Expected a home shortcuts widget");
      }
      return saved;
    }
    var entries = List.<HomepageContentStore.HomeShortcut>of();
    return new HomepageContentStore.Widget(id,
        new HomepageContentStore.Content("Raccourcis", "", "",
            new HomepageContentStore.Layout("main", 0, true)),
        "shortcuts", null, 0, null, null, null, null, null, null,
        new HomepageContentStore.ShortcutsConfiguration(entries));
  }

  public static HomepageContentStore.Widget subscriptionsWidget(HomepageContentStore store,
      HomepagePage page, String id, String language, LookAuroraHelper helper) throws IOException {
    var saved = store.readWidget(page, id, language);
    if (!saved.content().revision().isEmpty()) {
      if (!"subscriptions".equals(saved.type())) {
        throw new IllegalArgumentException("Expected a subscriptions widget");
      }
      return saved;
    }
    return new HomepageContentStore.Widget(id,
        new HomepageContentStore.Content("Actualités des abonnements", "", "",
            new HomepageContentStore.Layout("main", 70, true)),
        "subscriptions", null, 0, null, null, null, null, null,
        new HomepageContentStore.SubscriptionsConfiguration(4, true));
  }

  public static Map<String, List<org.silverpeas.looks.aurora.AuroraNews>> subscriptions(
      List<HomepageContentStore.Widget> widgets, LookAuroraHelper helper) {
    Map<String, List<org.silverpeas.looks.aurora.AuroraNews>> result = new LinkedHashMap<>();
    widgets.stream().filter(widget -> widget.isEnabled() && "subscriptions".equals(widget.type()))
        .forEach(widget -> result.put(widget.id(), helper.getConfiguredSubscriptions(widget.subscriptionsConfiguration())));
    return result;
  }

  public static List<HomepageContentStore.Widget> all(HomepageContentStore store,
      HomepagePage page, String language, LookAuroraHelper helper) throws IOException {
    var widgets = new ArrayList<>(store.widgets(page, language));
    var deleted = store.deletedWidgets(page);
    widgets.removeIf(widget -> deleted.contains(widget.id()));
    widgets.sort(Comparator.comparingInt(HomepageContentStore.Widget::getOrder)
        .thenComparing(HomepageContentStore.Widget::getId));
    return List.copyOf(widgets);
  }

  public static Map<String, NewsList> news(HomepageContentStore store, HomepagePage page,
      List<HomepageContentStore.Widget> widgets, LookAuroraHelper helper) throws IOException {
    Map<String, NewsList> news = new LinkedHashMap<>();
    for (var widget : widgets) {
      if ("news".equals(widget.type()) && widget.isEnabled()) {
        var configuration = widget.newsConfiguration();
        NewsList list;
        if ("spaces".equals(page.kind())) {
          list = helper.getNewsOfSpace(page.owner(), false, configuration.importantOnly(), configuration.size());
          list.setRenderingType(NewsList.RenderingType.valueOf(configuration.rendering()));
        } else {
          list = helper.getConfiguredNews(1, configuration);
        }
        news.put(widget.id(), list);
      }
    }
    return news;
  }
}
