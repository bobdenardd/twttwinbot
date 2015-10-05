package com.pject.helpers;

import com.google.common.collect.Maps;
import com.pject.bot.BotSetup;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;

/**
 * StatsHelper - Short description of the class
 *
 * @author Camille
 *         Last: 26/09/2015 14:20
 * @version $Id$
 */
public class StatsHelper implements BotSetup {

    private static final Logger LOGGER = Logger.getLogger(StatsHelper.class);

    private static int numberOfRetweets     = 0;
    private static int numberOfFollows      = 0;
    private static int numberOfUnfollows    = 0;
    private static int numberOfRealTweets   = 0;
    private static int numberOfFollowers    = 0;
    private static int numberOfFollowing    = 0;
    private static float currentRatio       = 0;

    private static Map<String, Long> sourcesLoadTime    = Maps.newHashMap();
    private static Map<String, Integer> sourcesNumbers  = Maps.newHashMap();

    public static void addRetweetCount() {
        numberOfRetweets++;
    }

    public static void addFollowerCount() {
        numberOfFollows++;
    }

    public static void addUnfollowCount() {
        numberOfUnfollows++;
    }

    public static void addRealTweetCount() {
        numberOfRealTweets++;
    }

    public static void addUnfollowInfo(float currentRatio, int followers, int following) {
        StatsHelper.currentRatio = currentRatio;
        numberOfFollowers = followers;
        numberOfFollowing = following;
    }

    public static void registerSource(String name, long loadTime, int numberOfSources) {
        sourcesLoadTime.put(name, loadTime);
        sourcesNumbers.put(name, numberOfSources);
    }

    public static void dumpStats() {
        if(! BotPropertiesHelper.getReadOnly()) {
            File statsFile = new File("stats-" + DATE_FORMAT.format(new Date()) + ".txt");
            statsFile.deleteOnExit();
            try (PrintWriter writer = new PrintWriter(statsFile)){
                writer.println("Registered sources: " + sourcesLoadTime.size());
                long totalLoadTime = 0;
                int totalNumer = 0;
                for(Map.Entry<String, Integer> entry : sourcesNumbers.entrySet()) {
                    writer.print("Source " + StringUtils.rightPad(entry.getKey(), 12));
                    writer.print("loaded in " + StringUtils.leftPad(sourcesLoadTime.get(entry.getKey()) + StringUtils.EMPTY, 7) + "ms");
                    writer.println(StringUtils.leftPad(entry.getValue() + StringUtils.EMPTY, 5) + " sources");
                    totalLoadTime+=sourcesLoadTime.get(entry.getKey());
                    totalNumer+=entry.getValue();
                }
                writer.println("Total load time: " + totalLoadTime + "ms Total number: " + totalNumer + "\n");
                writer.println("Number of retweets:    " + numberOfRetweets);
                writer.println("Number of follows:     " + numberOfFollows);
                writer.println("Number of unfollows:   " + numberOfUnfollows);
                writer.println("Number of real tweets: " + numberOfRealTweets + "\n");
                writer.println("Followers: " + numberOfFollowers + " Following: " + numberOfFollowing + " ratio: " + currentRatio);
                writer.flush();
                DropBoxHelper.uploadFile(DropBoxHelper.getRemoteFile(REMOTE_ROOT_DBOX, BotPropertiesHelper.getBotUniqueId(), statsFile.getName()), statsFile);
            } catch (Exception e) {
                LOGGER.warn("Could not dump errors file", e);
            }
        }
    }

}
