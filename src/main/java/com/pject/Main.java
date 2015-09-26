package com.pject;

import com.pject.bot.Bot;
import com.pject.exceptions.BotInitPropertiesException;
import com.pject.exceptions.BotInitTwitterException;
import com.pject.helpers.BotPropertiesHelper;
import com.pject.helpers.LogFormatHelper;
import com.pject.helpers.SchedulingHelper;
import org.apache.log4j.Logger;

/**
 * Main - Short description of the class
 *
 * @author Camille
 *         Last: 02/09/15 15:11
 * @version $Id$
 */
public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class);

    public static void main(String[] args) {
        LOGGER.info("Starting the twitter winning bot");

        // Initializing and checking schedule
        if (!SchedulingHelper.isValidRun()) {
            LOGGER.error("Bot startup is outside schedule");
            System.exit(1);
        }

        // Initializing bot properties and creating bot instance
        Bot bot = null;
        try {
            BotPropertiesHelper.init(args);
            bot = new Bot(BotPropertiesHelper.getConsumerKey(),
                    BotPropertiesHelper.getConsumerSecret(),
                    BotPropertiesHelper.getAccessToken(),
                    BotPropertiesHelper.getAccessTokenSecret(),
                    BotPropertiesHelper.getDropBoxToken());
        } catch (BotInitPropertiesException|BotInitTwitterException e) {
            LOGGER.error("Could not initialize bot: " + LogFormatHelper.formatExceptionMessage(e));
            System.exit(1);
        }

        // Checking if the bot is in dry run mode
        if(BotPropertiesHelper.getDryRun()) {
            LOGGER.info("Dry run mode, aborting");
            System.exit(0);
        }

        // Executing bot run
        bot.executeRun();
    }

}
