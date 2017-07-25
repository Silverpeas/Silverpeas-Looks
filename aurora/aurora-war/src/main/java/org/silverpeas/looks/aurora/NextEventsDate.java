package org.silverpeas.looks.aurora;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.silverpeas.components.almanach.model.EventDetail;
import org.silverpeas.components.almanach.model.EventOccurrence;

public class NextEventsDate {
  
  public Date date;
  public List<Event> events = new ArrayList<Event>();
  Calendar calendar;
  
  public NextEventsDate(Date date) {
    this.date = date;
  }
  
  public void addEvent(EventDetail event) {
    events.add(new Event(event));
  }

  public Date getDate() {
    return date;
  }

  public List<Event> getEvents() {
    return events;
  }

  public int getDayInMonth() {
    initCalendar();
    return calendar.get(Calendar.DATE);
  }

  public int getMonth() {
    initCalendar();
    return calendar.get(Calendar.MONTH)+1;
  }

  private void initCalendar() {
    if (calendar == null) {
      calendar = Calendar.getInstance();
      calendar.setTime(date);
    }
  }

}