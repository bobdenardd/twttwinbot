package com.pject.helpers;

import org.apache.commons.lang3.StringUtils;

/**
 * LogFormatHelper - Short description of the class
 *
 * @author Camille
 *         Last: 26/09/2015 14:12
 * @version $Id$
 */
public class LogFormatHelper {

    private static final String SPACE = " ";

    public static String formatExceptionMessage(Exception e) {
        if(e != null && StringUtils.isNotEmpty(e.getMessage())) {
            String message = oneLine(e.getMessage());
            return StringUtils.abbreviate(message, 500);
        }
        return StringUtils.EMPTY;
    }

    public static String getShortTweet(String tweetMessage) {
        return StringUtils.abbreviate(oneLine(tweetMessage), 140);
    }

    public static String oneLine(String message) {
        return StringUtils.isNotEmpty(message) ? message.replace("\n", SPACE).replace("\r", SPACE) : StringUtils.EMPTY;
    }

}
