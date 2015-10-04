package com.pject.sources.rss;

import com.google.common.collect.Lists;
import com.pject.helpers.StatsHelper;
import com.pject.sources.Source;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * ElleSource - Short description of the class
 *
 * @author Camille
 *         Last: 29/09/2015 21:57
 * @version $Id$
 */
public class ElleSource extends RssSource implements Source {

    private static final Logger LOGGER = Logger.getLogger(ElleSource.class);

    private static final String NAME = "elle";

    private static final List<String> RSS = Lists.newArrayList("http://cdn-elle.ladmedia.fr/var/plain_site/storage/flux_rss/fluxMode.xml",
            "http://cdn-elle.ladmedia.fr/var/plain_site/storage/flux_rss/fluxBeaute.xml",
            "http://cdn-elle.ladmedia.fr/var/plain_site/storage/flux_rss/fluxPsychoSexo.xml",
            "http://cdn-elle.ladmedia.fr/var/plain_site/storage/flux_rss/fluxPeople.xml");

    private static final int MAX_PER_RSS = 30;

    public ElleSource() {
        LOGGER.info("Initializing elle source");
        long start = System.currentTimeMillis();
        for(String rss : RSS) {
            processRss(rss, MAX_PER_RSS);
        }
        StatsHelper.registerSource(NAME, System.currentTimeMillis() - start, this.sources.size());
        LOGGER.info("Got " + this.sources.size() + " sources");
    }

    @Override
    public String processLink(String link) {
        if(StringUtils.isNotEmpty(link)) {
            return StringUtils.isNotEmpty(link.replace("http://www.elle.fr", StringUtils.EMPTY)) ? link.replaceAll("#.*", StringUtils.EMPTY) : null;
        }
        return null;
    }

}
