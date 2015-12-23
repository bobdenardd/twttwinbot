package com.pject.twitter;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TweetAnalyzer - Short description of the class
 *
 * @author Camille
 *         Last: 07/09/15 10:37
 * @version $Id$
 */
public class TweetAnalyzer {

    private static final Logger LOGGER = Logger.getLogger(TweetAnalyzer.class);

    private static final Pattern USER_PATTERN   = Pattern.compile("@([a-z0-9_]+)");
    private static final Pattern RT_PATTERN     = Pattern.compile("(?i).*(\\+| )(rt|retweet)(\\+| ).*");
    private static final String SPACE           = " ";

    public static boolean needsRetweet(String tweet) {
        return StringUtils.isNotEmpty(tweet) && RT_PATTERN.matcher(singleLine(tweet)).matches();
    }

    public static boolean needsFollow(String tweet) {
        return StringUtils.isNotEmpty(tweet) && (StringUtils.containsIgnoreCase(singleLine(tweet), "follow ") || StringUtils.containsIgnoreCase(singleLine(tweet), "fl "));
    }

    public static List<String> extractUsersToFollow(String tweet) {
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("Extracting users to follow from tweet: " + tweet);
        }
        if (StringUtils.isNotEmpty(tweet)) {
            List<String> result = Lists.newArrayList();

            // Normalizing tweet
            tweet = singleLine(tweet.toLowerCase());
            int index = StringUtils.containsIgnoreCase(tweet, "follow") ? tweet.lastIndexOf("follow") : (StringUtils.containsIgnoreCase(tweet, "fl ") ? tweet.lastIndexOf("fl ") : 0);
            tweet = tweet.substring(index, tweet.length());
            Matcher matcher = USER_PATTERN.matcher(tweet);

            // Applying user regex
            while (matcher.find()) {
                String user = matcher.group();
                if (!result.contains(user)) {
                    result.add(user.replaceFirst("@", StringUtils.EMPTY));
                }
            }
            if(LOGGER.isDebugEnabled()) {
                LOGGER.debug("Found " + result.size() + " users to follow: " + Joiner.on(",").join(result));
            }
            return result;
        }
        return Collections.emptyList();
    }

    public static String singleLine(String tweet) {
        return StringUtils.isNotEmpty(tweet) ? tweet.replace("\n", SPACE).replace("\r", SPACE) : StringUtils.EMPTY;
    }

}
