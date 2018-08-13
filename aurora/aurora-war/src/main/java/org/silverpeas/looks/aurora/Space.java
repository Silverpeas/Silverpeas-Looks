package org.silverpeas.looks.aurora;

import org.owasp.encoder.Encode;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.util.WebEncodeHelper;

public class Space {

  private SpaceInstLight spaceInst;
  private String intro;
  private String picture;
  private String userLanguage;

  public Space(SpaceInstLight space, String userLanguage) {
    this.spaceInst = space;
    this.userLanguage = userLanguage;
  }

  public String getId() {
    return spaceInst.getId();
  }

  public String getName() {
    return Encode.forHtml(spaceInst.getName(userLanguage));
  }

  public String getDescription() {
    return WebEncodeHelper
        .convertWhiteSpacesForHTMLDisplay(Encode.forHtml(spaceInst.getDescription(userLanguage)));
  }

  public void setIntro(String intro) {
    this.intro = intro;
  }

  public String getIntro() {
    return intro;
  }

  public SpaceInstLight getSpace() {
    return spaceInst;
  }

  public String getPicture() {
    return picture;
  }

  public void setPicture(final String picture) {
    this.picture = picture;
  }
}