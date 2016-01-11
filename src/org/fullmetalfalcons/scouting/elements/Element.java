package org.fullmetalfalcons.scouting.elements;

import org.fullmetalfalcons.scouting.exceptions.ElementParseException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by djher on 1/11/2016.
 */
public class Element {

    private ElementType type;
    private String description;
    private String key;
    private String[] arguments;

    private String argumentRegex = "<(.*)?>";
    private Pattern argumentPattern = Pattern.compile(argumentRegex);
    private Matcher argumentMatcher;

    public Element(String line) throws ElementParseException {
        parseString(line);

    }

    private void parseString(String line) throws ElementParseException {
        String[] splitLine = line.split(";;");
        if (splitLine.length!=3){
            throw new ElementParseException("Incorrect number of arguments, 3 expected: " + splitLine[0]);
        }

        argumentMatcher = argumentPattern.matcher(splitLine[0]);
        if (!argumentMatcher.find()){
            throw new ElementParseException("Type arguments not found: " + splitLine[0]);
        }
        arguments = argumentMatcher.group(1).split(",");

        type = ElementType.getElement(splitLine[0].replaceAll(argumentRegex,"").trim());
        if (type==null){
            throw new ElementParseException("Element Type not recognized: " + splitLine[0].replaceAll(argumentRegex,""));
        }

        description = splitLine[1].trim();
        key = splitLine[2].trim();


    }
}
