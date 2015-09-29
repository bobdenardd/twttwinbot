package com.pject.sources;

import com.google.common.collect.Lists;
import com.pject.helpers.LogFormatHelper;
import com.pject.sources.helpers.ExpressionsHelper;
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
import org.jsoup.parser.Parser;

import java.io.IOException;
import java.util.List;
import java.util.Random;

/**
 * MeltySource - Short description of the class
 *
 * @author Camille
 *         Last: 28/09/2015 13:23
 * @version $Id$
 */
public class MeltySource implements Source {

    private static final Logger LOGGER = Logger.getLogger(MeltySource.class);

    private static final String NAME = "melty";

    private static final List<String> RSS = Lists.newArrayList("http://www.melty.fr/tele-realite-23.rss",
            "http://www.melty.fr/jeux-video-1.rss",
            "http://www.melty.fr/people-13.rss",
            "http://www.melty.fr/musique-4.rss",
            "http://www.melty.fr/series-5.rss"
    );

    private static final int MAX_MELTY_LINKS_PER_RSS = 25;

    private List<String> sources = Lists.newArrayList();

    public MeltySource() {
        LOGGER.info("Initializing melty source");
        for(String meltyRss : RSS) {
            processRss(meltyRss);
        }
        LOGGER.info("Got " + this.sources.size() + " sources");
    }

    private void processRss(String rss) {
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("Processing rss " + rss);
        }
        HttpGet httpGet = new HttpGet(rss);
        try (CloseableHttpClient httpclient = HttpClients.createDefault(); CloseableHttpResponse response = httpclient.execute(httpGet)){
            if(response.getStatusLine().getStatusCode() == 200) {
                String raw = EntityUtils.toString(response.getEntity());
                Document doc = Jsoup.parse(raw, StringUtils.EMPTY, Parser.xmlParser());
                int i = 0;
                for(Element element : doc.select("link")) {
                    if(i < MAX_MELTY_LINKS_PER_RSS) {
                        String link = element.text();
                        if(StringUtils.isNotEmpty(link) && StringUtils.isNotEmpty(link.replace("http://www.melty.fr/", StringUtils.EMPTY))) {
                            sources.add(link);
                        }
                    } else {
                        break;
                    }
                    i++;
                }
            }
        } catch(IOException e) {
            LOGGER.error("Could not load melty links: " + LogFormatHelper.formatExceptionMessage(e));
        }
    }

    @Override
    public String getTweet() {
        if(this.sources.size() > 0) {
            String meltyLink = this.sources.get(new Random().nextInt(this.sources.size()));
            this.sources.remove(meltyLink);
            return StringUtils.trimToNull(ExpressionsHelper.getRandomEmojiedExpression() + " " + meltyLink);
        }
        return null;
    }

}
