package com.pject.helper;

import com.google.common.collect.Lists;
import com.pject.BotSetup;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;

/**
 * LoggerHelper - Short description of the class
 *
 * @author Camille
 *         Last: 06/09/15 16:08
 * @version $Id$
 */
public class LoggerHelper implements BotSetup {

    private static final Logger LOGGER = Logger.getLogger(LoggerHelper.class);

    private static final int MAX_LENGTH_DEBUG = 160;
    private static final int MAX_LENGHT_ERROR = 250;
    private static final String SPACE = " ";

    private static List<String> errors = Lists.newArrayList();

    public static void info(Logger logger, String message) {
        logger.info(message);
    }

    public static void debug(Logger logger, String message) {
        if(logger.isDebugEnabled()) {
            message = message.replace("\n", SPACE).replace("\r", SPACE);
            logger.debug(StringUtils.abbreviate(message, MAX_LENGTH_DEBUG));
        }
    }

    public static void error(Logger logger, String message, Exception e) {
        message = message.replace("\n", SPACE).replace("\r", SPACE);
        if(BotPropertiesHelper.getExtraErrorLogging()) {
            errors.add(message + ":: " + e.toString().replace("\n", SPACE));
        }
        logger.error(message + (e != null ? ": " + StringUtils.abbreviate(StringUtils.abbreviate(e.getMessage(), MAX_LENGHT_ERROR), MAX_LENGHT_ERROR) : StringUtils.EMPTY));
    }

    public static void dumpErrors() {
        try {
            File errors = new File("errors-" + DATE_FORMAT.format(new Date()) + ".txt");
            errors.deleteOnExit();
            PrintWriter writer = new PrintWriter(errors);
            for(String error : LoggerHelper.errors) {
                writer.println(error);
            }
            writer.close();
            DropBoxHelper.uploadFile(DropBoxHelper.getRemoteFile(REMOTE_ROOT_DBOX, BotPropertiesHelper.getBotUniqueId(), errors.getName()), errors);
        } catch(Exception e) {
            LOGGER.warn("Could not dump errors file", e);
        }
    }

}
