package com.pject.helpers;

import com.google.common.collect.Maps;
import com.pject.bot.BotSetup;
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

    public static void registerSource(String name, long loadTime, int numberOfSources) {
        sourcesLoadTime.put(name, loadTime);
        sourcesNumbers.put(name, numberOfSources);
    }

    public static void dumpStats() {
        if(! BotPropertiesHelper.getReadOnly()) {
            File statsFile = new File("stats-" + DATE_FORMAT.format(new Date()) + ".txt");
            //statsFile.deleteOnExit();
            try (PrintWriter writer = new PrintWriter(statsFile)){
                writer.println("Registered sources: " + sourcesLoadTime.size());
                for(Map.Entry<String, Integer> entry : sourcesNumbers.entrySet()) {
                    writer.println("Source " + entry.getKey() + " loaded in " + sourcesLoadTime.get(entry.getKey()) + "ms, number: " + entry.getValue());

                }
                writer.println("Number of retweets:    " + numberOfRetweets);
                writer.println("Number of follows:     " + numberOfFollows);
                writer.println("Number of unfollows:   " + numberOfUnfollows);
                writer.println("Number of real tweets: " + numberOfRealTweets);
                writer.flush();
                DropBoxHelper.uploadFile(DropBoxHelper.getRemoteFile(REMOTE_ROOT_DBOX, BotPropertiesHelper.getBotUniqueId(), statsFile.getName()), statsFile);
            } catch (Exception e) {
                LOGGER.warn("Could not dump errors file", e);
            }
        }
    }

}
