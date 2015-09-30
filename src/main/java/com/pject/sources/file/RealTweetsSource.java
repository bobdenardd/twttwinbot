package com.pject.sources.file;

import com.pject.sources.Source;
import org.apache.log4j.Logger;

/**
 * RealTweetsSource - Short description of the class
 *
 * @author Camille
 *         Last: 30/09/2015 12:17
 * @version $Id$
 */
public class RealTweetsSource extends FileSource implements Source {

    private static final Logger LOGGER = Logger.getLogger(RealTweetsSource.class);

    private static final String NAME = "real";

    private static final String REALTWEETS_FILE_NAME    = "conversations.txt";
    private static final int NB_REALTWEETS_TO_LOAD      = 60;

    public RealTweetsSource() {
        LOGGER.info("Initializing the real source");
        downAndLoadQuotes(REALTWEETS_FILE_NAME, NB_REALTWEETS_TO_LOAD);
        LOGGER.info("Got " + this.sources.size() + " sources");
    }

}
