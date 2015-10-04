package com.pject.sources.helpers;

import org.apache.commons.lang3.StringUtils;

import java.security.SecureRandom;
import java.util.Random;

/**
 * ExpressionsHelper - Short description of the class
 *
 * @author Camille
 *         Last: 27/09/2015 21:05
 * @version $Id$
 */
public class ExpressionsHelper implements TeenageExpressions {

    private static final Random RANDOM  = new SecureRandom();
    private static final int MAX_EMOJIS = 3;
    private static final String SPACE   = " ";

    public static String getRandomExpression() {
        if (RANDOM.nextBoolean()) {
            String expression = EXPRESSIONS.get(RANDOM.nextInt(EXPRESSIONS.size()));
            expression = getPrefixed(expression);
            expression = getSwaggmaned(expression);
            return expression;
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
        return StringUtils.trimToEmpty(getRandomEmojis() + SPACE + getRandomExpression());
    }

    private static String getPrefixed(String expression) {
        if(RANDOM.nextBoolean()) {
            return PREFIXES.get(RANDOM.nextInt(PREFIXES.size())) + SPACE + expression;
        }
        return expression;
    }

    private static String getSwaggmaned(String expression) {
        return StringUtils.isNotEmpty(expression) && expression.endsWith("é") && RANDOM.nextBoolean() ?
                replaceLast(expression, "é", "ey") : expression;
    }

    // Ugly shit from stackoverflow, ain't time for this
    private static String replaceLast(String string, String substring, String replacement) {
        int index = string.lastIndexOf(substring);
        if (index == -1) {
            return string;
        }
        return string.substring(0, index) + replacement + string.substring(index+substring.length());
    }

}
