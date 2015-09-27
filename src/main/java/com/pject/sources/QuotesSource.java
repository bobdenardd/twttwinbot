package com.pject.sources;

import com.google.common.collect.Lists;
import com.pject.helpers.DropBoxHelper;
import com.pject.helpers.LogFormatHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.List;
import java.util.Random;

/**
 * QuotesSource - Short description of the class
 *
 * @author Camille
 *         Last: 27/09/2015 16:32
 * @version $Id$
 */
public class QuotesSource implements Source {

    private static final Logger LOGGER = Logger.getLogger(QuotesSource.class);

    private static final String NAME = "quotes";

    private static final String QUOTES_FILE_NAME    = "citations.txt";
    private static final String REMOTE_ROOT_DBOX    = "/twttwinbot";
    private static final int NB_QUOTES_TO_LOAD      = 20;

    private List<String> quotes = Lists.newArrayList();

    public QuotesSource() {
        LOGGER.info("Initializing the quotes source");
        downAndLoadQuotes();
    }

    private void downAndLoadQuotes() {
        File quotesFile = new File(QUOTES_FILE_NAME);
        quotesFile.deleteOnExit();
        DropBoxHelper.downloadFile(DropBoxHelper.getRemoteFile(REMOTE_ROOT_DBOX, QUOTES_FILE_NAME), quotesFile);
        try (LineNumberReader lnr = new LineNumberReader(new FileReader(quotesFile))) {
            lnr.skip(Long.MAX_VALUE);
            int totalQuotesNumber = lnr.getLineNumber();

            for(int i = 0; i < NB_QUOTES_TO_LOAD; i++) {
                String quote;
                try (BufferedReader bufferedReader = new BufferedReader(new FileReader(quotesFile))) {
                    String line;
                    int counter = 0;
                    while((line = bufferedReader.readLine()) != null) {
                        counter++;
                        if(counter == new Random().nextInt(totalQuotesNumber)) {
                            if(StringUtils.isNotEmpty(line)) {
                                this.quotes.add(line);
                            }
                        }
                        counter++;
                    }
                }
            }
        } catch(Exception e) {
            LOGGER.error("Could not load quotes: " + LogFormatHelper.formatExceptionMessage(e), e);
        }
    }

    @Override
    public String getTweet() {
        if(this.quotes.size() > 0) {
            return this.quotes.get(new Random().nextInt(this.quotes.size()));
        }
        return null;
    }

}
