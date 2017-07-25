package org.silverpeas.looks.aurora;

import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.look.Shortcut;

import java.util.List;

/**
 * Created by Nicolas on 19/07/2017.
 */
public abstract class ListOfContributions {

  private List<Shortcut> appShortcuts;
  private String uniqueAppId;

  Shortcut getAppShortcut(String componentId) {
    String url = URLUtil.getSimpleURL(URLUtil.URL_COMPONENT, componentId);
    String label = OrganizationController.get().getComponentInstLight(componentId).getLabel();
    return new Shortcut("", componentId, url, label);
  }

  public List<Shortcut> getAppShortcuts() {
    return appShortcuts;
  }

  void setAppShortcuts(List<Shortcut> appShortcuts) {
    this.appShortcuts = appShortcuts;
  }

  void setUniqueAppId(String componentId) {
    uniqueAppId = componentId;
  }

  public String getUniqueAppURL() {
    if (StringUtil.isNotDefined(uniqueAppId)) {
      return null;
    }
    return URLUtil.getSimpleURL(URLUtil.URL_COMPONENT, uniqueAppId);
  }

}
