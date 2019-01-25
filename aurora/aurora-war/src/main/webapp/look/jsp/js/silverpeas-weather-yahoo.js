/**
 * The translator of the data coming from the Yahoo weather APÏ service.
 */
(function() {

   if (typeof window.spWeather.translators.Yahoo === 'undefined') {
      window.spWeather.translators.Yahoo = (function() {
         return {
            translate : function(data) {
               var cityWeather = new spWeather.CityWeather(data.location.woeid,
                   data.location.city);
               cityWeather.api = 'daily';
               for (var i = 0; i < data.forecasts.length; i++) {
                  var forecast = {
                     date : moment(data.forecasts[i].date * 1000),
                     label : data.forecasts[i].text,
                     description : data.forecasts[i].text,
                     icon : spWeather.iconBaseUrl + 'meteo_' + data.forecasts[i].code + '.png',
                     temperature : {
                        min : data.forecasts[i].low,
                        max : data.forecasts[i].high,
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