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

import java.io.IOException;
import java.util.List;
import java.util.Random;

/**
 * ImgurSource - Short description of the class
 *
 * @author Camille
 *         Last: 27/09/2015 20:21
 * @version $Id$
 */
public class ImgurSource implements Source {

    private static final Logger LOGGER = Logger.getLogger(ImgurSource.class);

    private static final String NAME = "imgur";

    private static final String IMGUR_ROOT_URL      = "http://imgur.com";
    private static final String IMGUR_TRENDING_URL  = IMGUR_ROOT_URL + "/hot/time";
    private static final int MAX_IMGUR_TO_LOAD      = 25;

    private List<String> imgurLinks = Lists.newArrayList();

    public ImgurSource() {
        LOGGER.info("Initializing the imgur source");
        HttpGet httpGet = new HttpGet(IMGUR_TRENDING_URL);
        try (CloseableHttpClient httpclient = HttpClients.createDefault(); CloseableHttpResponse response = httpclient.execute(httpGet)){
            if(response.getStatusLine().getStatusCode() == 200) {
                String raw = EntityUtils.toString(response.getEntity());
                Document doc = Jsoup.parse(raw);
                int i = 0;
                for(Element element : doc.select("a.image-list-link")) {
                    if(i < MAX_IMGUR_TO_LOAD) {
                        String link = IMGUR_ROOT_URL + element.attr("href");
                        imgurLinks.add(link);
                    } else {
                        break;
                    }
                    i++;
                }
            }
        } catch(IOException e) {
            LOGGER.error("Could not load imgur links: " + LogFormatHelper.formatExceptionMessage(e));
        }
        LOGGER.info("Got " + this.imgurLinks.size() + " sources");
    }

    @Override
    public String getTweet() {
        if(this.imgurLinks.size() > 0) {
            String imgurLink = this.imgurLinks.get(new Random().nextInt(this.imgurLinks.size()));
            this.imgurLinks.remove(imgurLink);
            return StringUtils.trimToNull(ExpressionsHelper.getRandomEmojiedExpression() + " " + imgurLink);
        }
        return null;
    }

}
