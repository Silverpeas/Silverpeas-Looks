package org.silverpeas.looks.aurora.servlets;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AjaxServletMeteo extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
		doPost(req, res);
	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
		String woeid = req.getParameter("woeid");

		res.setContentType("text/xml");
		res.setHeader("charset","UTF-8");
		
		Writer writer = res.getWriter();
		writer.write( loadMeteoXML(woeid) );
	}

	/**
	 * Load meteo as a XML stream.
	 * 
	 * @param townId	woeid of town
	 * 
	 * @return 	xml stream
	 * @throws IOException
	 */
	private String loadMeteoXML(String townId) throws IOException {

		String adresse = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20weather.forecast%20where%20woeid%3D'"+townId+"'&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";
			
		BufferedReader reader = null;
		InputStream in = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		try {

			// création de la connection
			URL url = new URL(adresse);
			URLConnection conn = url.openConnection();

			// lecture de la réponse
			in = conn.getInputStream();
			
			reader = new BufferedReader(new InputStreamReader(in));
			byte[] buff = new byte[1024];
			int l = in.read(buff);
			while (l > 0) {
				baos.write(buff, 0, l);
				l = in.read(buff);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				baos.flush();
				baos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				reader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return baos.toString();
	}

}
