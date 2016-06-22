package org.silverpeas.looks.aurora;

import java.io.Serializable;

public class City implements Serializable {

  private static final long serialVersionUID = -5923978180549813179L;
  
  private String woeid = null;
  private String label = null;
  
  public City(String woeid, String label) {
    super();
    this.woeid = woeid;
    this.label = label;
  }

  public String getWoeid() {
    return woeid;
  }

  public void setWoeid(String woeid) {
    this.woeid = woeid;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }
  
}