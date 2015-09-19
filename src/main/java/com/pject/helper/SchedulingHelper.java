package com.pject.helper;

import com.pject.BotSetup;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.util.Calendar;
import java.util.Properties;

/**
 * SchedulingHelper - Short description of the class
 *
 * @author Camille
 *         Last: 19/09/15 20:43
 * @version $Id$
 */
public class SchedulingHelper implements BotSetup {

    private static final Logger LOGGER = Logger.getLogger(SchedulingHelper.class);

    private static final String PROP_SCHEDULE_ACTIVE            = "active";
    private static final String PROP_START_HOUR                 = "start";
    private static final String PROP_END_HOUR                   = "end";
    private static final String PROP_PERIOD                     = "period";

    private static final boolean DEFAULT_ACTIVE                 = false;
    private static final int DEFAULT_START_HOUR                 = 8;
    private static final int DEFAULT_END_HOUR                   = 22;
    private static final int DEFAULT_PERIOD                     = 2;

    private static Properties schedulingProperties = null;

    public static boolean isValidRun() {
        // Initializing the schedule
        init();

        // If there is no scheduling properties, consider it a valid run anytime
        if(schedulingProperties == null) {
            return true;
        }
        boolean active = Boolean.parseBoolean(schedulingProperties.getProperty(PROP_SCHEDULE_ACTIVE, String.valueOf(DEFAULT_ACTIVE)));
        if(active) {
            int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            if(currentHour < getSafeHourProperty(PROP_START_HOUR, DEFAULT_START_HOUR) ||
                    currentHour > getSafeHourProperty(PROP_END_HOUR, DEFAULT_END_HOUR) ||
                    currentHour % getSafeHourProperty(PROP_PERIOD, DEFAULT_PERIOD) != 0) {
                return false;
            }
        }
        return true;
    }

    private static int getSafeHourProperty(String hourProperty, int defaultValue) {
        try {
            return Integer.parseInt(schedulingProperties.getProperty(hourProperty, String.valueOf(defaultValue)));
        } catch(Exception e) {
            LOGGER.warn("Cannot parse scheduling value " + hourProperty + "=" + schedulingProperties.getProperty(hourProperty));
            return defaultValue;
        }
    }

    private static boolean init() {
        // Loading optional schedule
        File propertiesFile = new File(System.getProperty(SCHEDULING_FILE_PROP_NAME, DEFAULT_SCHEDULING_FILE));
        if(propertiesFile.exists() && propertiesFile.length() > 0) {
            try {
                schedulingProperties = new Properties();
                schedulingProperties.load(new FileInputStream(propertiesFile));
            } catch(Exception e) {
                LOGGER.warn("Could not load scheduling properties file", e);
            }
        }
        return false;
    }

}
