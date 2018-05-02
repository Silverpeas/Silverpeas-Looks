package org.silverpeas.looks.aurora;

import org.silverpeas.components.rssaggregator.model.RSSItem;
import org.silverpeas.components.rssaggregator.model.SPChannel;

import java.util.List;

public class RSSFeeds {

  private static final String DISPLAYER_TABS = "tabs";
  private static final String DISPLAYER_AGGREGATE = "aggregate";
  private static final String DISPLAYER_CARROUSEL = "carrousel";

  private List<SPChannel> channels;
  private List<RSSItem> agregatedItems;
  private String displayer;

  public RSSFeeds(List<SPChannel> channels, List<RSSItem> items) {
    this.channels = channels;
    this.agregatedItems = items;
  }

  public List<SPChannel> getChannels() {
    return channels;
  }

  public List<RSSItem> getAggregatedItems() {
    return agregatedItems;
  }

  public boolean isTabs() { return DISPLAYER_TABS.equals(displayer); }

  public boolean isCarrousel() { return DISPLAYER_CARROUSEL.equals(displayer); }

  public boolean isAggregate() { return DISPLAYER_AGGREGATE.equals(displayer); }

  public void setDisplayer(String displayer) {
    this.displayer = displayer;
  }
}