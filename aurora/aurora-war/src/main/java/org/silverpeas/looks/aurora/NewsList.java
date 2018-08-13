package org.silverpeas.looks.aurora;

import org.silverpeas.components.quickinfo.model.News;
import org.silverpeas.core.web.look.Shortcut;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Nicolas on 19/07/2017.
 */
public class NewsList extends ListOfContributions {

  private List<AuroraNews> news;

  public NewsList(List<News> someNews, String uniqueAppId) {
    setUniqueAppId(uniqueAppId);
    news = new ArrayList<>();
    for(News aNews : someNews) {
      news.add(new AuroraNews(aNews));
    }
    processShortcuts();
  }

  public List<AuroraNews> getNews() {
    return news;
  }

  public boolean isEmpty() {
    return news.isEmpty();
  }

  private void processShortcuts() {
    Map<String, Shortcut> shortcuts = new HashMap();
    for (AuroraNews aNews : news) {
      String componentId = aNews.getNews().getComponentInstanceId();
      Shortcut appShortcut = shortcuts.computeIfAbsent(componentId, this::getAppShortcut);
      aNews.setAppShortcut(appShortcut);
    }
    setAppShortcuts(new ArrayList<>(shortcuts.values()));
  }

}
