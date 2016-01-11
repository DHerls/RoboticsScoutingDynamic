package org.fullmetalfalcons.scouting.elements;

/**
 * Created by djher on 1/11/2016.
 */
public enum ElementType {

    SEGMENTED_CONTROL("segmented_control"),
    TEXTFIELD("textfield"),
    STEPPER("stepper"),
    LABEL("label");

    private String key;

    ElementType(String key){
        this.key = key;
    }

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
