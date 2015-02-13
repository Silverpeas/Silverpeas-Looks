package org.silverpeas.looks.aurora;

import java.util.List;

import com.silverpeas.look.Shortcut;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;

public class Heading {

  private SpaceInstLight space;
  private String color;
  private String imageURL;
  private String edito;
  private String title;
  private boolean admin;
  private String backOfficeAppId;

  public List<PublicationDetail> getLastPublications() {
    return lastPublications;
  }

  public void setLastPublications(List<PublicationDetail> lastPublications) {
    this.lastPublications = lastPublications;
  }

  public List<NextEventsDate> getNextEventsDate() {
    return nextEventsDate;
  }

  public void setNextEventsDate(List<NextEventsDate> nextEventsDate) {
    this.nextEventsDate = nextEventsDate;
  }

  private List<Shortcut> shortcuts;
  private List<PublicationDetail> lastPublications;
  private List<NextEventsDate> nextEventsDate;

  public Heading(SpaceInstLight space, String color) {
    setSpace(space);
    setColor(color);
  }

  public String getImageURL() {
    return imageURL;
  }

  public void setImageURL(String imageURL) {
    this.imageURL = imageURL;
  }

  public String getEdito(String defaultEdito) {
    if (StringUtil.isDefined(edito)) {
      return edito;
    }
    return defaultEdito;
  }

  public void setEdito(String edito) {
    this.edito = edito;
  }

  public List<Shortcut> getShortcuts() {
    return shortcuts;
  }

  public void setShortcuts(List<Shortcut> shortcuts) {
    this.shortcuts = shortcuts;
  }

  public void setSpace(SpaceInstLight space) {
    this.space = space;
  }

  public SpaceInstLight getSpace() {
    return space;
  }

  public void setColor(String color) {
    this.color = color;
  }

  public String getColor() {
    return color;
  }

  public String getName() {
    return getSpace().getName();
  }

  public String getFullId() {
    return getSpace().getFullId();
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getTitle(String defaultTitle) {
    if (StringUtil.isDefined(title)) {
      return title;
    }
    return defaultTitle;
  }

  public void setAdmin(boolean admin) {
    this.admin = admin;
  }

  public boolean isAdmin() {
    return admin;
  }

  public void setBackOfficeAppId(String backOfficeAppId) {
    this.backOfficeAppId = backOfficeAppId;
  }

  public String getBackOfficeAppId() {
    return backOfficeAppId;
  }

  public String getBackOfficeAppURL() {
    return URLManager.getApplicationURL() + URLManager.getURL(null, getBackOfficeAppId()) + "Edit";
  }

}