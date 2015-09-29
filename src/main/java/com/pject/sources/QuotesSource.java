package com.pject.sources;

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
        downAndLoadQuotes(QUOTES_FILE_NAME, NB_QUOTES_TO_LOAD);
        LOGGER.info("Got " + this.sources.size() + " sources");
    }

}
