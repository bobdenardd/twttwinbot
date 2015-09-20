package com.pject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * BotSetup - Short description of the class
 *
 * @author Camille
 *         Last: 19/09/15 22:49
 * @version $Id$
 */
public interface BotSetup {

    // Defining hard coded properties
    static final String REMOTE_ROOT_DBOX    = "/twttwinbot";
    static final String TWEETS_FILE         = "tweets.dat";
    static final String USERS_FILE          = "users.dat";

    static final String SCHEDULING_FILE_PROP_NAME   = "scheduling";
    static final String DEFAULT_SCHEDULING_FILE     = "scheduling.properties";

    static final String PROPERTIES_FILE_PROP_NAME   = "properties";
    static final String DEFAULT_PROPERTIES_FILE     = "bot.properties";

    static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-YYYY_HH-mm");

}
