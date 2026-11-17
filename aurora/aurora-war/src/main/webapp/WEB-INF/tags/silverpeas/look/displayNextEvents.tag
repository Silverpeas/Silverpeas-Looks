<%--
  ~ Copyright (C) 2000 - 2026 Silverpeas
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU Affero General Public License as
  ~ published by the Free Software Foundation, either version 3 of the
  ~ License, or (at your option) any later version.
  ~
  ~ As a special exception to the terms and conditions of version 3.0 of
  ~ the GPL, you may redistribute this Program in connection with Free/Libre
  ~ Open Source Software ("FLOSS") applications as described in Silverpeas's
  ~ FLOSS exception.  You should have received a copy of the text describing
  ~ the FLOSS exception, and it is also available here:
  ~ "https://www.silverpeas.org/legal/floss_exception.html"
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU Affero General Public License for more details.
  ~
  ~ You should have received a copy of the GNU Affero General Public License
  ~ along with this program.  If not, see <https://www.gnu.org/licenses/>.
  --%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<%@ taglib uri="silverpeas.tags.silverFunctions" prefix="silfn" %>
<%@ taglib uri="silverpeas.tags.viewGenerator" prefix="view" %>

<c:set var="lookHelper" value="${sessionScope['Silverpeas_LookHelper']}"/>
<view:setBundle bundle="${lookHelper.localizedBundle}"/>

<%@ attribute name="nextEvents"
              required="true"
              type="org.silverpeas.looks.aurora.NextEvents"
              description="Next event structure" %>

<%@ attribute name="dateFormat"
              required="true"
              type="java.lang.String" %>

<%@ attribute name="id" required="false" type="java.lang.String" %>
<%@ attribute name="title" required="false" type="java.lang.String" %>
<%@ attribute name="showMore" required="false" type="java.lang.Boolean" %>
<c:set var="eventsId" value="${empty id ? 'home-event' : id}"/>
<c:set var="calendarId" value="${eventsId}-calendar"/>
<c:set var="eventsListId" value="${eventsId}-list"/>
<c:if test="${empty id}">
  <c:set var="calendarId" value="calendar"/>
  <c:set var="eventsListId" value="eventList"/>
</c:if>

<fmt:message var="labelEvents" key="look.home.events.next"/>
<fmt:message var="labelEventsMore" key="look.home.events.more"/>

<c:if test="${not empty nextEvents.nextEventsDates}">
  <div class="secteur-container events portlet homepage-events" id="${eventsId}">
    <div class="header">
      <h4 class="portlet-title"><c:out value="${empty title ? labelEvents : title}"/></h4>
    </div>
    <div class="portlet-content" id="${calendarId}">
      <ul class="eventList" id="${eventsListId}">
        <c:forEach var="date" items="${nextEvents.nextEventsDates}">
          <li class="events">
            <div class="eventShortDate">
              <span class="number">${date.dayInMonth}</span>/<span class="month">${date.month}</span>
            </div>
            <div class="eventLongDate">
              <fmt:formatDate value="${date.date}" pattern="${dateFormat}"/></div>
            <c:forEach var="eventFull" items="${date.events}">
              <c:set var="event" value="${eventFull.detail}"/>
              <c:set var="eventAppShortcut" value="${eventFull.appShortcut}"/>
              <div class="event eventFrom-${event.instanceId}">
                <div class="eventName">
                  <a class="sp-permalink" href="${event.occurrencePermalinkUrl}">${event.title}</a>
                  <view:componentPath componentId="${event.instanceId}" includeComponent="false"/>
                  <c:if test="${not event.onAllDay}">
                  <span class="clock-events">
                    <fmt:formatDate value="${event.startDateAsDate}" pattern="HH:mm"/>
                    -
                    <fmt:formatDate value="${event.endDateAsDate}" pattern="HH:mm"/>
                  </span>
                  </c:if>
                </div>
                <c:if test="${silfn:isDefined(event.location) || empty nextEvents.uniqueAppURL}">
                  <div class="eventInfo">
                    <c:if test="${silfn:isDefined(event.location)}">
                      <div class="eventPlace">
                        <div class="bloc"><span>${event.location}</span></div>
                      </div>
                    </c:if>
                    <c:if test="${empty nextEvents.uniqueAppURL}">
                      <div class="eventApp">
                        <a href="${eventAppShortcut.url}" title="${labelEventsMore}" class="event-app-link sp-permalink">${eventAppShortcut.altText}</a>
                      </div>
                    </c:if>
                  </div>
                </c:if>
              </div>
            </c:forEach>
          </li>
        </c:forEach>
      </ul>
    </div>
    <c:if test="${showMore ne false}">
      <c:choose>
        <c:when test="${not empty nextEvents.uniqueAppURL}">
          <a title="${labelEventsMore}" href="${nextEvents.uniqueAppURL}" class="link-more sp-permalink">
            <span>${labelEventsMore}</span>
          </a>
        </c:when>
        <c:otherwise>
          <div class="events-link-apps">
            <c:forEach items="${nextEvents.appShortcuts}" var="appAlmanachShortcut">
              <a title="${labelEventsMore}" href="${appAlmanachShortcut.url}" class="link-more sp-permalink">
                <span>${appAlmanachShortcut.altText}</span>
              </a>
            </c:forEach>
          </div>
        </c:otherwise>
      </c:choose>
    </c:if>
  </div>
</c:if>
