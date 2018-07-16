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

<c:if test="${showEphemeris}">
  <div class="secteur-container weather" id="weather-home">
    <h4><span class="title">${labelWeather}</span><span class="date-today"> <fmt:formatDate value="${now}" pattern="dd MMMMM yyyy"/></span></h4>
    <div id="ephemeride">Brigitte</div>
    <c:if test="${showWeather}">
      <div id="localisation-weather">
        <c:set var="firstCity" value="true"/>
        <c:forEach var="city" items="${cities}">
          <a class="select" id="${city.woeid}" href="#" onclick="showWeather(${city.woeid});return false;"><span>${city.label}</span></a>
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
  <script type="text/javascript">
    var weatherCookieName = "Silverpeas_Intranet_LastVisitedCity";

    function addPrefix(str) {
      if ($.browser.edge || $.browser.safari) {
        return str;
      }
      return "yweather\\:" + str;
    }

    jQuery.browser = {};
    jQuery.browser.edge = /edge/.test(navigator.userAgent.toLowerCase());
    jQuery.browser.chrome = /chrome/.test(navigator.userAgent.toLowerCase());
    jQuery.browser.safari = /safari/.test(navigator.userAgent.toLowerCase());
    if (jQuery.browser.chrome && jQuery.browser.safari) {
      jQuery.browser.safari = false;
    }

    function showWeather(woeid) {
      $.cookie(weatherCookieName, woeid);
      $('#localisation-weather a').removeClass("select");
      $('#' + woeid).addClass("select");
      var url = "http://fr.meteo.yahoo.com/france/dummy/unknown-" + woeid;
      $('#external_meteo').attr("href", url);
      $.ajax({
        url : webContext + "/RAjaxMeteo/", type : "get", dataType : "xml", cache : false,

        // As part of the data, we have to pass in the
        // the target url for our server-side AJAX request.
        data : {
          woeid : woeid
        },

        // Alert when content has been loaded.
        success : function(xmlData) {
          // Get the content from the response XML.
          var city = $(xmlData).find(addPrefix("location")).attr('city');
          var numeroJour = 0;
          var strData = $(xmlData).find(addPrefix("forecast")).each(function() {
            numeroJour++;
            var date = $(this).attr('date');
            var day = $(this).attr('day');
            var low = $(this).attr('low');
            var high = $(this).attr('high');

            // Convert F to C°
            low = Math.round((low - 32) / 1.8);
            high = Math.round((high - 32) / 1.8);

            var code = $(this).attr('code');
            $('#day' + numeroJour + ' .temperature .min').html("min " + low + "&deg;");
            $('#day' + numeroJour + ' .temperature .max').html("max " + high + "&deg;");
            $('#day' + numeroJour + ' img').attr("alt", $(this).attr('text'));
            $('#day' + numeroJour + ' img').attr("src", webContext +
                "/look/jsp/imgDesign/meteo/meteo_" + code + ".png");
          });
        },

        error : function(text) {
          $('#day1').html("M&eacute;t&eacute;o indisponible");
        }
      });
    }

    $(document).ready(function() {
      $("#ephemeride").html(ephemeris.getTodayEphemerisName());

      <c:if test="${showWeather}">
      // init weather
      var woeid = $.cookie(weatherCookieName);
      if(woeid == null){
        woeid = $("#localisation-weather a:first").attr("id");
      }
      showWeather(woeid);
      </c:if>
    });
  </script>
</c:if>