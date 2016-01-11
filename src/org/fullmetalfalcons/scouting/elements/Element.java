package org.fullmetalfalcons.scouting.elements;

import org.fullmetalfalcons.scouting.exceptions.ElementParseException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class holds information on each individual element including its type, description, arguments, and key
 * Holds data from the config file
 *
 * Created by Dan on 1/11/2016.
 */
public class Element {

    private ElementType type;
    private String description;
    private String key;
    private String[] arguments;

    //The below elements are used to capture the data between arrows <Like this>
    private String argumentRegex = "<(.*)?>";
    private Pattern argumentPattern = Pattern.compile(argumentRegex);
    private Matcher argumentMatcher;

    /**
     * Constructor for the Element Class, requires a line from the config file
     *
     * @param line A line read from the config file
     * @throws ElementParseException If there is a problem with the line/ it does not conform to conventions
     */
    public Element(String line) throws ElementParseException {
        parseString(line);

    }

    /**
     * Breaks the line apart and assignes its parts to different variables.
     *
     * @param line
     * @throws ElementParseException
     */
    private void parseString(String line) throws ElementParseException {
        //Arguments in the config file should be separated by ";;"
        String[] splitLine = line.split(";;");
        //There should be no more and no less than three arguments per line
        if (splitLine.length!=3){
            throw new ElementParseException("Incorrect number of arguments, 3 expected: " + splitLine[0]);
        }

        argumentMatcher = argumentPattern.matcher(splitLine[0]);
        //Checks to see if there is any information <like this> in the first portion of the line
        if (argumentMatcher.find()){
            arguments = argumentMatcher.group(1).split(",");
        } else {
            throw new ElementParseException("Type arguments not found: " + splitLine[0]);
        }

        //Retrieves the ElementType based on the portion of the first section not in <> i.e. LABEL
        type = ElementType.getElement(splitLine[0].replaceAll(argumentRegex,"").trim());
        if (type==null){
            throw new ElementParseException("Element Type not recognized: " + splitLine[0].replaceAll(argumentRegex,""));
        }

        description = splitLine[1].trim();
        key = splitLine[2].trim();


    }
}
