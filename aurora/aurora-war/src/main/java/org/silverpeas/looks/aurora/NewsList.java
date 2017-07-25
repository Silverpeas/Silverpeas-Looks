package org.silverpeas.looks.aurora;

import org.silverpeas.components.quickinfo.model.News;
import org.silverpeas.core.web.look.Shortcut;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    HashMap<String, Shortcut> map = new HashMap();
    for (AuroraNews aNews : news) {
      String componentId = aNews.getNews().getComponentInstanceId();
      Shortcut appShortcut = map.get(componentId);
      if (appShortcut == null) {
        appShortcut = getAppShortcut(componentId);
        map.put(componentId, appShortcut);
      }
      aNews.setAppShortcut(appShortcut);
    }
    setAppShortcuts(new ArrayList<>(map.values()));
  }

}
