package org.silverpeas.looks.aurora;

import org.owasp.encoder.Encode;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.util.WebEncodeHelper;

public class Space {

  private SpaceInstLight space;
  private String intro;
  private String picture;
  private String userLanguage;

  public Space(SpaceInstLight space, String userLanguage) {
    this.space = space;
    this.userLanguage = userLanguage;
  }

  public String getId() {
    return space.getId();
  }

  public String getName() {
    return Encode.forHtml(space.getName(userLanguage));
  }

  public String getDescription() {
    return WebEncodeHelper
        .convertWhiteSpacesForHTMLDisplay(Encode.forHtml(space.getDescription(userLanguage)));
  }

  public void setIntro(String intro) {
    this.intro = intro;
  }

  public String getIntro() {
    return intro;
  }

  public SpaceInstLight getSpace() {
    return space;
  }

  public String getPicture() {
    return picture;
  }

  public void setPicture(final String picture) {
    this.picture = picture;
  }
}