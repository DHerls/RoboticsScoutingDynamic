package org.fullmetalfalcons.scouting.exceptions;

/**
 * Created by djher on 1/11/2016.
 */
public class ElementParseException extends Exception {

    public ElementParseException(String message){
        super(message);
    }

    public ElementParseException(Throwable cause){
        super(cause);
    }

    public ElementParseException(String message, Throwable cause){
        super(message,cause);
    }

}
