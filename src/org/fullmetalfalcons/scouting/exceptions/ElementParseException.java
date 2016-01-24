package org.fullmetalfalcons.scouting.exceptions;

/**
 * Custom Exception
 *
 * Created by Dan on 1/11/2016.
 */
@SuppressWarnings({"serial"})
public class ElementParseException extends Exception {

    public ElementParseException(String message){
        super(message);
    }

    @SuppressWarnings("unused")
    public ElementParseException(Throwable cause){
        super(cause);
    }

    @SuppressWarnings("unused")
    public ElementParseException(String message, Throwable cause){
        super(message,cause);
    }

}
