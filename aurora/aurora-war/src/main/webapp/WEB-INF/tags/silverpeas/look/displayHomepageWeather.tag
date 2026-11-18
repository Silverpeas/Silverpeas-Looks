<%--
 * Copyright (C) 2000 - 2026 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 --%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<%@ attribute name="widget" required="true" type="org.silverpeas.looks.aurora.homepage.HomepageContentStore.Widget" %>
<fmt:message var="weatherToday" key="look.home.weather.today"/>
<fmt:message var="weatherTomorrow" key="look.home.weather.tomorrow"/>
<fmt:message var="weatherUnavailable" key="look.message.weatherUnvailable"/>
<c:set var="configuredWeatherCities" value="${sessionScope['Silverpeas_LookHelper'].configuredWeatherCities}"/>
<c:if test="${not empty configuredWeatherCities}">
  <div id="homepage-widget-${widget.id}" class="secteur-container weather homepage-weather"
      data-widget-id="<c:out value='${widget.id}'/>" data-unavailable="<c:out value='${weatherUnavailable}'/>">
    <c:if test="${not empty widget.title}"><h4><c:out value="${widget.title}"/></h4></c:if>
    <div class="homepage-weather-cities">
      <c:forEach items="${configuredWeatherCities}" var="city">
        <button type="button" data-city="<c:out value='${city.id}'/>" aria-pressed="false"><c:out value="${city.name}"/></button>
      </c:forEach>
    </div>
    <p class="homepage-weather-status" role="status" aria-live="polite"></p>
    <div class="homepage-weather-days">
      <c:forEach begin="0" end="1" var="day">
        <div class="homepage-weather-day" hidden>
          <img alt="">
          <div class="temperature"><span class="min"></span><br><span class="max"></span></div>
          <div class="label"><span><c:out value="${day == 0 ? weatherToday : weatherTomorrow}"/></span><span class="hour"></span></div>
        </div>
      </c:forEach>
    </div>
  </div>
</c:if>
