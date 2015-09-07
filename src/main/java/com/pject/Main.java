package com.pject;

import com.google.common.collect.Lists;
import com.pject.helper.LoggerHelper;
import com.pject.helper.TweetAnalyzer;
import com.pject.helper.TwitterProxy;
import com.pject.persistence.Persistence;
import com.pject.persistence.Tweets;
import com.pject.persistence.Users;
import org.apache.log4j.Logger;
import twitter4j.IDs;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

import java.util.List;

/**
 * Main - Short description of the class
 *
 * @author Camille
 *         Last: 02/09/15 15:11
 * @version $Id$
 */
public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class);
    private static final String SEARCH_QUERY = "concours OR gagner";

    private static Twitter twitter;
    private static Users users;
    private static Tweets tweets;

    public static void main(String[] args) {
        // Initializing
        LoggerHelper.info(LOGGER, "Starting the twitter winning bot");
        if(args.length != 4) {
            LoggerHelper.error(LOGGER, "Args: consumerKey consumerSecret accessToken accesTokenSecret", null);
        }
        String consumerKey = args[0];
        String consumerSecret = args[1];
        String accessToken = args[2];
        String accessTokenSecret = args[3];
        try {
            init(consumerKey, consumerSecret, accessToken, accessTokenSecret);
        } catch (Exception e) {
            LoggerHelper.error(LOGGER, "Could not init the twitter proxy", e);
        }

        // Searching for tweets
        List<Status> result = searchForTweets();

        // Handling new tweets
        for (Status status : result) {
            processTweet(status);
        }

        // Closing the persistence
        LoggerHelper.info(LOGGER, "Persisting tweets and info");
        Persistence.storeTweets(tweets);
        Persistence.storeUsers(users);
    }

    private static void init(String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret) throws TwitterException {
        // Initializing twitter
        LoggerHelper.info(LOGGER, "Initializing twitter account");
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(consumerKey)
                .setOAuthConsumerSecret(consumerSecret)
                .setOAuthAccessToken(accessToken)
                .setOAuthAccessTokenSecret(accessTokenSecret);
        TwitterFactory tf = new TwitterFactory(cb.build());
        twitter = tf.getInstance();

        // Initializing persistence
        LoggerHelper.info(LOGGER, "Initializing persistence");
        tweets = Persistence.loadTweets();
        users = Persistence.loadUsers();

        // Initializing twitter proxy
        LoggerHelper.info(LOGGER, "Initializing twitter proxy");
        TwitterProxy.init(twitter);
    }

    private static List<Status> searchForTweets() {
        LoggerHelper.info(LOGGER, "Searching for tweets using query: " + SEARCH_QUERY);
        List<Status> result = Lists.newArrayList();
        Query query = new Query(SEARCH_QUERY);
        query.setCount(100);

        try {
            QueryResult results = TwitterProxy.search(twitter, query);
            LoggerHelper.info(LOGGER, "Found " + results.getCount() + " tweets matching query");
            for (Status status : results.getTweets()) {
                if (!tweets.isTweetAlreadyProcessed(status)) {
                    tweets.markAsSeen(status);
                    result.add(getRootTweet(status));
                }
            }
        } catch (Exception e) {
            LoggerHelper.error(LOGGER, "Could not search for tweets", e);
        }
        LoggerHelper.info(LOGGER, "Found " + result.size() + " new tweets");
        return result;
    }

    private static void processTweet(Status status) {
        // Determining if the tweet is a retweet or original content
        LoggerHelper.debug(LOGGER, "-=-=- Processing tweet: " + status.getText());
        Status toConsider = !status.isRetweet() ? status : status.getRetweetedStatus();
        LoggerHelper.debug(LOGGER, "After detweet " + toConsider.getText());
        // Checking for retweet need
        try {
            if (TweetAnalyzer.needsRetweet(toConsider.getText())) {
                LoggerHelper.debug(LOGGER, " -> Needs retweeting");
                TwitterProxy.retweet(twitter, toConsider);
            }
        } catch (Exception e) {
            LoggerHelper.error(LOGGER, "Could not retweet tweet " + status.getId() + ":" + status.getText(), e);
        }
            // Checking for follow need
        try {
            if (TweetAnalyzer.needsFollow(toConsider.getText())) {
                LoggerHelper.debug(LOGGER, " -> Needs follow");
                List<String> usersToFollow = TweetAnalyzer.extractUsersToFollow(toConsider.getText());
                if (!usersToFollow.contains(toConsider.getUser().getScreenName().toLowerCase())) {
                    usersToFollow.add(toConsider.getUser().getScreenName().toLowerCase());
                }
                for (String userToFollow : usersToFollow) {
                    LoggerHelper.debug(LOGGER, "Preparing to follow user " + userToFollow);
                    if (!users.isAlreadyFollowed(userToFollow)) {
                        TwitterProxy.follow(twitter, userToFollow, true);
                        users.addFollowed(userToFollow);
                    } else {
                        LoggerHelper.debug(LOGGER, "User " + usersToFollow + " already followed");
                    }
                }
            }
            // Marking as processed
            tweets.markAsSeen(status);
            tweets.markAsSeen(toConsider);
        } catch(Exception e) {
            LoggerHelper.error(LOGGER, "Could not follow", e);
        }
    }

    private static Status getRootTweet(Status status) {
        int index = 0;
        while (status.isRetweet() && index < 5) {
            status = status.getRetweetedStatus();
            index++;
        }
        return status;
    }

    // yet to come
    private static void unfollowRoutine() {
        try {
            User u1 = null;
            long cursor = -1;
            IDs ids;
            do {
                ids = twitter.getFriendsIDs(cursor);

                for (long id : ids.getIDs()) {
                    System.out.println(id);
                    User user = twitter.showUser(id);
                    System.out.println(id + ":" + user.getName());
                }
            } while ((cursor = ids.getNextCursor()) != 0);
        } catch (Exception e) {
            System.err.println("Could not trigger unfollow routine " + e.getMessage());
        }
    }

}
