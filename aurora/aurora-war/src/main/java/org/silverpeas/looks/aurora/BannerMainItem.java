package org.silverpeas.looks.aurora;

import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.space.SpaceInstLight;

import java.util.ArrayList;
import java.util.List;

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
  
  private int getNumberOfElements() {
    int nb = 0;
    if (getSubspaces() != null) {
      nb += getSubspaces().size();
    }
    if (getApps() != null) {
      nb += getApps().size();
    }
    return nb;
  }
  
  public int getNumberOfColumns() {
    int nb = getNumberOfElements();
    int result = 0;
    while (nb > 6) {
      result++;
      nb = nb-6;
    }
    result++;
    return result;
  }
  
}