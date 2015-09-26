package com.pject.helpers;

import com.google.common.collect.Lists;
import com.pject.bot.BotSetup;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;

/**
 * ErrorHelper - Short description of the class
 *
 * @author Camille
 *         Last: 26/09/2015 14:20
 * @version $Id$
 */
public class ErrorHelper implements BotSetup {

    private static final Logger LOGGER = Logger.getLogger(ErrorHelper.class);

    private static List<String> errors = Lists.newArrayList();

    public static void addError(String errorMessage) {
        errors.add(errorMessage);
    }

    public static void dumpErrors() {
        try {
            File errorsFile = new File("errors-" + DATE_FORMAT.format(new Date()) + ".txt");
            errorsFile.deleteOnExit();
            PrintWriter writer = new PrintWriter(errorsFile);
            for(String error : errors) {
                writer.println(error);
            }
            writer.close();
            DropBoxHelper.uploadFile(DropBoxHelper.getRemoteFile(REMOTE_ROOT_DBOX, BotPropertiesHelper.getBotUniqueId(), errorsFile.getName()), errorsFile);
        } catch(Exception e) {
            LOGGER.warn("Could not dump errors file", e);
        }
    }

}
