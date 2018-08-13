package org.silverpeas.looks.aurora;

import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.space.SpaceInstLight;

import java.util.ArrayList;
import java.util.List;

public class Project {

  private SpaceInstLight space;
  private List<SilverpeasComponentInstance> components = new ArrayList<>();
  
  public Project(SpaceInstLight space) {
    this.space = space;
  }
  
  public void setSpace(SpaceInstLight space) {
    this.space = space;
  }
  public SpaceInstLight getSpace() {
    return space;
  }
  
  public void setComponents(List<SilverpeasComponentInstance> components) {
    this.components = components;
  }
  public List<SilverpeasComponentInstance> getComponents() {
    return components;
  }
  
  public String getName() {
    return space.getName();
  }
  
  public String getDescription() {
    return space.getDescription();
  } 
}