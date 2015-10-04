package com.pject.sources.file;

import com.google.common.collect.Lists;
import com.pject.helpers.DropBoxHelper;
import com.pject.helpers.LogFormatHelper;
import com.pject.sources.Source;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.security.SecureRandom;
import java.util.List;

/**
 * FileSource - Short description of the class
 *
 * @author Camille
 *         Last: 29/09/2015 17:18
 * @version $Id$
 */
public abstract class FileSource implements Source {

    private static final Logger LOGGER = Logger.getLogger(FileSource.class);

    private static final String NAME = StringUtils.EMPTY;

    private static final String REMOTE_ROOT_DBOX    = "/twttwinbot";

    protected List<String> sources = Lists.newArrayList();

    protected void downAndLoadQuotes(String fileName, int maxToLoad) {
        File quotesFile = new File(fileName);
        quotesFile.deleteOnExit();
        DropBoxHelper.downloadFile(DropBoxHelper.getRemoteFile(REMOTE_ROOT_DBOX, fileName), quotesFile);
        try (LineNumberReader lnr = new LineNumberReader(new FileReader(quotesFile))) {
            lnr.skip(Long.MAX_VALUE);
            int totalQuotesNumber = lnr.getLineNumber();

            for(int i = 0; i < maxToLoad; i++) {
                try (BufferedReader bufferedReader = new BufferedReader(new FileReader(quotesFile))) {
                    String line;
                    int counter = 0;
                    while((line = bufferedReader.readLine()) != null) {
                        if(counter == RANDOM.nextInt(totalQuotesNumber)) {
                            if(StringUtils.isNotEmpty(line)) {
                                this.sources.add(line);
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

    public String getTweet() {
        if(this.sources.size() > 0) {
            String quote = this.sources.get(new SecureRandom().nextInt(this.sources.size()));
            this.sources.remove(quote);
            return quote;
        }
        return null;
    }

}
