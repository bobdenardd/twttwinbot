package com.pject.bot;

import com.google.common.collect.Lists;
import com.pject.exceptions.BotInitTwitterException;
import com.pject.helpers.BotPropertiesHelper;
import com.pject.helpers.DropBoxHelper;
import com.pject.helpers.ErrorHelper;
import com.pject.helpers.LogFormatHelper;import com.pject.helpers.StatsHelper;
import com.pject.persistence.Persistence;
import com.pject.persistence.Tweets;
import com.pject.persistence.Users;
import com.pject.twitter.TweetAnalyzer;
import com.pject.twitter.TwitterProxy;
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
 * Bot - Short description of the class
 *
 * @author Camille
 *         Last: 26/09/2015 14:01
 * @version $Id$
 */
public class Bot implements BotSetup {

    private static final Logger LOGGER = Logger.getLogger(Bot.class);

    private String consumerKey;
    private String consumerSecret;
    private String accessToken;
    private String accessTokenSecret;
    private String dropBoxToken;

    private Twitter twitter;
    private Tweets tweets;
    private Users users;

    public Bot(String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret, String dropBoxToken) throws BotInitTwitterException {
        // Setting up bot essentials
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        this.accessToken = accessToken;
        this.accessTokenSecret = accessTokenSecret;
        this.dropBoxToken = dropBoxToken;

        // Initializing bot twitter client
        try {
            initTwitter();
        } catch(TwitterException e) {
            throw new BotInitTwitterException("Could not init twitter client: " + e.getMessage());
        }

        // Initializing bot dropbox helpers
        initDropBox();

        // Initalizing persistence
        initPersistence();

        // Persisting and optional error dumping
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                terminateBot();
            }
        }));
    }

    public void executeRun() {
        // Searching for tweets
        List<Status> result = searchForTweets();

        // Handling new tweets
        for (Status status : result) {
            processTweet(status);
        }

        // Unfollow routine
        unfollowRoutine();
    }

    private List<Status> searchForTweets() {
        LOGGER.info("Searching for tweets using query: " + SEARCH_QUERY);
        List<Status> result = Lists.newArrayList();
        Query query = new Query(SEARCH_QUERY);
        query.setCount(100);

        try {
            QueryResult results = TwitterProxy.search(twitter, query);
            LOGGER.info("Found " + results.getCount() + " tweets matching query");
            for (Status status : results.getTweets()) {
                if (!tweets.isTweetAlreadyProcessed(status)) {
                    tweets.markAsSeen(status);
                    result.add(getRootTweet(status));
                }
            }
        } catch (Exception e) {
            LOGGER.error("Could not search for tweets: " + LogFormatHelper.formatExceptionMessage(e));
        }
        LOGGER.info("Found " + result.size() + " new tweets");
        return result;
    }

    private void processTweet(Status status) {
        // Determining if the tweet is a retweet or original content
        Status toConsider = !status.isRetweet() ? status : status.getRetweetedStatus();
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("Processing tweet " + LogFormatHelper.getShortTweet(status.getText()));
            LOGGER.debug("After detweet " + LogFormatHelper.getShortTweet(toConsider.getText()));
        }

        retweet(toConsider);

        follow(status, toConsider);
    }

    private void retweet(Status toConsider) {
        // Checking for retweet need
        try {
            if (TweetAnalyzer.needsRetweet(toConsider.getText())) {
                if(LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Retweet need detected");
                }
                TwitterProxy.retweet(twitter, toConsider);
                StatsHelper.addRetweetCount();
            }
        } catch (Exception e) {
            LOGGER.error("Could not retweet " + LogFormatHelper.getShortTweet(toConsider.getText()) + ": " + LogFormatHelper.formatExceptionMessage(e));
        }
    }

    private void follow(Status original, Status toConsider) {
        // Checking for follow need
        try {
            if (TweetAnalyzer.needsFollow(toConsider.getText())) {
                if(LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Follow need detected");
                }
                List<String> usersToFollow = TweetAnalyzer.extractUsersToFollow(toConsider.getText());
                if (!usersToFollow.contains(toConsider.getUser().getScreenName().toLowerCase())) {
                    usersToFollow.add(toConsider.getUser().getScreenName().toLowerCase());
                }
                for (String userToFollow : usersToFollow) {
                    if(LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Preparing to follow user " + userToFollow);
                    }
                    if (!users.isAlreadyFollowed(userToFollow)) {
                        StatsHelper.addFollowerCount();
                        TwitterProxy.follow(twitter, userToFollow, true);
                        users.addFollowed(userToFollow);
                    } else if(LOGGER.isDebugEnabled()) {
                        LOGGER.debug("User " + userToFollow + " already followed");
                    }
                }
            }
            // Marking as processed
            tweets.markAsSeen(original);
            tweets.markAsSeen(toConsider);
        } catch (Exception e) {
            LOGGER.error("Could not follow: " + LogFormatHelper.formatExceptionMessage(e));
        }
    }

    private void unfollowRoutine() {
        LOGGER.info("Unfollow routine");
        try {
            // Computing the number of people to unfollow
            User botUser = this.twitter.verifyCredentials();
            int myFollowersNumber = botUser.getFollowersCount();
            int areFollowedNumber = botUser.getFriendsCount();
            float currentRatio = (float) myFollowersNumber / (float) areFollowedNumber;
            int neededUnfollows = (int) ((FOLLOWED_TO_FOLLOWER_RATIO * (float) areFollowedNumber - (float) myFollowersNumber) / FOLLOWED_TO_FOLLOWER_RATIO);
            if(LOGGER.isDebugEnabled()) {
                LOGGER.debug("followers: " + myFollowersNumber + " followed: " + areFollowedNumber + " current ratio: " + currentRatio);
            }

            // Checking if unfollowing is needed
            if(currentRatio < FOLLOWED_TO_FOLLOWER_RATIO && neededUnfollows > MAX_UNFOLLOWS_PER_RUN) {
                List<Long> userIds = Lists.newArrayList();
                long cursor = -1;
                IDs ids;
                do {
                    ids = this.twitter.getFriendsIDs(cursor);
                    for (long id : ids.getIDs()) {
                        userIds.add(id);
                    }
                } while ((cursor = ids.getNextCursor()) != 0);
                List<Long> idsToUnfollow = Lists.reverse(userIds).subList(0, Math.min(userIds.size(), Math.min(neededUnfollows, MAX_UNFOLLOWS_PER_RUN)));

                // Unfollow users in the list
                if(LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Preparing to unfollow " + idsToUnfollow.size() + " users");
                }
                for(Long userToUnfollow : idsToUnfollow) {
                    try {
                        this.twitter.destroyFriendship(userToUnfollow);
                        StatsHelper.addUnfollowCount();
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug(this.twitter.lookupUsers(new long[]{userToUnfollow}).get(0).getScreenName());
                        }
                    } catch(TwitterException e) {
                        LOGGER.error("Could not unfollow user " + userToUnfollow + ": " + LogFormatHelper.formatExceptionMessage(e));
                    }
                }

            }
        } catch (Exception e) {
            LOGGER.error("Could not perform unfollow routine: " + LogFormatHelper.formatExceptionMessage(e));
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

    private void initTwitter() throws TwitterException {
        // Initializing twitter
        LOGGER.info("Initializing twitter client");
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(this.consumerKey)
                .setOAuthConsumerSecret(this.consumerSecret)
                .setOAuthAccessToken(this.accessToken)
                .setOAuthAccessTokenSecret(this.accessTokenSecret);
        this.twitter = new TwitterFactory(cb.build()).getInstance();

        // Initializing twitter proxy
        LOGGER.info("Initializing twitter proxy");
        TwitterProxy.init(this.twitter);
    }

    private void initDropBox() {
        DropBoxHelper.init(this.dropBoxToken);
    }

    private void initPersistence() {
        // Initializing persistence
        LOGGER.info("Initializing persistence");
        this.tweets = Persistence.loadTweets();
        this.users = Persistence.loadUsers();
    }

    private void terminateBot() {
        // Persisting discovered tweets and users
        LOGGER.info("Persisting tweets and info");
        Persistence.storeTweets(this.tweets);
        Persistence.storeUsers(this.users);

        // Dumping errors if necessary
        if(BotPropertiesHelper.getExtraErrorLogging()) {
            LOGGER.info("Dumping run errors");
            ErrorHelper.dumpErrors();
        }

        // Dumping stats if necessary
        if(BotPropertiesHelper.getLogStats()) {
            LOGGER.info("Dumping run stats");
            StatsHelper.dumpStats();
        }
    }

}
