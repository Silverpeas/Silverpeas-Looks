/**
 * The translator of the data coming from the OpenWeatherMap APÏ service.
 */
(function() {

   if (typeof window.spWeather.translators.OpenWeatherMap === 'undefined') {
      window.spWeather.translators.OpenWeatherMap = (function() {

         // if the forecast codes are about thunderstorm, then translate them to the correct
         // weather icon.
         function thunderstorm(codes) {
            var icon;
            if (codes.weather >= 200 && codes.weather <= 211) {
               icon = 'meteo_4.png';
            } else if (codes.weather >= 212 && codes.weather <= 232) {
               icon = 'meteo_3.png';
            } else if (codes.weather > 232 && codes.weather < 300) {
               icon = 'meteo_3.png';
               console.log('Weather code unknown:', codes.weather);
            }
            return icon;
         }

         // if the forecast codes are about drizzle, then translate them to the correct
         // weather icon.
         function drizzle(codes) {
            var icon;
            if (codes.weather >= 300 && codes.weather <= 310) {
               icon = 'meteo_9.png';
            } else if (codes.weather >= 311 && codes.weather <= 321) {
               icon = 'meteo_11.png';
            } else if (codes.weather > 300 && codes.weather < 400) {
               icon = 'meteo_11.png';
               console.log('Weather code unknown:', codes.weather);
            }
            return icon;
         }

         // if the forecast codes are about rain, then translate them to the correct
         // weather icon.
         function rain(codes) {
            var icon;
            if ([500, 501, 520].includes(codes.weather)) {
               icon = 'meteo_9.png';
            } else if ([502, 503, 504, 521, 522, 531].includes(codes.weather)) {
               icon = 'meteo_11.png';
            } else if (codes.weather === 511) {
               icon = 'meteo_10.png';
            } else if (codes.weather > 500 && codes.weather < 600) {
               icon = 'meteo_11.png';
               console.log('Weather code unknown:', codes.weather);
            }
            return icon;
         }

         // if the forecast codes are about snow, then translate them to the correct
         // weather icon.
         function snow(codes) {
            var icon;
            if ([600, 620].includes(codes.weather)) {
               icon = 'meteo_14.png';
            } else if ([601, 621].includes(codes.weather)) {
               icon = 'meteo_13.png';
            } else if ([602, 622].includes(codes.weather)) {
               icon = 'meteo_16.png';
            } else if ([611, 612].includes(codes.weather)) {
               icon = 'meteo_5.png';
            } else if ([615, 616].includes(codes.weather)) {
               icon = 'meteo_6.png';
            } else if (codes.weather > 600 && codes.weather < 700) {
               icon = 'meteo_7.png';
               console.log('Weather code unknown:', codes.weather);
            }
            return icon;
         }

         // if the forecast codes are about atmosphere state, then translate them to the correct
         // weather icon.
         function atmosphere(codes) {
            var icon;
            if ([701, 711, 721].includes(codes.weather)) {
               icon = 'meteo_21.png';
            } else if ([741, 751, 761, 762].includes(codes.weather)) {
               icon = 'meteo_20.png'
            } else if (codes.weather === 731) {
               icon = 'meteo_0.png';
            } else if (codes.weather === 771) {
               icon = 'meteo_24.png';
            } else if (codes.weather === 781) {
               icon = 'meteo_2.png'
            } else if (codes.weather >= 700 && codes.weather < 800) {
               icon = 'meteo_21.png';
               console.log('Weather code unknown:', codes.weather);
            }
            return icon;
         }

         // if the forecast codes are about clear weather, then translate them to the correct
         // weather icon.
         function clear(codes) {
            var icon;
            if (codes.weather === 800) {
               icon = codes.icon.endsWith('d') ? 'meteo_32.png' : 'meteo_33.png';
            }
            return icon;
         }

         // if the forecast codes are about cloudy weather, then translate them to the correct
         // weather icon.
         function clouds(codes) {
            var icon;
            if (codes.weather === 801) {
               icon = codes.icon.endsWith('d') ? 'meteo_30.png' : 'meteo_29.png';
            } else if (codes.weather === 802 || codes.weather === 803) {
               icon = codes.icon.endsWith('d') ? 'meteo_28.png' : 'meteo_27.png';
            } else if (codes.weather === 804) {
               icon = 'meteo_26.png';
            } else if (codes.weather > 800) {
               icon = 'meteo_26.png';
               console.log('Weather code unknown:', codes.weather);
            }
            return icon;
         }

         function nothing(codes) {
            console.log('Weather code unknown:', codes.weather);
            return 'meteo_3200.png';
         }

         function computeIconUrl(weatherCode, iconCode) {
            return spWeather.iconBaseUrl + new SelectionPipeline({weather : weatherCode, icon : iconCode})
                .either(thunderstorm)
                .or(drizzle)
                .or(rain)
                .or(snow)
                .or(atmosphere)
                .or(clear)
                .or(clouds)
                .or(nothing)
                .get();
         }

         return {
            translate : function(data) {
               var cityWeather = new spWeather.CityWeather(data.city.id, data.city.name);
               cityWeather.service = 'OpenWeatherMap';
               cityWeather.api = 'days per 3-hourly';
               for (var i = 0; i < data.list.length; i++) {
                  console.log('FORCECAST ' + i + ': ', data.list[i]);
                  var forecast = {
                     date : moment(data.list[i].dt_txt + 'Z'),
                     label : data.list[i].weather[0].main,
                     description : data.list[i].weather[0].description,
                     icon : computeIconUrl(data.list[i].weather[0].id,
                         data.list[i].weather[0].icon),
                     temperature : {
                        min : data.list[i].main.temp_min,
                        max : data.list[i].main.temp_max,
                        unit : 'Celcius'
                     },
                     pressure : data.list[i].main.pressure,
                     humidity : data.list[i].main.humidity
                  };
                  cityWeather.forecasts.push(forecast);
               }
               return cityWeather;
            }
         };

      })();
   }

})();