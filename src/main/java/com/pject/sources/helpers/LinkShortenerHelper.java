package com.pject.sources.helpers;

import com.pject.helpers.BotPropertiesHelper;
import com.pject.helpers.LogFormatHelper;
import net.swisstech.bitly.BitlyClient;
import net.swisstech.bitly.model.Response;
import org.apache.commons.lang3.StringUtils;
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
import java.security.SecureRandom;

/**
 * LinkShortenerHelper - Short description of the class
 *
 * @author Camille
 *         Last: 04/10/2015 13:02
 * @version $Id$
 */
public class LinkShortenerHelper {

    private static final Logger LOGGER = Logger.getLogger(LinkShortenerHelper.class);

    private static final String SHORTENER_GOOGLE_URL = "https://www.googleapis.com/urlshortener/v1/url?key=";

    private static BitlyClient bitlyClient = null;

    public static String shorten(String toShorten) {
        switch (new SecureRandom().nextInt(2)) {
            case 0:
                return googleShorten(toShorten);
            default:
                return bitlyShorten(toShorten);
        }
    }

    private static String googleShorten(String toShorten) {
        if(StringUtils.isNotEmpty(BotPropertiesHelper.getGooGlKey())) {
            HttpPost httpPost = new HttpPost(SHORTENER_GOOGLE_URL + BotPropertiesHelper.getGooGlKey());
            httpPost.addHeader("Content-Type", "application/json");
            httpPost.setEntity(new StringEntity("{\"longUrl\": \"" + toShorten + "\"}", "UTF-8"));
            try (CloseableHttpClient httpclient = HttpClients.createDefault(); CloseableHttpResponse response = httpclient.execute(httpPost)) {
                if (response.getStatusLine().getStatusCode() == 200) {
                    String raw = EntityUtils.toString(response.getEntity());
                    return new JSONObject(raw).getString("id");
                }
            } catch (IOException | JSONException e) {
                LOGGER.error("Could not google shorten " + toShorten + ": " + LogFormatHelper.formatExceptionMessage(e));
            }
        }
        return toShorten;
    }

    private static String bitlyShorten(String toShorten) {
        if(StringUtils.isNotEmpty(BotPropertiesHelper.getBitlyKey())) {
            if (bitlyClient == null) {
                bitlyClient = new BitlyClient(BotPropertiesHelper.getBitlyKey());
            }
            Response respShort = bitlyClient.shorten()
                    .setLongUrl(toShorten)
                    .call();
            try {
                return new JSONObject(respShort.data.toString()).getString("url");
            } catch (Exception e) {
                LOGGER.error("Could not bitly shorten " + toShorten + ": " + LogFormatHelper.formatExceptionMessage(e));
            }
        }
        return toShorten;
    }

}
