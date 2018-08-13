package org.silverpeas.looks.aurora.servlets;

import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;

public class AjaxServletMeteo extends HttpServlet {

  private static final long serialVersionUID = 1L;

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res) {
    doPost(req, res);
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res) {
    try {
      String woeid = req.getParameter("woeid");

      res.setContentType("text/xml");
      res.setHeader("charset", "UTF-8");

      Writer writer = res.getWriter();
      writer.write(loadWeatherXML(woeid));
    } catch (IOException e) {
      res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Load the weather of the specified town as a XML stream.
   * @param townId unique identifier of the town in the weather map.
   * @return xml stream or nothing if the the request fails.
   */
  private String loadWeatherXML(String townId) {
    String weatherUrl =
        "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20weather" +
            ".forecast%20where%20woeid%3D'" +
            townId + "'&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";
    try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      URL url = new URL(weatherUrl);
      URLConnection conn = url.openConnection();
      read(conn, baos);
      baos.flush();
      return baos.toString(Charsets.UTF_8.name());
    } catch (IOException e) {
      SilverLogger.getLogger(this).error(e);
      return "";
    }
  }

  private void read(final URLConnection conn, final ByteArrayOutputStream baos) {
    try(InputStream in = conn.getInputStream()) {
      byte[] buff = new byte[1024];
      int l = in.read(buff);
      while (l > 0) {
        baos.write(buff, 0, l);
        l = in.read(buff);
      }
    } catch (IOException e) {
      SilverLogger.getLogger(this).error(e);
    }
  }

}
