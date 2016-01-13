package org.fullmetalfalcons.scouting.exceptions;

/**
 * Custom Exception
 *
 * Created by Dan on 1/11/2016.
 */
@SuppressWarnings("unused")
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
