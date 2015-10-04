package com.pject.bot;

import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Random;

/**
 * BotSetup - Short description of the class
 *
 * @author Camille
 *         Last: 19/09/15 22:49
 * @version $Id$
 */
public interface BotSetup {

    String REMOTE_ROOT_DBOX             = "/twttwinbot";
    String TWEETS_FILE                  = "tweets.dat";
    String USERS_FILE                   = "users.dat";

    String SCHEDULING_FILE_PROP_NAME    = "scheduling";
    String DEFAULT_SCHEDULING_FILE      = "scheduling.properties";

    String PROPERTIES_FILE_PROP_NAME    = "properties";
    String DEFAULT_PROPERTIES_FILE      = "bot.properties";

    DateFormat DATE_FORMAT              = new SimpleDateFormat("dd-MM-YYYY_HH-mm");
    Random RANDOM                       = new SecureRandom();

    String SEARCH_QUERY                 = "concours OR gagner";
    int MAX_TWEETS_RESULTS              = 100;

    int MAX_UNFOLLOWS_PER_RUN           = 8;
    float FOLLOWED_TO_FOLLOWER_RATIO    = 0.25f;

    String SOURCES_PACKAGE              = "com.pject.sources";
    int MAX_SOURCE_TRIES                = 15;

    int REAL_TWEETS_BEGINING_MAX        = 4;
    int REAL_TWEETS_BEGINNING_MIN       = 2;

    int REAL_TWEETS_END_MAX             = 5;
    int REAL_TWEETS_END_MIN             = 2;

    int REAL_TWEETS_MIDDLE_MAX          = 15;
    int REAL_TWEETS_MIDDLE_MIN          = 8;

}
