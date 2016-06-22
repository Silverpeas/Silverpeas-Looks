package org.silverpeas.looks.aurora;

import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.space.SpaceInstLight;

import java.util.ArrayList;
import java.util.List;

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