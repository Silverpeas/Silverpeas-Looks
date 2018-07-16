<%--
  ~ Copyright (C) 2000 - 2018 Silverpeas
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
  ~ along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="lookHelper" value="${sessionScope['Silverpeas_LookHelper']}"/>
<view:setBundle bundle="${lookHelper.localizedBundle}"/>

<%@ attribute name="nextEvents"
              required="true"
              type="org.silverpeas.looks.aurora.NextEvents"
              description="Next event structure" %>

<fmt:message var="labelEvents" key="look.home.events.next"/>
<fmt:message var="labelEventsMore" key="look.home.events.more"/>

<c:if test="${not empty nextEvents.nextEventsDates}">
  <div class="secteur-container events portlet" id="home-event">
    <div class="header">
      <h4 class="portlet-title">${labelEvents}</h4>
    </div>
    <div class="portlet-content" id="calendar">
      <ul class="eventList" id="eventList">
        <c:forEach var="date" items="${nextEvents.nextEventsDates}">
          <li class="events">
            <div class="eventShortDate">
              <span class="number">${date.dayInMonth}</span>/<span class="month">${date.month}</span>
            </div>
            <div class="eventLongDate">
              <fmt:formatDate value="${date.date}" pattern="EEEE dd MMMM yyyy"/></div>
            <c:forEach var="eventFull" items="${date.events}">
              <c:set var="event" value="${eventFull.detail}"/>
              <c:set var="eventAppShortcut" value="${eventFull.appShortcut}"/>
              <div class="event eventFrom-${event.instanceId}">
                <div class="eventName">
                  <a class="sp-permalink" href="${event.occurrencePermalinkUrl}">${event.title}</a>
                  <span class="clock-events">
                        <c:if test="${not event.onAllDay}">
                          <fmt:formatDate value="${event.startDateAsDate}" pattern="HH:mm"/>
                          -
                          <fmt:formatDate value="${event.endDateAsDate}" pattern="HH:mm"/>
                        </c:if>
                        </span>
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
    <c:choose>
      <c:when test="${not empty nextEvents.uniqueAppURL}">
        <a title="${labelEventsMore}" href="${nextEvents.uniqueAppURL}" class="link-more sp-permalink">
          <span>${labelEventsMore}</span>
        </a>
      </c:when>
      <c:otherwise>
        <div id="events-link-apps">
          <c:forEach items="${nextEvents.appShortcuts}" var="appAlmanachShortcut">
            <a title="${labelEventsMore}" href="${appAlmanachShortcut.url}" class="link-more sp-permalink" id="link-app-${appAlmanachShortcut.target}">
              <span>${appAlmanachShortcut.altText}</span>
            </a>
          </c:forEach>
        </div>
      </c:otherwise>
    </c:choose>
  </div>
</c:if>
