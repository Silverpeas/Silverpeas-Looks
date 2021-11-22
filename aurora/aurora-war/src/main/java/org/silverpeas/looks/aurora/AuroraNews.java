package org.silverpeas.looks.aurora;

import org.silverpeas.components.quickinfo.model.News;
import org.silverpeas.core.contribution.model.Thumbnail;
import org.silverpeas.core.pdc.pdc.model.ClassifyPosition;
import org.silverpeas.core.pdc.pdc.model.ClassifyValue;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.look.Shortcut;

import java.util.Date;

/**
 * Created by Nicolas on 19/07/2017.
 */
public class AuroraNews {

  private final News news;
  private Shortcut appShortcut;

  public AuroraNews(News news) {
    this.news = news;
  }

  public String getTitle() {
    return news.getTitle();
  }

  public String getDescription() {
    return news.getDescription();
  }

  public Date getDate() {
    return news.getOnlineDate();
  }

  public String getPermalink() {
    return news.getPermalink();
  }

  public String getThumbnailURL() {
    Thumbnail thumbnail = news.getPublication()
        .getThumbnail();
    if (thumbnail == null) {
      return null;
    }
    return thumbnail.getURL();
  }

  public News getNews() {
    return news;
  }

  void setAppShortcut(Shortcut shortcut) {
    this.appShortcut = shortcut;
  }

  public Shortcut getAppShortcut() {
    return appShortcut;
  }

  public String getTaxonomyPositionAsString() {
    StringBuilder result = new StringBuilder();
    try {
      for (ClassifyPosition position : news.getTaxonomyPositions()) {
        for (ClassifyValue value : position.getListClassifyValue()) {
          result.append(" taxonomy-")
              .append(value.getAxisId())
              .append("-")
              .append(value.getValue().replace('/', '_'));
        }
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this)
          .error(e);
    }
    return result.toString();
  }
}
