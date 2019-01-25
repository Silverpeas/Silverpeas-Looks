(function() {
   if (typeof window.spWeather === 'undefined') {

      /**
       * The Silverpeas plugin to load and render the weather of one or more cities in 5 days
       * maximum.
       * @type {object}
       */
      window.spWeather = new function() {
         var url = window.webContext + '/RWeatherService/';

         /**
          * The weather of a given city as defined by the specified data. The constructor of the
          * CityWeather object acts as a translator of the data format specified by the weather
          * service to the format expected by the Silverpeas spWeather plugin.
          * @param data the weather data of a city fetched from the weather service.
          * @constructor
          */
         this.CityWeather = function(cityId, cityName) {

            /**
             * The city about which this weather is.
             * @type {{name: String, id: String}}
             */
            this.city = {
               id : cityId, name : cityName
            };

            /**
             * An array of weather forecasts.
             * @type {Array}
             */
            this.forecasts = [];
         };

         /**
          * A dictionary of weather data translators. A translator is an object dedicated to
          * translate the data coming from a weather service API to a CityWeather object. Those
          * objects have to implement a method named translate.
          * @type {object} whose each attribute is a translator of a given supported weather service.
          */
         this.translators = {};

         /**
          * Loads the weather forecast data of the specified city, translates the data to a
          * CityWeather object and passes it to the specified renderer.
          * @param cityId the unique identifier of a city. The identifiers depend on the weather
          * service that will be requested (each weather service has their own way to identify
          * a city in the world).
          * @param renderer a function to apply to the CityWeather object constructed from the
          * received weather forecast data.
          * @param error a function to call if an error has occurred while requesting the weather
          * forecast data.
          */
         this.load = function(cityId, renderer, error) {
            window.sp.ajaxRequest(url).withParam('city', cityId).send().then(function(request) {
               var weatherService = request.getResponseHeader('X-WeatherService');
               if (spWeather.translators[weatherService]) {
                  var cityWeather = spWeather.translators[weatherService].translate(
                      request.responseAsJson());
                  if (typeof renderer === 'function') {
                     renderer(cityWeather)
                  } else {
                     console.log('ERROR spWeather: no renderer of weather data specified!');
                  }
               } else {
                  console.log('ERROR spWeather: no data translator for weather service: ',
                      weatherService);
               }
            }, function() {
               if (error) {
                  error();
               }
            });
         };

         /**
          * The URL path of the directory containing all the weather icons. Dedicated to be used
          * by the different weather service translators.
          * @type {string}
          */
         this.iconBaseUrl = window.webContext + '/look/jsp/imgDesign/meteo/';
      };
   }
})();