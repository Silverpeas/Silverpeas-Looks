package org.silverpeas.looks.aurora.service.weather;

import jakarta.inject.Named;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.kernel.logging.SilverLogger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Requester of the Yahoo weather service.
 *
 * @author mmoquillon
 */
@Service
@Named("Yahoo")
public class YahooWeatherRequester implements WeatherServiceRequester {

  private static final String API_URL = "http://weather-ydn-yql.media.yahoo.com/forecastrss";
  private static final String SERVICE_NAME = "Yahoo";
  private static final Random RANDOM = new SecureRandom();

  @Override
  public WeatherForecastData request(final String cityId) {
    final WeatherSettings settings = WeatherSettings.get();
    final String appKey = settings.getAPIKey();
    final String consumerKey = settings.getClientKey();
    final String consumerSecret = settings.getClientSecret();
    final long timestamp = new Date().getTime() / 1000;

    final String oauthNonce = computeOAuthNonce();
    final String oauthSignature =
        computeOAuthSignature(consumerKey, consumerSecret, oauthNonce, cityId, timestamp);
    final String oauth = "OAuth oauth_consumer_key=\"" + consumerKey +
                         "\",oauth_nonce=\"" + oauthNonce +
                         "\",oauth_timestamp=\"" + timestamp +
                         "\",oauth_signature_method=\"HMAC-SHA1\"" +
                         "\",oauth_signature=\"" + oauthSignature +
                         "\",oauth_version=\"1.0\"";
    try (var client = ClientBuilder.newClient()) {
      final String weatherData =
          client.target(API_URL)
              .queryParam("woeid", cityId)
              .queryParam("u", "c")
              .queryParam("format", "json")
              .request(MediaType.APPLICATION_JSON_TYPE)
              .header("Yahoo-App-Id", appKey)
              .header("Authorization", oauth)
              .get(String.class);
      return new WeatherForecastData(SERVICE_NAME, weatherData, MediaType.APPLICATION_JSON_TYPE);
    }
  }

  private String computeOAuthNonce() {
    final byte[] nonce = new byte[32];
    RANDOM.nextBytes(nonce);
    return new String(nonce).replaceAll("\\W", "");
  }

  private String computeOAuthSignature(final String consumerKey, final String consumerSecret,
      final String oauthNonce, final String cityId, final long timestamp) {
    try {
      final String parameters =
          Stream.of("oauth_consumer_key=" + consumerKey,
                  "oauth_nonce=" + oauthNonce,
                  "oauth_signature_method=HMAC-SHA1",
                  "oauth_timestamp=" + timestamp,
                  "oauth_version=1.0",
                  "woeid=" + cityId,
                  "u=c",
                  "format=json")
              .sorted()
              .collect(Collectors.joining("&"));
      final String toSign = "GET&" +
                            URLEncoder.encode(API_URL, Charsets.UTF_8) + "&" +
                            URLEncoder.encode(parameters, Charsets.UTF_8);
      SecretKeySpec signingKey = new SecretKeySpec((consumerSecret + "&").getBytes(), "HmacSHA1");
      Mac mac = Mac.getInstance("HmacSHA1");
      mac.init(signingKey);
      byte[] rawHMAC = mac.doFinal(toSign.getBytes());
      Base64.Encoder encoder = Base64.getEncoder();
      return encoder.encodeToString(rawHMAC);
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      SilverLogger.getLogger(this).error(e);
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
  }
}

