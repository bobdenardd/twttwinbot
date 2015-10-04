package com.pject.sources.parsing;

import com.google.common.collect.Lists;
import com.pject.helpers.BotPropertiesHelper;
import com.pject.helpers.LogFormatHelper;
import com.pject.helpers.StatsHelper;
import com.pject.sources.Source;
import com.pject.sources.helpers.ExpressionsHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import twitter4j.JSONArray;
import twitter4j.JSONException;
import twitter4j.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * YoutubeSource - Short description of the class
 *
 * @author Camille
 *         Last: 04/10/2015 20:54
 * @version $Id$
 */
public class YoutubeSource implements Source {

    private static final Logger LOGGER = Logger.getLogger(YoutubeSource.class);

    private static final String NAME = "youtube";

    private static final String YOUTUBE_URL = "https://www.googleapis.com/youtube/v3/search?maxResults=50&part=snippet&q=";

    private static final List<String> SEARCH_TERMS = Lists.newArrayList("puppy", "cute", "makeup", "maquillage", "clash", "mode", "fashion", "kitty");

    private List<String> sources = Lists.newArrayList();

    public YoutubeSource() {
        LOGGER.info("Initializing youtube source");
        long start = System.currentTimeMillis();
        for(String searchTerm : SEARCH_TERMS) {
            searchForTerm(searchTerm);
        }
        StatsHelper.registerSource(NAME, System.currentTimeMillis() - start, this.sources.size());
        LOGGER.info("Got " + this.sources.size() + " sources");
    }

    private void searchForTerm(String term) {
        if(StringUtils.isNotEmpty(BotPropertiesHelper.getYoutubeKey())) {
            HttpGet httpGet = new HttpGet(YOUTUBE_URL + term + "&key=" + BotPropertiesHelper.getYoutubeKey() + "&publishedBefore=" + getBefore());
            try (CloseableHttpClient httpclient = HttpClients.createDefault(); CloseableHttpResponse response = httpclient.execute(httpGet)) {
                if (response.getStatusLine().getStatusCode() == 200) {
                    String raw = EntityUtils.toString(response.getEntity());
                    JSONArray items = new JSONObject(raw).getJSONArray("items");
                    for(int i = 0; i < items.length(); i++) {
                        if(items.getJSONObject(i).getJSONObject("id").has("videoId")) {
                            String videoId = items.getJSONObject(i).getJSONObject("id").getString("videoId");
                            if(StringUtils.isNotEmpty(videoId)) {
                                this.sources.add("http://www.youtube.com/watch?v=" + videoId);
                            }
                        }
                    }
                }
            } catch (IOException| JSONException e) {
                LOGGER.error("Could not load youtube links: " + LogFormatHelper.formatExceptionMessage(e));
            }
        }
    }

    private String getBefore() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -2);
        return (URLEncoder.encode(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(calendar.getTime())));
    }

    @Override
    public String getTweet() {
        if(this.sources.size() > 0) {
            String imgurLink = this.sources.get(RANDOM.nextInt(this.sources.size()));
            this.sources.remove(imgurLink);
            return StringUtils.trimToNull(ExpressionsHelper.getRandomEmojiedExpression() + " " + imgurLink);
        }
        return null;
    }

}
