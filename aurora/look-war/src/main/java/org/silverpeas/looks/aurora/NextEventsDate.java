package org.silverpeas.looks.aurora;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.stratelia.webactiv.almanach.model.EventOccurrence;

public class NextEventsDate {
  
  public Date date;
  public List<EventOccurrence> events = new ArrayList<EventOccurrence>();
  
  public NextEventsDate(Date date) {
    this.date = date;
  }
  
  public void addEvent(EventOccurrence event) {
    events.add(event);
  }

}
