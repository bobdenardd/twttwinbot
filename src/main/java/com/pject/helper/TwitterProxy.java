package com.pject.helper;

import com.google.common.collect.Maps;
import com.pject.exceptions.NoRemainingException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.RateLimitStatus;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

import java.util.Map;

/**
 * TwitterProxy - Short description of the class
 *
 * @author Camille
 *         Last: 04/09/15 15:10
 * @version $Id$
 */
public class TwitterProxy {

    private static final Logger LOGGER = Logger.getLogger(TwitterProxy.class);

    private static Map<ApiCall, Integer> remaining = Maps.newHashMap();

    public static void init(Twitter twitter) throws TwitterException {
        for (Map.Entry<String, RateLimitStatus> entry : twitter.getRateLimitStatus().entrySet()) {
            ApiCall method = ApiCall.forName(entry.getKey());
            if(method != null) {
                remaining.put(method, entry.getValue().getRemaining());
            }
        }

        // Hack here regarding retweet and follow
        remaining.put(ApiCall.RETWEET, remaining.get(ApiCall.RETWEET) + 100);
        remaining.put(ApiCall.FOLLOW, remaining.get(ApiCall.FOLLOW) + 100);

        // Inspection
        if(LOGGER.isDebugEnabled()) {
            for(ApiCall method : ApiCall.values()) {
                LOGGER.debug(StringUtils.rightPad(method.name(), 20) + remaining.get(method));
            }
        }
    }

    public static QueryResult search(Twitter twitter, Query query) throws TwitterException, NoRemainingException {
        if(remaining.get(ApiCall.SEARCH) > 0) {
            consume(ApiCall.SEARCH);
            return twitter.search(query);
        }
        throw new NoRemainingException(ApiCall.SEARCH);
    }

    public static void retweet(Twitter twitter, Status status) throws TwitterException, NoRemainingException {
        if(remaining.get(ApiCall.RETWEET) > 0) {
            consume(ApiCall.RETWEET);
            twitter.retweetStatus(status.getId());
            return;
        }
        throw new NoRemainingException(ApiCall.RETWEET);
    }

    public static User follow(Twitter twitter, String userName, boolean follow) throws TwitterException, NoRemainingException {
        if(remaining.get(ApiCall.FOLLOW) > 0) {
            consume(ApiCall.FOLLOW);
            return twitter.createFriendship(userName, follow);
        }
        throw new NoRemainingException(ApiCall.FOLLOW);
    }

    private static void consume(ApiCall method) {
        remaining.put(method, remaining.get(method) - 1);
    }

}
