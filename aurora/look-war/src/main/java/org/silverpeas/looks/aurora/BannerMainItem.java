package org.silverpeas.looks.aurora;

import java.util.ArrayList;
import java.util.List;

import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;

public class BannerMainItem {
  
  private SpaceInstLight space;
  private List<SpaceInstLight> subspaces;
  private List<ComponentInstLight> apps;
  
  public BannerMainItem(SpaceInstLight space) {
    this.space = space;
  }
  
  public void addSubspace(SpaceInstLight space) {
    if (subspaces == null) {
      subspaces = new ArrayList<SpaceInstLight>();
    }
    subspaces.add(space);
  }
  
  public void addApp(ComponentInstLight app) {
    if (apps == null) {
      apps = new ArrayList<ComponentInstLight>();
    }
    apps.add(app);
  }

  public SpaceInstLight getSpace() {
    return space;
  }

  public void setSpace(SpaceInstLight space) {
    this.space = space;
  }

  public List<SpaceInstLight> getSubspaces() {
    return subspaces;
  }

  public void setSubspaces(List<SpaceInstLight> subspaces) {
    this.subspaces = subspaces;
  }

  public List<ComponentInstLight> getApps() {
    return apps;
  }

  public void setApps(List<ComponentInstLight> apps) {
    this.apps = apps;
  }
  
  

}
