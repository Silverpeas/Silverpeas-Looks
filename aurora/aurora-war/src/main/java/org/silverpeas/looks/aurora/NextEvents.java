package org.silverpeas.looks.aurora;

import org.silverpeas.core.web.look.Shortcut;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Nicolas on 19/07/2017.
 */
public class NextEvents extends ListOfContributions {

  private List<NextEventsDate> nextEventsDates;

  public NextEvents(final List<NextEventsDate> nextEventsDates) {
    this.nextEventsDates = nextEventsDates;
    processShortcuts();
  }

  public List<NextEventsDate> getNextEventsDates() {
    return nextEventsDates;
  }

  private void processShortcuts() {
    Map<String, Shortcut> map = new HashMap<>();
    for (NextEventsDate date : nextEventsDates) {
      for (Event event : date.getEvents()) {
        String componentId = event.getDetail().getInstanceId();
        Shortcut appShortcut = map.computeIfAbsent(componentId, this::getAppShortcut);
        event.setAppShortcut(appShortcut);
      }
    }
    setAppShortcuts(new ArrayList<>(map.values()));
  }

}
