package com.pject.sources.rss;

import com.google.common.collect.Lists;
import com.pject.helpers.LogFormatHelper;
import com.pject.sources.Source;
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
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;

/**
 * RssSource - Short description of the class
 *
 * @author Camille
 *         Last: 29/09/2015 21:54
 * @version $Id$
 */
public abstract class RssSource implements Source {

    private static final Logger LOGGER = Logger.getLogger(RssSource.class);

    private static final String NAME = StringUtils.EMPTY;

    protected List<String> sources = Lists.newArrayList();

    protected void processRss(String rss, int maxSourcePerRss) {
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("Processing rss " + rss);
        }
        HttpGet httpGet = new HttpGet(rss);
        try (CloseableHttpClient httpclient = HttpClients.createDefault(); CloseableHttpResponse response = httpclient.execute(httpGet)){
            if(response.getStatusLine().getStatusCode() == 200) {
                String raw = EntityUtils.toString(response.getEntity());
                Document doc = Jsoup.parse(raw, StringUtils.EMPTY, Parser.xmlParser());
                int i = 0;
                for(Element element : doc.select("item")) {
                    if(i < maxSourcePerRss) {
                        Elements links = element.select("link");
                        if(links != null && links.size() > 0) {
                            String processedLink = processLink(links.get(0).text());
                            if (StringUtils.isNotEmpty(processedLink)) {
                                this.sources.add(processedLink);
                            }
                        }
                    } else {
                        break;
                    }
                    i++;
                }
            }
        } catch(IOException e) {
            LOGGER.error("Could not load rss links: " + LogFormatHelper.formatExceptionMessage(e));
        }
    }

    protected abstract String processLink(String link);

    public String getTweet() {
        if(this.sources.size() > 0) {
            String rssLink = this.sources.get(RANDOM.nextInt(this.sources.size()));
            this.sources.remove(rssLink);
            return StringUtils.trimToNull(ExpressionsHelper.getRandomEmojiedExpression() + " " + rssLink);
        }
        return null;
    }

}
