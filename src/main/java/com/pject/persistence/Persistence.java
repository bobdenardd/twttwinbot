package com.pject.persistence;

import com.pject.helper.DropBoxHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

/**
 * Persistence - Short description of the class
 *
 * @author Camille
 *         Last: 04/09/15 14:30
 * @version $Id$
 */
public class Persistence {

    private static final Logger LOGGER = Logger.getLogger(Persistence.class);

    private static final String REMOTE_ROOT_DBOX = "/twttwinbot";
    private static final String TWEETS_FILE = "tweets.dat";
    private static final String USERS_FILE = "users.dat";

    public static Tweets loadTweets() {
        Properties properties = new Properties();
        File tweetsFile = new File(TWEETS_FILE);
        tweetsFile.deleteOnExit();
        try {
            DropBoxHelper.downloadFile(DropBoxHelper.getRemoteFile(REMOTE_ROOT_DBOX, TWEETS_FILE), tweetsFile);
            properties.load(new FileInputStream(tweetsFile));
            LOGGER.info("Loaded " + properties.size() + " tweets");
        } catch(Exception e) {
            LOGGER.error("Could not load tweets", e);
        }
        return new Tweets(properties);
    }

    public static Users loadUsers() {
        Properties properties = new Properties();
        File usersFile = new File(USERS_FILE);
        usersFile.deleteOnExit();
        try {
            DropBoxHelper.downloadFile(DropBoxHelper.getRemoteFile(REMOTE_ROOT_DBOX, USERS_FILE), usersFile);
            properties.load(new FileInputStream(usersFile));
            LOGGER.info("Loaded " + properties.size() + " followed users");
        } catch(Exception e) {
            LOGGER.error("Could not load users", e);
        }
        return new Users(properties);
    }

    public static void storeTweets(Tweets tweets) {
        store(tweets.getProperties(), TWEETS_FILE);
    }

    public static void storeUsers(Users users) {
        store(users.getProperties(), USERS_FILE);
    }

    public static void store(Properties properties, String file) {
        File fileStorage = new File(file);
        try {
            properties.store(new FileOutputStream(fileStorage), StringUtils.EMPTY);
            DropBoxHelper.uploadFile(DropBoxHelper.getRemoteFile(REMOTE_ROOT_DBOX, file), fileStorage);
        } catch(Exception e) {
            LOGGER.error("Could not sotre " + file, e);
        }
    }

}
