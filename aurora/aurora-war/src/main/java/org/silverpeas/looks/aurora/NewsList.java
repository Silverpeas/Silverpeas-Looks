package org.silverpeas.looks.aurora;

import org.silverpeas.components.quickinfo.model.News;
import org.silverpeas.core.pdc.pdc.model.ClassifyPosition;
import org.silverpeas.core.pdc.pdc.model.ClassifyValue;
import org.silverpeas.core.pdc.pdc.model.Value;
import org.silverpeas.core.pdc.pdc.service.PdcManager;
import org.silverpeas.core.util.logging.SilverLogger;
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
  private List<NewsListButton> buttons = new ArrayList<>();

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

  public void limitNews(int size) {
    news = news.subList(0, size);
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

  public void withTaxonomyButtons() {
    for (AuroraNews aNews : news) {
      try {
        List<ClassifyPosition> positions = aNews.getNews().getTaxonomyPositions();
        for (ClassifyPosition position : positions) {
          List<ClassifyValue> values = position.getListClassifyValue();
          for (ClassifyValue classifyValue : values) {
            Value value = classifyValue.getFullPath().get(classifyValue.getFullPath().size()-1);
            NewsListButton button = new NewsListButton(value.getName());
            button.setParam(classifyValue.getAxisId()+":"+classifyValue.getValue());
            if (!buttons.contains(button)) {
              buttons.add(button);
            }
          }
        }
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e);
      }
    }
  }

  public List<NewsListButton> getButtons() {
    return buttons;
  }
}
