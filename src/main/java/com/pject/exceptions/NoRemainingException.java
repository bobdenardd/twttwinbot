package com.pject.exceptions;

import com.pject.twitter.ApiCall;

/**
 * NoRemainingException - Short description of the class
 *
 * @author Camille
 *         Last: 04/09/15 16:55
 * @version $Id$
 */
public class NoRemainingException extends Exception {

    public NoRemainingException(ApiCall call) {
        super("Could not perform twitter api call for " + call.name() + " (" + call.call() + ")");
    }

}
