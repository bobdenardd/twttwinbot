package com.pject.sources.file;

import com.pject.helpers.StatsHelper;
import com.pject.sources.Source;
import org.apache.log4j.Logger;

/**
 * QuotesSource - Short description of the class
 *
 * @author Camille
 *         Last: 27/09/2015 16:32
 * @version $Id$
 */
public class QuotesSource extends FileSource implements Source {

    private static final Logger LOGGER = Logger.getLogger(QuotesSource.class);

    private static final String NAME = "quotes";

    private static final String QUOTES_FILE_NAME    = "citations.txt";
    private static final int NB_QUOTES_TO_LOAD      = 50;

    public QuotesSource() {
        LOGGER.info("Initializing the quotes source");
        Long start = System.currentTimeMillis();
        downAndLoadQuotes(QUOTES_FILE_NAME, NB_QUOTES_TO_LOAD);
        StatsHelper.registerSource(NAME, System.currentTimeMillis() - start, this.sources.size());
        LOGGER.info("Got " + this.sources.size() + " sources");
    }

}
