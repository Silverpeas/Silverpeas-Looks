/*
 * Copyright (C) 2000 - 2017 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.looks.aurora.service.almanach;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.silverpeas.core.calendar.Calendar;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * @author silveryocha
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CalendarEventOccurrenceEntity
    extends org.silverpeas.core.webapi.calendar.CalendarEventOccurrenceEntity {

  @XmlTransient
  private Calendar calendar;

  public Date getStartDateAsDate() {
    return asDate(getStartDate());
  }

  public Date getEndDateAsDate() {
    return asDate(getEndDate());
  }

  private Date asDate(final String isoDateAsString) {
    if (isOnAllDay()) {
      final ZoneId zoneId = getCalendar().getZoneId();
      return Date.from(LocalDate.parse(isoDateAsString).atStartOfDay(zoneId).toInstant());
    } else {
      return Date.from(OffsetDateTime.parse(isoDateAsString).toInstant());
    }
  }

  public String getInstanceId() {
    return getCalendar().getComponentInstanceId();
  }

  private Calendar getCalendar() {
    if (calendar == null) {
      calendar = Calendar.getById(getCalendarId());
    }
    return calendar;
  }
}
