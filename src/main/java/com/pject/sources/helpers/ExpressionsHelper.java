package com.pject.sources.helpers;

import org.apache.commons.lang3.StringUtils;

import java.util.Random;

/**
 * ExpressionsHelper - Short description of the class
 *
 * @author Camille
 *         Last: 27/09/2015 21:05
 * @version $Id$
 */
public class ExpressionsHelper implements TeenageExpressions {

    private static final Random RANDOM  = new Random();
    private static final int MAX_EMOJIS = 3;

    public static String getRandomExpression() {
        if (RANDOM.nextBoolean()) {
            return EXPRESSIONS.get(RANDOM.nextInt(EXPRESSIONS.size()));
        }
        return StringUtils.EMPTY;
    }

    public static String getRandomEmojis() {
        if(RANDOM.nextBoolean()) {
            StringBuilder builder = new StringBuilder();
            for(int i = 0; i < RANDOM.nextInt(MAX_EMOJIS + 1); i++) {
                builder.append(EMOJIS.get(RANDOM.nextInt(EMOJIS.size())));
            }
            return StringUtils.trimToEmpty(builder.toString());
        }
        return StringUtils.EMPTY;
    }

    public static String getRandomEmojiedExpression() {
        return StringUtils.trimToEmpty(getRandomEmojis() + " " + getRandomExpression());
    }

}
