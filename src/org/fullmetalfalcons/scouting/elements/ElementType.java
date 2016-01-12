package org.fullmetalfalcons.scouting.elements;

/**
 * This Enum holds the list of types for elements and contains methods that return the appropriate element based on
 * a string
 *
 * Created by Dan on 1/11/2016.
 */
public enum ElementType {

    SEGMENTED_CONTROL("segmented_control"),
    TEXTFIELD("textfield"),
    STEPPER("stepper"),
    LABEL("label"),
    SWITCH("switch");

    private String key;

    ElementType(String key){
        this.key = key;
    }

    /**
     * Parses a string and returns the appropriate ElementType
     *
     * @param input String to be parsed
     * @return The ElementType that matches input or null if none match
     */
    public static ElementType getElement(String input){
        for (ElementType e: values()){
            if (input.toLowerCase().trim().equals(e.getKey())){
                return e;
            }
        }
        return null;
    }

    public String getKey(){
        return key;
    }
}
