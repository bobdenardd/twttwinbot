package com.pject.scrapping;

import com.google.common.collect.Lists;
import com.pject.bot.Bot;
import com.pject.exceptions.BotInitPropertiesException;
import com.pject.exceptions.BotInitTwitterException;
import com.pject.helpers.BotPropertiesHelper;
import com.pject.helpers.LogFormatHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;

import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.List;

/**
 * TeenTwitterScapper - Short description of the class
 *
 * @author Camille
 *         Last: 30/09/2015 10:18
 * @version $Id$
 */
public class TeenTwitterScapper {

    private static final Logger LOGGER = Logger.getLogger(TeenTwitterScapper.class);

    private static final String FILE = "conversations.txt";

    private static final List<String> USERS = Lists.newArrayList("Natek_world", "agenlgar", "SUP3RKONAR", "HugoPOSAY", "clarahautman",
            "T0ki_T0ki", "PopiGames", "Sizzrawww", "HikkariMC", "MarionPossety", "Warzpir", "Hoooran_Smile", "ChelxieLive", "Erya62",
            "laclarabelle", "MahoganyLOX", "Yoaxnn", "NouilleNoire", "Laura_Japan_", "azorartFR");

    public static void main(String[] args) throws Exception {
        LOGGER.info("Starting the twitter teenage scrapper");
        Bot bot = null;
        try {
            BotPropertiesHelper.init(args);
            bot = new Bot(BotPropertiesHelper.getConsumerKey(),
                    BotPropertiesHelper.getConsumerSecret(),
                    BotPropertiesHelper.getAccessToken(),
                    BotPropertiesHelper.getAccessTokenSecret(),
                    BotPropertiesHelper.getDropBoxToken());
        } catch (BotInitPropertiesException |BotInitTwitterException e) {
            LOGGER.error("Could not initialize twitter layer: " + LogFormatHelper.formatExceptionMessage(e));
            System.exit(1);
        }

        // Extracting the twitter layer for the searches
        Field twitterField = bot.getClass().getDeclaredField("twitter");
        twitterField.setAccessible(true);
        Twitter twitter = (Twitter) twitterField.get(bot);

        List<String> results = Lists.newArrayList();

        PrintWriter writer = new PrintWriter(FILE);
        for(String user : USERS) {
            for(int i = 1; i < 5; i++) {
                Paging paging = new Paging(i, 200);
                List<Status> statuses = twitter.getUserTimeline(user, paging);
                for (Status status : statuses) {
                    String result = processStatus(status);
                    if (StringUtils.isNotEmpty(result)) {
                        LOGGER.info(result);
                        if(!results.contains(result)) {
                            results.add(result);
                            writer.println(result);
                            writer.flush();
                        }
                    }
                }
            }
        }
        writer.close();
    }

    private static String processStatus(Status status) {
        if(StringUtils.isNotEmpty(status.getText())) {
            String test = LogFormatHelper.oneLine(status.getText());
            if(test.matches(".*(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|].*") ||
                    test.matches(".*#.*") ||
                    test.matches(".*@.*") ||
                    test.matches("^RT .*") ||
                    test.matches(".* RT .*") ||
                    test.length() < 15) {
                LOGGER.warn("Found non single tweet: " + LogFormatHelper.oneLine(status.getText()));
            } else {
                LOGGER.info("Found single tweet: " + LogFormatHelper.oneLine(status.getText()));
                return LogFormatHelper.oneLine(status.getText());
            }
        }
        return null;
    }

}
