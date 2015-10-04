package com.pject.helpers;

import com.google.common.collect.Lists;
import com.google.common.reflect.ClassPath;
import com.pject.bot.BotSetup;
import com.pject.sources.Source;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Random;

/**
 * SourcesHelper - Short description of the class
 *
 * @author Camille
 *         Last: 27/09/2015 16:33
 * @version $Id$
 */
public class SourcesHelper implements BotSetup {

    private static final Logger LOGGER = Logger.getLogger(SourcesHelper.class);

    private static List<Source> sources = Lists.newArrayList();

    public static void init() {
        try {
            for (final ClassPath.ClassInfo info : ClassPath.from(Thread.currentThread().getContextClassLoader()).getTopLevelClasses()) {
                if (info.getName().startsWith(SOURCES_PACKAGE)) {
                    final Class<?> clazz = info.load();
                    if(Source.class.isAssignableFrom(clazz)) {
                        String sourceName = getSourceName(clazz);
                        if(StringUtils.isNotEmpty(sourceName) && BotPropertiesHelper.getSources().contains(sourceName)) {
                            Source source = getInstance(clazz);
                            if(source != null) {
                                if(LOGGER.isDebugEnabled()) {
                                    LOGGER.debug("Adding " + sourceName + " to the registered sources");
                                }
                                sources.add(source);
                            }
                        }
                    }
                }
            }
        } catch(IOException e) {
            LOGGER.error("Could not register sources: " + LogFormatHelper.formatExceptionMessage(e));
        }
    }

    public static String getTweet() {
        if(sources != null && sources.size() > 0) {
            int i = 0;
            String sourceTweet = sources.get(new Random().nextInt(sources.size())).getTweet();
            while(StringUtils.isEmpty(sourceTweet) && i < MAX_SOURCE_TRIES) {
                sourceTweet = sources.get(new Random().nextInt(sources.size())).getTweet();
                i++;
            }
            return StringUtils.isNotEmpty(sourceTweet) && sourceTweet.length() <= 140 ? sourceTweet : null;
        }
        return null;
    }

    private static String getSourceName(Class sourceClass) {
        try {
            Field nameField = sourceClass.getDeclaredField("NAME");
            if(nameField != null) {
                nameField.setAccessible(true);
                Object value = nameField.get(null);
                if (value != null) {
                    return value.toString();
                }
            }
        } catch(NoSuchFieldException|IllegalAccessException e) {
            LOGGER.error("Could get source name for " + sourceClass.getName() + ": " + LogFormatHelper.formatExceptionMessage(e));
        }
        return StringUtils.EMPTY;
    }

    @SuppressWarnings("unchecked")
    private static Source getInstance(Class sourceClass) {
        try {
            return (Source) sourceClass.getConstructor().newInstance();
        } catch(NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
            LOGGER.error("Could not instanciate " + sourceClass.getName() + ": " + LogFormatHelper.formatExceptionMessage(e));
        }
        return null;
    }

    // Only for testing purposes, will go away
    public static void main(String[] args) throws Exception{
        BotPropertiesHelper.init(new String[]{});
        DropBoxHelper.init(BotPropertiesHelper.getDropBoxToken());
        init();

        // Triying out the sources
        for(int i = 0; i < 140; i++) {
            System.out.println(getTweet());
        }

        StatsHelper.dumpStats();
    }

}
