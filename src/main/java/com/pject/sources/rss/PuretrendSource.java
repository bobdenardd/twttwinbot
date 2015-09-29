package com.pject.sources.rss;

import com.pject.sources.file.PunchlinesSource;
import com.pject.sources.Source;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * PuretrendSource - Short description of the class
 *
 * @author Camille
 *         Last: 29/09/2015 22:48
 * @version $Id$
 */
public class PuretrendSource extends RssSource implements Source {

    private static final Logger LOGGER = Logger.getLogger(PunchlinesSource.class);

    private static final String NAME = "puretrend";

    private static final String ROOT = "http://www.puretrend.com";
    private static final String RSS = "http://www.puretrend.com/rss/news_t0.xml";
    private static final int MAX_RSS = 30;

    public PuretrendSource() {
        LOGGER.info("Initializing puretrend source");
        processRss(RSS, MAX_RSS);
        LOGGER.info("Got " + this.sources.size() + " sources");
    }

    @Override
    public String processLink(String link) {
        return StringUtils.isNotEmpty(link.replace(ROOT, StringUtils.EMPTY)) || StringUtils.isNotEmpty(link.replace(ROOT + "/", StringUtils.EMPTY)) ? link : null;
    }

}
