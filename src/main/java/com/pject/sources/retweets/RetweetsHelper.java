package com.pject.sources.retweets;

import com.google.common.collect.Lists;
import com.pject.twitter.TweetAnalyzer;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Trends;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import java.util.Collections;
import java.util.List;

/**
 * RetweetsHelper - Short description of the class
 *
 * @author Camille
 *         Last: 21/12/2015 14:34
 * @version $Id$
 */
public class RetweetsHelper {

    private static final Logger LOGGER = Logger.getLogger(RetweetsHelper.class);

    private static List<Status> toBeRetweeted = Lists.newArrayList();

    public static void init(Twitter twitter) {
        // Picking up trends
        List<String> frenchTrends = Lists.newArrayList();
        try {
            Trends trends = twitter.getPlaceTrends(23424819); // for french trends
            for (int i = 0; i < trends.getTrends().length; i++) {
                if (StringUtils.isNotEmpty(trends.getTrends()[i].getName()) && trends.getTrends()[i].getName().startsWith("#")) {
                    frenchTrends.add(trends.getTrends()[i].getName());
                }
            }

            // Picking up corresponding tweets
            Collections.shuffle(frenchTrends);
            for (int i = 0; i < Math.min(frenchTrends.size(), 20); i++) {
                LOGGER.info("Retrieving potential retweets for trend " + frenchTrends.get(i));
                Query query = new Query(frenchTrends.get(i));
                query.setCount(50);
                QueryResult result = twitter.search(query);
                for (Status status : result.getTweets()) {
                    String normalizedText = TweetAnalyzer.singleLine(status.getText());
                    if (!normalizedText.matches(".*@.*") &&
                            !normalizedText.matches("^RT .*") &&
                            !normalizedText.matches(".* RT .*") &&
                            normalizedText.length() >= 100) {
                        toBeRetweeted.add(status);
                    }
                }

            }
        } catch (TwitterException e) {
            LOGGER.warn("Could not initialize the retweets helper: " + e.getMessage());
        }
    }

    public static Status getStatusToRetweet() {
        // Get random tweet from the list
        Collections.shuffle(toBeRetweeted);
        if (toBeRetweeted.size() > 0) {
            Status status = toBeRetweeted.get(0);
            toBeRetweeted.remove(0);
            return status;
        }
        return null;
    }

}
