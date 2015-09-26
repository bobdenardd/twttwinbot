package com.pject.helpers;

import com.pject.bot.BotSetup;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.PrintWriter;
import java.util.Date;

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

    public static void addRetweetCount() {
        numberOfRetweets++;
    }

    public static void addFollowerCount() {
        numberOfFollows++;
    }

    public static void addUnfollowCount() {
        numberOfUnfollows++;
    }

    public static void dumpStats() {
        try {
            File statsFile = new File("errors-" + DATE_FORMAT.format(new Date()) + ".txt");
            statsFile.deleteOnExit();
            PrintWriter writer = new PrintWriter(statsFile);
            writer.println("Number of retweets:  " + numberOfRetweets);
            writer.println("Number of follows:   " + numberOfFollows);
            writer.println("Number of unfollows: " + numberOfUnfollows);
            writer.close();
            DropBoxHelper.uploadFile(DropBoxHelper.getRemoteFile(REMOTE_ROOT_DBOX, BotPropertiesHelper.getBotUniqueId(), statsFile.getName()), statsFile);
        } catch(Exception e) {
            LOGGER.warn("Could not dump errors file", e);
        }
    }

}
