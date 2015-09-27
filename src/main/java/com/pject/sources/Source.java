package com.pject.sources;

import org.apache.commons.lang3.StringUtils;

/**
 * Source - Short description of the class
 *
 * @author Camille
 *         Last: 27/09/2015 16:32
 * @version $Id$
 */
public interface Source {

    String NAME = StringUtils.EMPTY;

    String getTweet();

}
