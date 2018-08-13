package org.silverpeas.looks.aurora;

import org.owasp.encoder.Encode;
import org.silverpeas.core.admin.component.model.ComponentInstLight;

public class App {

  private ComponentInstLight component;
  private String userLanguage;

  public App(ComponentInstLight component, String userLanguage) {
    this.component = component;
    this.userLanguage = userLanguage;
  }

  public String getId() {
    return component.getId();
  }

  public String getLabel() {
    return Encode.forHtml(component.getLabel(userLanguage));
  }

  public String getDescription() {
    return Encode.forHtml(component.getDescription(userLanguage));
  }

  public String getBigIcon() {
    return component.getIcon(true);
  }

  public String getIcon() {
    return component.getIcon(false);
  }

  public ComponentInstLight getComponent() {
    return component;
  }
}