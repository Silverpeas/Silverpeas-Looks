package org.silverpeas.looks.aurora;

import org.silverpeas.looks.aurora.service.almanach.CalendarEventOccurrenceEntity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class NextEventsDate {
  
  private final Date date;
  private final List<Event> events = new ArrayList<>();
  private Calendar calendar;
  
  public NextEventsDate(Date date) {
    this.date = date;
  }

  public void addEvent(CalendarEventOccurrenceEntity event) {
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