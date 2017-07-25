package org.silverpeas.looks.aurora;

import org.silverpeas.components.almanach.model.EventDetail;
import org.silverpeas.core.web.look.Shortcut;

/**
 * Created by Nicolas on 19/07/2017.
 */
public class Event {

  private EventDetail event;
  private Shortcut appShortcut;

  public Event(EventDetail event) {
    this.event = event;
  }

  public EventDetail getDetail() {
    return this.event;
  }

  public void setAppShortcut(final Shortcut appShortcut) {
    this.appShortcut = appShortcut;
  }

  public Shortcut getAppShortcut() {
    return appShortcut;
  }

}