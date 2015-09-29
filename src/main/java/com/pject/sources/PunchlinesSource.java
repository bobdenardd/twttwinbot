package com.pject.sources;

import org.apache.log4j.Logger;

/**
 * PunchlinesSource - Short description of the class
 *
 * @author Camille
 *         Last: 29/09/2015 17:21
 * @version $Id$
 */
public class PunchlinesSource extends FileSource implements Source {

    private static final Logger LOGGER = Logger.getLogger(PunchlinesSource.class);

    private static final String NAME = "punchlines";

    private static final String PUNCHLINES_FILE_NAME    = "punchlines.txt";
    private static final int NB_PUNCHLINES_TO_LOAD      = 50;

    public PunchlinesSource() {
        LOGGER.info("Initializing the punchlines source");
        downAndLoadQuotes(PUNCHLINES_FILE_NAME, NB_PUNCHLINES_TO_LOAD);
        LOGGER.info("Got " + this.sources.size() + " sources");
    }


}
