package com.pject;

import com.google.common.collect.Lists;
import com.google.common.primitives.Longs;
import com.pject.exceptions.StartupException;
import com.pject.helper.BotPropertiesHelper;
import com.pject.helper.DropBoxHelper;
import com.pject.helper.LoggerHelper;
import com.pject.helper.SchedulingHelper;
import com.pject.twitter.TweetAnalyzer;
import com.pject.twitter.TwitterProxy;
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

    private static int retweetNumber = 0;
    private static int followNumber = 0;

    public static void main(String[] args) {
        LoggerHelper.info(LOGGER, "Starting the twitter winning bot");

        // Checking schedule
        if (!SchedulingHelper.isValidRun()) {
            LoggerHelper.error(LOGGER, "Bot startup is outside schedule", null);
            System.exit(1);
        }

        // Initializing bot properties
        try {
            BotPropertiesHelper.init(args);
        } catch (StartupException e) {
            LoggerHelper.error(LOGGER, "Could not start bot", e);
            System.exit(1);
        }

        // Initializing twitter api client
        try {
            init(BotPropertiesHelper.getConsumerKey(),
                    BotPropertiesHelper.getConsumerSecret(),
                    BotPropertiesHelper.getAccessToken(),
                    BotPropertiesHelper.getAccessTokenSecret(),
                    BotPropertiesHelper.getDropBoxToken());
        } catch (TwitterException e) {
            LoggerHelper.error(LOGGER, "Could not init the twitter proxy", e);
        }

        // Checking if the bot is in dry run mode
        if(BotPropertiesHelper.getDryRun()) {
            LoggerHelper.info(LOGGER, "Dry run mode, aborting");
            System.exit(0);
        }

        // Persisting and optional error dumping
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                LoggerHelper.info(LOGGER, "Persisting tweets and info");
                Persistence.storeTweets(tweets);
                Persistence.storeUsers(users);
                if(BotPropertiesHelper.getExtraErrorLogging()) {
                    LoggerHelper.dumpErrors();
                }
            }
        }));

        // Searching for tweets
        List<Status> result = searchForTweets();

        // Handling new tweets
        for (Status status : result) {
            processTweet(status);
        }

        // Quick stats
        LoggerHelper.info(LOGGER, "Number of retweets: " + retweetNumber);
        LoggerHelper.info(LOGGER, "Number of follows: " + followNumber);
    }

    private static void init(String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret, String dropBoxToken) throws TwitterException {
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

        // Initializing dropbox
        DropBoxHelper.init(dropBoxToken);

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
                retweetNumber++;
            }
        } catch (Exception e) {
            LoggerHelper.error(LOGGER, "Could not retweet tweet " + toConsider.getText(), e);
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
                        followNumber++;
                        TwitterProxy.follow(twitter, userToFollow, true);
                        users.addFollowed(userToFollow);
                    } else {
                        LoggerHelper.debug(LOGGER, "User " + userToFollow + " already followed");
                    }
                }
            }
            // Marking as processed
            tweets.markAsSeen(status);
            tweets.markAsSeen(toConsider);
        } catch (Exception e) {
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

    // yet to come, too busy taking dumps n shit
    private static void unfollowRoutine() {
        try {
            List<Long> userIds = Lists.newArrayList();
            long cursor = -1;
            IDs ids;
            do {
                ids = twitter.getFriendsIDs(cursor);
                for (long id : ids.getIDs()) {
                    userIds.add(id);
                }
            } while ((cursor = ids.getNextCursor()) != 0);
            int size = userIds.size();
            int index = 0;
            for (List<Long> miniList : Lists.partition(userIds, 5)) {
                for (User user : twitter.lookupUsers(Longs.toArray(miniList))) {
                    System.out.println(user.getName());
                }
            }
        } catch (Exception e) {
            System.err.println("Could not trigger unfollow routine " + e.getMessage());
        }
    }

}
