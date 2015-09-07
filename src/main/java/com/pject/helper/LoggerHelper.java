package com.pject.helper;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * LoggerHelper - Short description of the class
 *
 * @author Camille
 *         Last: 06/09/15 16:08
 * @version $Id$
 */
public class LoggerHelper {

    private static final int MAX_LENGTH_DEBUG = 160;
    private static final int MAX_LENGHT_ERROR = 160;
    private static final String SPACE = " ";

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
        logger.error(message + (e != null ? ": " + StringUtils.abbreviate(StringUtils.abbreviate(e.getMessage(), 45), MAX_LENGHT_ERROR) : StringUtils.EMPTY));
    }

}
