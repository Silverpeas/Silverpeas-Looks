package org.silverpeas.looks.aurora.service.weather;

import java.io.Serializable;

public class City implements Serializable {

  private static final long serialVersionUID = -5923978180549813179L;
  
  private final String id;
  private final String name;
  
  public City(String id, String name) {
    super();
    this.id = id;
    this.name = name;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

}