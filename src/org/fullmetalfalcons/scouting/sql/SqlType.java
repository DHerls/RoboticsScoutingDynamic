package org.fullmetalfalcons.scouting.sql;

import org.fullmetalfalcons.scouting.elements.Element;

/**
 * Created by Dan on 2/17/2016.
 */
public enum SqlType {
    INTEGER("INTEGER"),
    DECIMAL("DECIMAL"),
    STRING("VARCHAR");

    private String keyword;

    SqlType(String keyword){
        this.keyword = keyword;
    }

    public String getKeyword(){
        return keyword;
    }

    public String getKeyword(int length){
        if (length>0){
            return keyword+"("+length+")";
        } else {
            return keyword;
        }
    }

    public static SqlType getType(Element e){
        switch (e.getType()){

            case SEGMENTED_CONTROL:
                return STRING;
            case TEXTFIELD:
                if (e.getArguments()[0].equals("number")){
                    return INTEGER;
                } else if (e.getArguments()[0].equals("decimal")){
                    return DECIMAL;
                }
            case STEPPER:
                return INTEGER;
            case LABEL:
                return null;
            case SWITCH:
                return INTEGER;
            case SPACE:
                return null;
            case SLIDER:
                return DECIMAL;
            default:
                return null;
        }
    }
}
