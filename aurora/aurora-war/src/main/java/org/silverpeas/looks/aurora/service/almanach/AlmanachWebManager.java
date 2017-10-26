/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.looks.aurora.service.almanach;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.silverpeas.components.almanach.AlmanachSettings;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.util.JSONCodec;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.silverpeas.core.util.URLUtil.getAbsoluteApplicationURL;
import static org.silverpeas.core.util.URLUtil.getApplicationURL;

/**
 * <p>
 * This WEB provider is in charge of requesting WEB services of almanach component.
 * </p>
 * <p>
 * It has been created in order to replace in a first time the deleted AlmanachService (since
 * Almanach is using Calendar engine).
 * </p>
 * <p>
 * TODO After a full refactoring of look event display, this implementation has to be removed.<br/>
 * Indeed Almanach WEB services should be requested directly by AJAX requests from the WEB browser
 * client.
 * </p>
 * @author silveryocha
 */
public class AlmanachWebManager {

  private static final String[] jsonParametersToAvoidWhenEmpty = {"attachmentParameters"};

  private AlmanachWebManager() {
  }

  /**
   * Gets the next occurrences of an almanach represented by the given component instance
   * identifier.
   * @param almanachId identifier of component instance of type almanach.
   * @return a list of {@link CalendarEventOccurrenceEntity} instances.
   */
  public static Stream<CalendarEventOccurrenceEntity> getNextEventOccurrences(String almanachId) {
    final String jsonResponse = httpGetAsString(
        UriBuilder.fromPath(URLUtil.getAbsoluteLocalApplicationURL()).path("/services/almanach/")
            .path(almanachId).path("/events/occurrences/next")
            .queryParam("limit", AlmanachSettings.getNbOccurrenceLimitOfNextEventView()));
    return Arrays.stream(JSONCodec.decode(jsonResponse, CalendarEventOccurrenceEntity[].class)).peek(e->{
      e.setEventPermalinkUrl(getRightApplicationUrl(e.getEventPermalinkUrl()));
      e.setOccurrencePermalinkUrl(getRightApplicationUrl(e.getOccurrencePermalinkUrl()));
    });
  }

  private static URI getRightApplicationUrl(final URI localURL) {
    String previousLink = localURL.toString();
    return URI.create(
        previousLink.replaceFirst(".+" + getApplicationURL(), getAbsoluteApplicationURL()));
  }

  private static String httpGetAsString(UriBuilder uriBuilder) {
    final String serviceUrl = uriBuilder.build().toString();
    final GetMethod httpGet = new GetMethod(serviceUrl);
    httpGet.addRequestHeader("Accept", "application/json");
    httpGet.addRequestHeader("X-Silverpeas-Session",
        UserDetail.getById(User.getCurrentRequester().getId()).getToken());
    try {
      int statusCode = initHttpClient().executeMethod(httpGet);
      if (statusCode != HttpStatus.SC_OK) {
        throw new WebApplicationException(statusCode);
      }
      final String jsonResponse = httpGet.getResponseBodyAsString();
      return removeJsonParts(jsonResponse);
    } catch (WebApplicationException wae) {
      SilverLogger.getLogger(AlmanachWebManager.class)
          .error("{0} -> HTTP ERROR {1}", serviceUrl, wae.getMessage());
      throw wae;
    } catch (Exception e) {
      SilverLogger.getLogger(AlmanachWebManager.class)
          .error("{0} -> {1}", serviceUrl, e.getMessage());
      throw new WebApplicationException(e);
    } finally {
      httpGet.releaseConnection();
    }
  }

  /**
   * Removes JSON parts which avoid a decoding.
   * @param jsonResponse a JSON response.
   */
  private static String removeJsonParts(final String jsonResponse) {
    String result = jsonResponse;
    for (String jsonParameterToAvoidWhenEmpty : jsonParametersToAvoidWhenEmpty) {
      result = result.replaceAll(",\"" + jsonParameterToAvoidWhenEmpty + "\"[:][{][}]", "");
    }
    return result;
  }

  private static HttpClient initHttpClient() {
    return new HttpClient();
  }
}
