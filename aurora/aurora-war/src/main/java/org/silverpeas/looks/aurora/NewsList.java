package org.silverpeas.looks.aurora;

import org.silverpeas.components.quickinfo.model.News;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.pdc.pdc.model.ClassifyPosition;
import org.silverpeas.core.pdc.pdc.model.ClassifyValue;
import org.silverpeas.core.pdc.pdc.model.Value;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.web.look.Shortcut;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.silverpeas.kernel.util.StringUtil.EMPTY;

/**
 * Created by Nicolas on 19/07/2017.
 */
public class NewsList extends ListOfContributions {

  private List<AuroraNews> news;
  private final List<NewsListButton> buttons = new ArrayList<>();
  private String imageSize = "800x";
  private AuroraSpaceHomePageZone zone = AuroraSpaceHomePageZone.RIGHT;
  private RenderingType type = RenderingType.CAROUSEL;

  public NewsList(List<News> someNews, ComponentInst app) {
    setApp(app);
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
    Map<String, Shortcut> shortcuts = new HashMap<>();
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

  /**
   * Gets the zone location the news list MUST be managed.
   * @return a {@link AuroraSpaceHomePageZone} instance.
   */
  public AuroraSpaceHomePageZone getZone() {
    return zone;
  }

  /**
   * Gets the image size the image of news MUST be displayed.
   * <p>
   * Format MUST be "WxH" (W = width and H = height in pixels)
   * </p>
   * @return the image size (width x height) as string.
   */
  public String getImageSize() {
    return imageSize;
  }

  /**
   * Sets the size the image of news MUST be displayed. A dimension not defined, not positive (as
   * the 0 of a setting) or not a number sets no constraint on it, so that the image keeps its
   * ratio. Without any positive dimension, the image is displayed at its own size.
   * @param width the width in pixels as string.
   * @param height the height in pixels as string.
   */
  public void setImageSize(final String width, final String height) {
    final String imageWidth = dimension(width);
    final String imageHeight = dimension(height);
    this.imageSize = imageWidth.isEmpty() && imageHeight.isEmpty() ? EMPTY :
        imageWidth + "x" + imageHeight;
  }

  private static String dimension(final String pixels) {
    final String value = StringUtil.isDefined(pixels) ? pixels.trim() : EMPTY;
    return StringUtil.isInteger(value) && Integer.parseInt(value) > 0 ? value : EMPTY;
  }

  /**
   * Sets the zone location the news list MUST be managed.
   * @param zone {@link AuroraSpaceHomePageZone} instance.
   */
  public void setZone(final AuroraSpaceHomePageZone zone) {
    this.zone = zone;
  }

  /**
   * Gets the type the news list MUST be rendered in.
   * @return a {@link RenderingType} instance.
   */
  public RenderingType getRenderingType() {
    return type;
  }

  /**
   * Sets the type the news list MUST be rendered in.
   * @param type a {@link RenderingType} instance.
   */
  public void setRenderingType(final RenderingType type) {
    this.type = type;
  }

  public enum RenderingType {
    LIST, CAROUSEL;

    public boolean isCarousel() {
      return this == CAROUSEL;
    }
  }
}
