package com.pject.sources.parsing;

import com.google.common.collect.Lists;
import com.pject.helpers.LogFormatHelper;
import com.pject.helpers.StatsHelper;
import com.pject.sources.Source;
import com.pject.sources.helpers.LinkShortenerHelper;
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
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * SoundcloudSource - Short description of the class
 *
 * @author Camille
 *         Last: 02/10/2015 16:22
 * @version $Id$
 */
public class SoundcloudSource implements Source {

    private static final Logger LOGGER = Logger.getLogger(SoundcloudSource.class);

    private static final String NAME = "soundcloud";

    private static final String CLIENTID = "4ab00a1d604152f05bfa13954f72a937";

    private static final List<String> SETS = Lists.newArrayList("new-music-september",
            "aa5",
            "best-of-september-2015",
            "beats-september-15",
            "rap-hip-hop-workout-playlist",
            "jet-96-selects-6-introducing",
            "tracks-of-september-2015",
            "tbd-fest-2015-playlist",
            "the-best-of-august-2015");

    private static final List<String> PREFIXES = Lists.newArrayList("j'écoute", "listening", "ecoute", "écoute", "bon son", "zik", "track",
            "la melodie...", "du beat", "beeatz", "je vibre");

    private static final int MAX_TRACKS = 100;
    private static final int MAX_COMMENTS = 100;

    private List<String> liveComments = Lists.newArrayList();
    private List<String> sources = Lists.newArrayList();

    public SoundcloudSource() {
        Collections.shuffle(SETS);
        LOGGER.info("Starting the soundcloud source");
        long start = System.currentTimeMillis();
        for(String set : SETS) {
            processSet(set);
            if(this.sources.size() > MAX_TRACKS) {
                break;
            }
        }
        // Mixing up comments
        this.liveComments.addAll(PREFIXES);
        Collections.shuffle(this.liveComments);
        StatsHelper.registerSource(NAME, System.currentTimeMillis() - start, this.sources.size());
        LOGGER.info("Got " + this.sources.size() + " sources");
    }

    private void processSet(String set) {
        HttpGet httpGet = new HttpGet("http://api.soundcloud.com/playlists/" + set + "?client_id=" + CLIENTID);
        try (CloseableHttpClient httpclient = HttpClients.createDefault(); CloseableHttpResponse response = httpclient.execute(httpGet)){
            if(response.getStatusLine().getStatusCode() == 200) {
                String raw = EntityUtils.toString(response.getEntity());
                JSONArray tracks = new JSONObject(raw).getJSONArray("tracks");
                for(int i = 0; i < tracks.length(); i++) {
                    if("public".equals(tracks.getJSONObject(i).getString("sharing"))) {
                        String trackId = tracks.getJSONObject(i).getString("id");
                        String trackUrl = getFullSoundCloudLink(trackId);
                        if(StringUtils.isNotEmpty(trackUrl)) {
                            this.sources.add(trackUrl);
                            if(this.liveComments.size() < MAX_COMMENTS) {
                                getCommentsForTrack(trackId);
                            }
                        }
                    }
                }
            }
        } catch(IOException| JSONException e) {
            LOGGER.error("Could not load soundcloud links: " + LogFormatHelper.formatExceptionMessage(e));
        }
    }

    private String getFullSoundCloudLink(String trackId) {
        HttpGet httpGet = new HttpGet("http://api.soundcloud.com/tracks/" + trackId + "?client_id=" + CLIENTID);
        try (CloseableHttpClient httpclient = HttpClients.createDefault(); CloseableHttpResponse response = httpclient.execute(httpGet)){
            if(response.getStatusLine().getStatusCode() == 200) {
                return new JSONObject(EntityUtils.toString(response.getEntity())).getString("permalink_url");
            }
        } catch(IOException| JSONException e) {
            LOGGER.error("Could not load soundcloud track: " + LogFormatHelper.formatExceptionMessage(e));
        }
        return null;
    }

    private void getCommentsForTrack(String trackId) {
        HttpGet httpGet = new HttpGet("http://api.soundcloud.com/tracks/" + trackId + "/comments?client_id=" + CLIENTID);
        try (CloseableHttpClient httpclient = HttpClients.createDefault(); CloseableHttpResponse response = httpclient.execute(httpGet)){
            if(response.getStatusLine().getStatusCode() == 200) {
                String raw = EntityUtils.toString(response.getEntity());
                JSONArray comments = new JSONArray(raw);
                for(int i = 0; i < comments.length(); i++) {
                    if("comment".equals(comments.getJSONObject(i).get("kind"))) {
                        this.liveComments.add(comments.getJSONObject(i).getString("body"));
                    }
                }
            }
        } catch(IOException| JSONException e) {
            LOGGER.error("Could not load soundcloud comments: " + LogFormatHelper.formatExceptionMessage(e));
        }
    }

    private String getCommentForSize(int maxSize) {
        String foundComment = null;
        for(String comment : this.liveComments) {
            if(comment.length() < maxSize) {
                foundComment = comment;
                break;
            }
        }
        if(foundComment != null) {
            this.liveComments.remove(foundComment);
        }
        return foundComment;
    }

    @Override
    public String getTweet() {
        if(this.sources.size() > 0) {
            String source = this.sources.get(new Random().nextInt(this.sources.size()));
            this.sources.remove(source);
            // Building it up
            String link = LinkShortenerHelper.shorten(source);
            return getCommentForSize(139 - link.length()) + " " + link;
        }
        return null;
    }

}
