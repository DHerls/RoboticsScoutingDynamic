package org.fullmetalfalcons.scouting.equations;

import expr.Expr;
import expr.Parser;
import expr.SyntaxException;
import org.fullmetalfalcons.scouting.elements.Element;
import org.fullmetalfalcons.scouting.exceptions.EquationParseException;
import org.fullmetalfalcons.scouting.main.Main;
import org.fullmetalfalcons.scouting.teams.Team;

import java.math.BigDecimal;

/**
 * Used to evaluate equations as written in the config file
 * Replaces keys with their associated value
 *
 * Created by Dan on 1/13/2016.
 */
public class Equation {

    private String equation;
    private String name;
    private String columnValue;

    /**
     * Constructor for Equation class
     *
     * @param line line from config file that contains equation data
     */
    public Equation(String line) throws EquationParseException {
        try{
            //Line should come in the form EQUATION_NAME=Equation
            String[] splitLine = line.split("=");
            name = splitLine[0].trim();
            equation = splitLine[1].trim();
        } catch(ArrayIndexOutOfBoundsException e){
            throw new EquationParseException("Config error, equation missing \"=\"");
        }
    }

    /**
     * Evaluates the equation for a Team based on the current values the Team has
     *
     * Works by replacing each key found in the equation with the value associated with the key
     *
     * @param t  The team whose score is calculated
     * @return The calculated score
     */
    public double evaluate(Team t){
        //For each Element
        for (Element e: Main.getElements()){
            //Different types need to be handled differently
            switch(e.getType()){

                case SEGMENTED_CONTROL:
                    //The program can only parse if the options are "Yes", "No", and "Try/Fail"
                    for (String key : e.getKeys()) {
                        switch (t.getStringValue(key).toLowerCase()) {
                            case "yes":
                                equation = equation.replace(key, "1");
                                break;
                            case "no":
                                equation = equation.replace(key, "0");
                                break;
                            case "try/fail":
                            case "try_fail":
                            case "try fail":
                                equation = equation.replace(key, "0.5");
                                break;
                            default:
                                equation = equation.replace(key, "0");
                        }
                    }
                    break;

                //For a textfield
                case TEXTFIELD:
                    for (String arg: e.getArguments()){
                        //Can only parse if arguments are "number" or "decimal"
                        if (arg.equalsIgnoreCase("number")){
                            for(String key: e.getKeys()){
                                //To make sure it's a number, parse int, then parse back to string
                                equation = equation.replace(key,String.valueOf(t.getIntValue(key)));

                            }
                        } else if (arg.equalsIgnoreCase("decimal")){
                            for(String key: e.getKeys()){
                                try{
                                    //To make sure it's a number, parse int, then parse back to string
                                    equation = equation.replace(key,String.valueOf(t.getDoubleValue(key)));
                                } catch(NumberFormatException e1){
                                    equation = equation.replace(key,"0");
                                    Main.sendError("Textfield: " + key + " does not have a numeric value. You should not be seeing this error.",false,e1);
                                }
                            }
                        }
                    }

                    break;
                case STEPPER:

                    for(String key: e.getKeys()){
                        try{
                            //To make sure it's a number, parse int, then parse back to string
                            equation = equation.replace(key,String.valueOf(t.getIntValue(key)));
                        } catch(NumberFormatException e1){
                            equation = equation.replace(key,"0");
                            Main.sendError(key + " does not have a numeric value. You should not be seeing this error.",false,e1);
                        }
                    }
                    break;
                //Labels have no keys, literally can't do anything
                case LABEL:
                    break;
                //Literally cannot do anything
                case SPACE:
                    break;
                //Yesses are 1, Nos are 0
                case SWITCH:
                    for(String key: e.getKeys()){
                        if (t.getStringValue(key).equalsIgnoreCase("yes")){
                            equation = equation.replace(key,"1");
                        } else if (t.getStringValue(key).equalsIgnoreCase("no")){
                            equation = equation.replace(key,"0");
                        } else {
                            equation = equation.replace(key,"0");
                            Main.sendError("Switch: " + key + " returned a value that was not Yes or No. You should not be seeing this error.",false);
                        }
                    }
                    break;
                case SLIDER:
                    for(String key: e.getKeys()){
                        try {
                            equation = equation.replace(key, String.valueOf(t.getDoubleValue(key)));
                        } catch (NumberFormatException e1){
                            equation = equation.replace(key,"0");
                            Main.sendError("Slider: " + key + " returned a non-numeric value.  You should not be seeing this error.",false,e1);
                        }
                    }
                    break;
            }
        }
        Double value = 0.0;
        try {
            //Uses the EPXR library by darius
            Expr expr = Parser.parse(equation);
            //Calculate the value of the equation
            value = expr.value();
        } catch (SyntaxException e) {
            Main.sendError("Equation " + name + " is formatted incorrectly",false,e);
        }
        return round(value,3);
    }

    /**
     * Takes the name of the equation, puts all words to lowercase, then capitalizes each word
     *
     * @return Properly formatted name of equation
     */
    public String getName() {
        //Break string apart
        String[] nameSplit = name.split(" ");
        StringBuilder b = new StringBuilder();
        //Put them all to lowercase
        for (String s: nameSplit){
            b.append(s.substring(0,1).toUpperCase()).append(s.substring(1).toLowerCase()).append(" ");
        }
        return b.toString();
    }

    public String getColumnValue(){
        if (this.columnValue !=null){
            return this.columnValue;
        }

        columnValue = name.replace("\\","_")
                .replace("/","_")
                .replace(" ","_")
                .toLowerCase()
                .trim();
        columnValue += "_score";

        return this.columnValue;

    }

    private double round(double value, @SuppressWarnings("SameParameterValue") int numberOfDigitsAfterDecimalPoint) {
        BigDecimal bigDecimal = new BigDecimal(value);
        bigDecimal = bigDecimal.setScale(numberOfDigitsAfterDecimalPoint,
                BigDecimal.ROUND_HALF_UP);
        return bigDecimal.doubleValue();
    }
}
