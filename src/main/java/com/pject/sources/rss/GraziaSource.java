package com.pject.sources.rss;

import com.google.common.collect.Lists;
import com.pject.helpers.StatsHelper;
import com.pject.sources.Source;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * GraziaSource - Short description of the class
 *
 * @author Camille
 *         Last: 29/09/2015 22:56
 * @version $Id$
 */
public class GraziaSource extends RssSource implements Source {

    /*
    I really do hope that one day this bot will be able to print out all this stuff coming from grazia so that
    I can enjoy wiping my ass with it.
    */

    private static final Logger LOGGER = Logger.getLogger(GraziaSource.class);

    private static final String NAME = "grazia";

    private static final String ROOT = "http://www.grazia.fr";
    private static final List<String> RSS = Lists.newArrayList("http://www.grazia.fr/feed/list/rss-au-quotidien/(limit)/30",
            "http://www.grazia.fr/feed/list/rss-beaute/(limit)/30");

    private static final int MAX_GRAZIA_LINKS_PER_RSS = 10;

    public GraziaSource() {
        LOGGER.info("Initializing grazia source");
        long start = System.currentTimeMillis();
        for(String meltyRss : RSS) {
            processRss(meltyRss, MAX_GRAZIA_LINKS_PER_RSS);
        }
        StatsHelper.registerSource(NAME, System.currentTimeMillis() - start, this.sources.size());
        LOGGER.info("Got " + this.sources.size() + " sources");
    }

    @Override
    public String processLink(String link) {
        return StringUtils.isNotEmpty(link.replace(ROOT, StringUtils.EMPTY)) || StringUtils.isNotEmpty(link.replace(ROOT + "/", StringUtils.EMPTY)) ? link : null;
    }

}
