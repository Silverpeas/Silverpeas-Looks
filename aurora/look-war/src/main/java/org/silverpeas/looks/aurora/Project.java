package org.silverpeas.looks.aurora;

import java.util.ArrayList;
import java.util.List;

import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;

public class Project {

  private SpaceInstLight space;
  private List<ComponentInst> components = new ArrayList<ComponentInst>();
  
  public Project(SpaceInstLight space) {
    this.space = space;
  }
  
  public void setSpace(SpaceInstLight space) {
    this.space = space;
  }
  public SpaceInstLight getSpace() {
    return space;
  }
  
  public void setComponents(List<ComponentInst> components) {
    this.components = components;
  }
  public List<ComponentInst> getComponents() {
    return components;
  }
  public void addComponent(ComponentInst component) {
    components.add(component);
  }
  
  public String getName() {
    return space.getName();
  }
  
  public String getDescription() {
    return space.getDescription();
  } 
}