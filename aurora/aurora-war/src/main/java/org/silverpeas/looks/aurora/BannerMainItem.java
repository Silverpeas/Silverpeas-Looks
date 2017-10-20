package org.silverpeas.looks.aurora;

import org.silverpeas.core.admin.space.SpaceInstLight;

public class BannerMainItem {
  
  private SpaceInstLight space;

  public BannerMainItem(SpaceInstLight space) {
    this.space = space;
  }

  public SpaceInstLight getSpace() {
    return space;
  }

}