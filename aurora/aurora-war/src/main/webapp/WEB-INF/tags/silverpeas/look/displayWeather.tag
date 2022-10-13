<%@ tag import="org.silverpeas.core.web.http.HttpRequest" %><%--
  ~ Copyright (C) 2000 - 2022 Silverpeas
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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="lookHelper" value="${sessionScope['Silverpeas_LookHelper']}"/>
<c:set var="settings" value="${lookHelper.lookSettings}"/>
<view:setBundle bundle="${lookHelper.localizedBundle}"/>

<%@ attribute name="showEphemeris"
              required="true"
              type="java.lang.Boolean" %>

<%@ attribute name="showWeather"
              required="true"
              type="java.lang.Boolean" %>

<%@ attribute name="cities"
              required="false"
              type="java.util.List" %>

<c:set var="now" value="<%=new java.util.Date()%>" />

<fmt:message var="labelWeather" key="look.home.weather.title"/>
<fmt:message var="labelWeatherToday" key="look.home.weather.today"/>
<fmt:message var="labelWeatherTomorrow" key="look.home.weather.tomorrow"/>
<fmt:message var="unvailableWeather" key="look.message.weatherUnvailable"/>

<c:if test="${showEphemeris}">
  <div class="secteur-container weather" id="weather-home">
    <h4><span class="title">${labelWeather}</span><span class="date-today"> <fmt:formatDate value="${now}" pattern="${settings.dateFormat}"/></span></h4>
    <div id="ephemeride"></div>
    <c:if test="${showWeather}">
      <div id="localisation-weather">
        <c:set var="firstCity" value="true"/>
        <c:forEach var="city" items="${cities}">
          <a class="select" id="${city.id}" href="#" onclick="showWeather(${city.id});return false;"><span>${city.name}</span></a>
        </c:forEach>
      </div>
      <div class="day" id="day1"> <img alt="soleil et nuage" src="imgDesign/meteo/meteo_44.png" />
        <div class="temperature"><span class="min">min XX&deg;</span> <br />
          <span class="max">max XX&deg;</span> </div>
        <div class="label"><span>${labelWeatherToday}</span></div>
      </div>
      <div class="day" id="day2"> <img alt="soleil et nuage" src="imgDesign/meteo/meteo_44.png" />
        <div class="temperature"><span class="min">min XX&deg;</span> <br />
          <span class="max">max XX&deg;</span> </div>
        <div class="label"><span>${labelWeatherTomorrow}</span></div>
      </div>
    </c:if>
  </div>

  <script type="text/javascript" src="js/ephemeris.min.js"></script>
  <script type="text/javascript" src="js/silverpeas-weather.js"></script>
  <script type="text/javascript" src="js/silverpeas-weather-yahoo.js"></script>
  <script type="text/javascript" src="js/silverpeas-weather-openweathermap.js"></script>
  <script type="text/javascript" src="js/silverpeas-weather-accuweather.js"></script>
  <script type="text/javascript">
    var weatherCookieName = "Silverpeas_Intranet_LastVisitedCity";

    function showWeather(cityId) {
       jQuery.cookie(weatherCookieName, cityId, {secure: <%= HttpRequest.isCurrentRequestSecure() %>});
       jQuery('#localisation-weather a').removeClass("select");
       jQuery('#' + cityId).addClass("select");
       jQuery('span#hour').remove();
       spWeather.load(cityId, function(weather) {
          var indexes = [0];
          var time = false;
          if (weather.api.includes('hourly')) {
             time = true;
             for (var i = 1; i < weather.forecasts.length; i++) {
                if (weather.forecasts[i].date.dayOfYear() > weather.forecasts[0].date.dayOfYear()
                  && weather.forecasts[i].date.hours() >= 8) {
                   console.log('TOMORROW: ', weather.forecasts[i].date);
                   indexes.push(i);
                   break;
                }
             }
          } else {
             indexes.push(1);
          }
          for (var i = 0; i < indexes.length; i++) {
             var dayNb = i + 1;
             jQuery('#day' + dayNb + ' .temperature .min').html(
                 "min " + weather.forecasts[indexes[i]].temperature.min + "&deg;");
             jQuery('#day' + dayNb + ' .temperature .max').html(
                 "max " + weather.forecasts[indexes[i]].temperature.max + "&deg;");
             jQuery('#day' + dayNb + ' img').attr("alt", weather.forecasts[indexes[i]].description).attr(
                 "src", weather.forecasts[indexes[i]].icon);
             if (time) {
                jQuery('#day' + dayNb + ' .label').append(
                    $('<span>', {id: 'hour'}).html(' ' + weather.forecasts[indexes[i]].date.hours() + 'H'));
             }
          }
       }, function() {
          jQuery('#day1').html("${unvailableWeather}");
      });
    }

    whenSilverpeasReady(function() {
      $("#ephemeride").html(ephemeris.getTodayEphemerisName());

      <c:if test="${showWeather}">
      // init weather
       var cities = [];
       <c:forEach var="city" items="${cities}">
       cities.push('${city.id}');
       </c:forEach>
       var cityId = $.cookie(weatherCookieName);
       if (!cityId || !cities.includes(cityId)) {
          cityId = $("#localisation-weather a:first").attr("id");
       } else {
      }
       showWeather(cityId);
      </c:if>
    });
  </script>
</c:if>