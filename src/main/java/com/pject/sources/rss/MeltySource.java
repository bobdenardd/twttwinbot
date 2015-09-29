package com.pject.sources.rss;

import com.google.common.collect.Lists;
import com.pject.sources.Source;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * MeltySource - Short description of the class
 *
 * @author Camille
 *         Last: 28/09/2015 13:23
 * @version $Id$
 */
public class MeltySource extends RssSource implements Source {

    private static final Logger LOGGER = Logger.getLogger(MeltySource.class);

    private static final String NAME = "melty";

    private static final String ROOT = "http://www.melty.fr";
    private static final List<String> RSS = Lists.newArrayList("http://www.melty.fr/tele-realite-23.rss",
            "http://www.melty.fr/jeux-video-1.rss",
            "http://www.melty.fr/people-13.rss",
            "http://www.melty.fr/musique-4.rss",
            "http://www.melty.fr/series-5.rss"
    );

    private static final int MAX_MELTY_LINKS_PER_RSS = 25;

    public MeltySource() {
        LOGGER.info("Initializing melty source");
        for(String meltyRss : RSS) {
            processRss(meltyRss, MAX_MELTY_LINKS_PER_RSS);
        }
        LOGGER.info("Got " + this.sources.size() + " sources");
    }

    @Override
    public String processLink(String link) {
        return StringUtils.isNotEmpty(link.replace(ROOT, StringUtils.EMPTY)) || StringUtils.isNotEmpty(link.replace(ROOT + "/", StringUtils.EMPTY)) ? link : null;
    }

}
