package com.pject.persistence;

import org.apache.commons.lang3.StringUtils;
import twitter4j.Status;

import java.util.Properties;

/**
 * Tweets - Short description of the class
 *
 * @author Camille
 *         Last: 04/09/15 10:41
 * @version $Id$
 */
public class Tweets {

    private Properties tweetProperties;

    public Tweets(Properties properties) {
        this.tweetProperties = properties;
    }

    public void markAsSeen(Status status) {
        markAsSeen(status.getId(), status.getText());
    }

    public void markAsSeen(long id, String text) {
        if(!this.tweetProperties.contains(StringUtils.EMPTY + id)) {
            this.tweetProperties.put(StringUtils.EMPTY + id, text);
        }
    }

    public boolean isTweetAlreadyProcessed(Status status) {
        return isTweetAlreadyProcessed(status.getId());
    }

    public boolean isTweetAlreadyProcessed(long id) {
        return this.tweetProperties.containsKey(StringUtils.EMPTY + id);
    }

    public Properties getProperties() {
        return this.tweetProperties;
    }

}
