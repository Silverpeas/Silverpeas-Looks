/**
 * The translator of the data coming from the AccuWeather APÏ service.
 */
(function() {

   if (typeof window.spWeather.translators.AccuWeather === 'undefined') {
      window.spWeather.translators.AccuWeather = (function() {
         function parseCityInfo(data) {
            var link = data.Headline.Link;
            return link.match(/[a-z]+\/\d+/)[0].split('/');
         }

         function computeIconUrl(code) {

            function clear(code) {
               var icon;
               if (code === 1 || code === 2) {
                  icon = 'meteo_32.png';
               } else if (code === 33 || code === 34) {
                  icon = 'meteo_31.png';
               }
               return icon;
            }

            function clouds(code) {
               var icon;
               if (code === 3 || code === 4) {
                  icon = 'meteo_30.png'
               } else if (code === 5 || code === 6) {
                  icon = 'meteo_28.png';
               } else if (code === 7) {
                  icon = 'meteo_26.png'
               } else if (code === 35 || code === 36) {
                  icon = 'meteo_29.png';
               } else if (code === 37 || code === 38) {
                  icon = 'meteo_29.png';
               } else if (code === 8) {
                  icon = 'meteo_22.png';
               }
               return icon;
            }

            function rain(code) {
               var icon;
               if (code === 12) {
                  icon = 'meteo_12.png';
               } else if (code === 13 || code === 18 || code === 40) {
                  icon = 'meteo_40.png';
               } else if (code === 14 || code === 25 || code === 39) {
                  icon = 'meteo_9.png';
               } else if (code === 26) {
                  icon = 'meteo_10.png';
               }
               return icon;
            }

            function thunderstorm(code) {
               var icon;
               if (code === 15) {
                  icon = 'meteo_39.png';
               } else if (code === 16 || code === 42) {
                  icon = 'meteo_37.png';
               } else if (code === 17 || code === 41) {
                  icon = 'meteo_4.png';
               }
               return icon;
            }

            function atmosphere(code) {
               var icon;
               if (code === 11) {
                  icon = 'meteo_21.png';
               } else if ([19, 20, 21, 43].includes(code)) {
                  icon = 'meteo_1.png';
               } else if (code === 30) {
                  icon = 'meteo_36.png';
               } else if (code === 31) {
                  icon = 'meteo_25.png';
               } else if (code === 32) {
                  icon = 'meteo_23.png';
               }
               return icon;
            }

            function snow(code) {
               var icon;
               if (code === 22) {
                  icon = 'meteo_16.png';
               } else if (code === 23 || code === 44) {
                  icon = 'meteo_5.png';
               } else if (code === 24) {
                  icon = 'meteo_18.png';
               } else if (code === 29) {
                  icon = 'meteo_5.png';
               }
               return icon;
            }

            function nothing(code) {
               console.log('Weather icon code unknown:', code);
               return 'meteo_3200.png';
            }

            return spWeather.iconBaseUrl + new SelectionPipeline(code)
                .either(clear)
                .or(clouds)
                .or(rain)
                .or(thunderstorm)
                .or(atmosphere)
                .or(snow)
                .or(nothing)
                .get();
         }

         return {
            translate : function(data) {
               var cityInfo = parseCityInfo(data);
               var cityWeather = new spWeather.CityWeather(cityInfo[1], cityInfo[0]);
               cityWeather.api = 'daily';
               for (var i = 0; i < data.DailyForecasts.length; i++) {
                  var date = moment(data.DailyForecasts[i].Date);
                  var iconPath;
                  if (date.hours() > 19 && date.hours() < 5) {
                     iconPath = computeIconUrl(data.DailyForecasts[i].Night.Icon);
                  } else {
                     iconPath = computeIconUrl(data.DailyForecasts[i].Day.Icon);
                  }
                  var forecast = {
                     date : date,
                     label : data.DailyForecasts[i].Day.IconPhrase,
                     description : data.DailyForecasts[i].Day.IconPhrase,
                     icon : iconPath,
                     temperature : {
                        min : Math.round(data.DailyForecasts[i].Temperature.Minimum.Value),
                        max : Math.round(data.DailyForecasts[i].Temperature.Maximum.Value),
                        unit : 'Celcius'
                     },
                     pressure : 'NA',
                     humidity : 'NA'
                  };
                  cityWeather.forecasts.push(forecast);
               }
               return cityWeather;
            }
         }
      })();
   }
})();
