package com.pject.helpers;

import com.pject.bot.BotSetup;
import com.pject.exceptions.BotInitPropertiesException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Properties;

/**
 * BotPropertiesHelper - Short description of the class
 *
 * @author Camille
 *         Last: 19/09/15 20:14
 * @version $Id$
 */
public class BotPropertiesHelper implements BotSetup {

    private static final Logger LOGGER = Logger.getLogger(BotPropertiesHelper.class);

    // Defining mandatory properties
    private static final String BOT_UNIQUE_ID_PROP              = "uniqueId";
    private static final String BOT_CONSUMER_KEY_PROP           = "consumerKey";
    private static final String BOT_CONSUMER_KEY_SECRET_DROP    = "consumerKeySecret";
    private static final String BOT_ACCESS_TOKEN_PROP           = "accessToken";
    private static final String BOT_ACCESS_TOKEN_SECRET_PROP    = "accessTokenSecret";
    private static final String BOT_DROPBOX_TOKEN_PROP          = "dropboxToken";

    // Defining optional properties
    private static final String OPT_LOG_ERRORS                  = "logErrors";
    private static final String OPT_DRY_RUN                     = "dryRun";
    private static final String OPT_LOG_STATS                   = "logStats";

    private static Properties botProperties = new Properties();

    public static void init(String[] args) throws BotInitPropertiesException {
        // Attempting to load bot properties
        File propertiesFile = new File(System.getProperty(PROPERTIES_FILE_PROP_NAME, DEFAULT_PROPERTIES_FILE));
        if(propertiesFile.exists() && propertiesFile.length() > 0) {
            if(LOGGER.isDebugEnabled()) {
                LOGGER.debug("Starting up from properties file " + propertiesFile.getAbsolutePath());
            }
            try {
                botProperties.load(new FileInputStream(propertiesFile));
            } catch(Exception e) {
                LOGGER.error("Could not startup from properties file " + propertiesFile.getAbsolutePath(), e);
            }
        } else {
            if(LOGGER.isDebugEnabled()) {
                LOGGER.debug("Starting up from bot args");
            }
            if(args.length < 6) {
                throw new BotInitPropertiesException("Not enough arguments for starting up bot");
            }
            botProperties.put(BOT_UNIQUE_ID_PROP, args[0]);
            botProperties.put(BOT_CONSUMER_KEY_PROP, args[1]);
            botProperties.put(BOT_CONSUMER_KEY_SECRET_DROP, args[2]);
            botProperties.put(BOT_ACCESS_TOKEN_PROP, args[3]);
            botProperties.put(BOT_ACCESS_TOKEN_SECRET_PROP, args[4]);
            botProperties.put(BOT_DROPBOX_TOKEN_PROP, args[5]);
            if(args.length > 6) {
                botProperties.put(OPT_LOG_ERRORS, args[6]);
            }
        }

        // Logging bot properties
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("Starting bot with properties:");
            for(Map.Entry entry : botProperties.entrySet()) {
                LOGGER.debug(entry.getKey() + "=" + entry.getValue());
            }
        }

        // Analyzing bot properties validity
        for(Field field : BotPropertiesHelper.class.getDeclaredFields()) {
            if(field.getName().startsWith("BOT_")) {
                try {
                    if(StringUtils.isEmpty(botProperties.getProperty(field.get(null).toString()))) {
                        throw new BotInitPropertiesException("Could not determine value for " + field.getName());
                    }
                } catch(Exception e) {
                    throw new BotInitPropertiesException("Could not determine value for " + field.getName());
                }
            }
        }
    }

    public static String getBotUniqueId() {
        return botProperties.getProperty(BOT_UNIQUE_ID_PROP);
    }

    public static String getConsumerKey() {
        return botProperties.getProperty(BOT_CONSUMER_KEY_PROP);
    }

    public static String getConsumerSecret() {
        return botProperties.getProperty(BOT_CONSUMER_KEY_SECRET_DROP);
    }

    public static String getAccessToken() {
        return botProperties.getProperty(BOT_ACCESS_TOKEN_PROP);
    }

    public static String getAccessTokenSecret() {
        return botProperties.getProperty(BOT_ACCESS_TOKEN_SECRET_PROP);
    }

    public static String getDropBoxToken() {
        return botProperties.getProperty(BOT_DROPBOX_TOKEN_PROP);
    }

    public static boolean getExtraErrorLogging() {
        return Boolean.valueOf(botProperties.getProperty(OPT_LOG_ERRORS, String.valueOf(Boolean.FALSE)));
    }

    public static boolean getDryRun() {
        return Boolean.valueOf(botProperties.getProperty(OPT_DRY_RUN, String.valueOf(Boolean.FALSE)));
    }

    public static boolean getLogStats() {
        return Boolean.valueOf(botProperties.getProperty(OPT_LOG_STATS, String.valueOf(Boolean.FALSE)));
    }

}
