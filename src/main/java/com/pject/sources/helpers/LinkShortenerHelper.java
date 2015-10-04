package com.pject.sources.helpers;

import com.pject.helpers.LogFormatHelper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import twitter4j.JSONException;
import twitter4j.JSONObject;

import java.io.IOException;

/**
 * LinkShortenerHelper - Short description of the class
 *
 * @author Camille
 *         Last: 04/10/2015 13:02
 * @version $Id$
 */
public class LinkShortenerHelper {

    private static final Logger LOGGER = Logger.getLogger(LinkShortenerHelper.class);

    private static final String SHORTENER_GOOGLE_KEY = "AIzaSyB5GOtm_qwdiDQYERaFJv-TooyKTPztDeY";
    private static final String SHORTENER_GOOGLE_URL = "https://www.googleapis.com/urlshortener/v1/url?key=" + SHORTENER_GOOGLE_KEY;

    public static String shorten(String toShorten) {
        return googleShorten(toShorten);
    }

    private static String googleShorten(String toShorten) {
        HttpPost httpPost = new HttpPost(SHORTENER_GOOGLE_URL);
        httpPost.addHeader("Content-Type", "application/json");
        httpPost.setEntity(new StringEntity("{\"longUrl\": \"" + toShorten+ "\"}","UTF-8"));
        try (CloseableHttpClient httpclient = HttpClients.createDefault(); CloseableHttpResponse response = httpclient.execute(httpPost)){
            if(response.getStatusLine().getStatusCode() == 200) {
                String raw = EntityUtils.toString(response.getEntity());
                return new JSONObject(raw).getString("id");
            }
        } catch(IOException|JSONException e) {
            LOGGER.error("Could not google shorten " + toShorten + ": " + LogFormatHelper.formatExceptionMessage(e));
        }
        return toShorten;
    }

}
