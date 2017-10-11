package org.silverpeas.looks.aurora;

import org.silverpeas.core.web.look.Shortcut;
import org.silverpeas.looks.aurora.service.almanach.CalendarEventOccurrenceEntity;

/**
 * Created by Nicolas on 19/07/2017.
 */
public class Event {

  private CalendarEventOccurrenceEntity event;
  private Shortcut appShortcut;

  public Event(CalendarEventOccurrenceEntity event) {
    this.event = event;
  }

  public CalendarEventOccurrenceEntity getDetail() {
    return this.event;
  }

  public void setAppShortcut(final Shortcut appShortcut) {
    this.appShortcut = appShortcut;
  }

  public Shortcut getAppShortcut() {
    return appShortcut;
  }

}