package com.pject.sources.parsing;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;
import com.pject.helpers.LogFormatHelper;
import com.pject.sources.Source;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;
import java.util.Random;

/**
 * InstagramSource - Short description of the class
 *
 * @author Camille
 *         Last: 29/09/2015 15:06
 * @version $Id$
 */
public class InstagramSource implements Source {

    private static final Logger LOGGER = Logger.getLogger(InstagramSource.class);

    private static final String NAME = "instagram";

    private static final List<String> INSTA_URLS = Lists.newArrayList(
            "http://www.hashtagig.com/analytics/instagood",
            "http://www.hashtagig.com/analytics/love",
            "http://www.hashtagig.com/analytics/photooftheday",
            "http://www.hashtagig.com/analytics/beautiful"
    );

    private static final int MAX_INSTAS_PER_TAG = 35;

    private List<String> sources = Lists.newArrayList();

    public InstagramSource() {
        LOGGER.info("Initializing instagram source");
        for(String instaUrl : INSTA_URLS) {
            processHashTag(instaUrl);
        }
        LOGGER.info("Got " + this.sources.size() + " sources");
    }

    private void processHashTag(String hashTagUrl) {
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("Processing hashtag analytics " + hashTagUrl);
        }
        HttpGet httpGet = new HttpGet(hashTagUrl);
        try (CloseableHttpClient httpclient = HttpClients.createDefault(); CloseableHttpResponse response = httpclient.execute(httpGet)){
            if(response.getStatusLine().getStatusCode() == 200) {
                String raw = EntityUtils.toString(response.getEntity());
                Document doc = Jsoup.parse(raw);
                int i = 0;
                for(Element element : doc.select("div.snapshot")) {
                    if(i > MAX_INSTAS_PER_TAG) {
                        break;
                    }
                    // Retrieving image
                    String imageUrl = null;
                    List<String> hashtags = null;
                    Elements elements = element.select("img");
                    if(elements.size() > 0) {
                        imageUrl = elements.get(0).attr("src");
                        hashtags = extractHashTags(elements.get(0).attr("title"));
                    }

                    // Building source
                    String source = mergeTagsWithPicture(imageUrl, hashtags);
                    if(StringUtils.isNotEmpty(source)) {
                        this.sources.add(source);
                        i++;
                    }
                }
            }
        } catch(IOException e) {
            LOGGER.error("Could not load instagram links: " + LogFormatHelper.formatExceptionMessage(e));
        }
    }

    private List<String> extractHashTags(String text) {
        List<String> result = Lists.newArrayList();
        if(StringUtils.isNotEmpty(text) && text.contains("#")) {
            for(String token : text.split("#")) {
                String potentialToken = StringUtils.trimToEmpty(token.replaceAll(" .*", StringUtils.EMPTY));
                if(StringUtils.isNotEmpty(potentialToken)) {
                    result.add("#" + potentialToken);
                }
            }
        }
        return result;
    }

    private String mergeTagsWithPicture(String pictureUrl, List<String> hashtags) {
        if(StringUtils.isEmpty(pictureUrl) || hashtags == null) {
            return null;
        }

        // Preordering the list of hashtags
        Ordering<String> o = new Ordering<String>() {
            @Override
            public int compare(String left, String right) {
                return Ints.compare(left.length(), right.length());
            }
        };

        // Merging stuff depending on the size
        int possible = 139 - pictureUrl.length();
        StringBuilder source = new StringBuilder();
        while (possible > 0 && hashtags.size() > 0) {
            String hash = o.min(hashtags);
            if(possible - hash.length() - 1 > 0) {
                source.append(hash).append(" ");
                hashtags.remove(hash);
                possible = possible - hash.length() - 1;
            } else {
                break;
            }
        }
        source.setLength(Math.max(source.length() - 1, 0));
        source.append(" ").append(pictureUrl);
        return source.toString();
    }

    @Override
    public String getTweet() {
        if(this.sources.size() > 0) {
            String source =  this.sources.get(new Random().nextInt(this.sources.size()));
            this.sources.remove(source);
            return source;
        }
        return null;
    }

}
