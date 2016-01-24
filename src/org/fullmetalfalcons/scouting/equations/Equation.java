package org.fullmetalfalcons.scouting.equations;

import expr.Expr;
import expr.Parser;
import expr.SyntaxException;
import org.fullmetalfalcons.scouting.elements.Element;
import org.fullmetalfalcons.scouting.main.Main;
import org.fullmetalfalcons.scouting.teams.Team;

/**
 * Holds and does math with equations
 *
 * Created by Dan on 1/13/2016.
 */
public class Equation {

    private String equation;
    private final String name;


    public Equation(String line){
        String[] splitLine = line.split("=");
        name = splitLine[0].trim();
        equation = splitLine[1].trim();
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
                    String[] args = e.getArguments();
                    //The program can only parse if the options are "Yes", "No", and "Try/Fail"
                    if (args[0].equalsIgnoreCase("yes") && args[1].equalsIgnoreCase("no")
                            && args[2].equalsIgnoreCase("try/fail")){
                        for (String key: e.getKeys()){
                            switch(t.getValue(key).toLowerCase()){
                                case "yes":
                                    equation = equation.replace(key.toLowerCase(),"1");
                                    break;
                                case "no":
                                    equation = equation.replace(key.toLowerCase(),"0");
                                    break;
                                case "try/fail":
                                    equation = equation.replace(key.toLowerCase(),"0.5");
                                    break;
                            }
                        }

                    } else {
                        for (String key: e.getKeys()){
                            equation = equation.replace(key,"0");
                        }
                    }
                    break;

                //For a textfield
                case TEXTFIELD:
                    for (String arg: e.getArguments()){
                        if (arg.equalsIgnoreCase("number")){
                            for(String key: e.getKeys()){
                                try{
                                    //To make sure it's a number, parse int, then parse back to string
                                    equation = equation.replace(key,String.valueOf(Integer.parseInt(t.getValue(key))));
                                } catch(NumberFormatException e1){
                                    equation = equation.replace(key,"0");
                                    Main.sendError("Textfield: " + key + " does not have a numeric value. You should not be seeing this error.",false);
                                }
                            }
                        } else if (arg.equalsIgnoreCase("decimal")){
                            for(String key: e.getKeys()){
                                try{
                                    //To make sure it's a number, parse int, then parse back to string
                                    equation = equation.replace(key,String.valueOf(Double.parseDouble(t.getValue(key))));
                                } catch(NumberFormatException e1){
                                    equation = equation.replace(key,"0");
                                    Main.sendError("Textfield: " + key + " does not have a numeric value. You should not be seeing this error.",false);
                                }
                            }
                        }
                    }

                    break;
                case STEPPER:

                    for(String key: e.getKeys()){
                        try{
                            //To make sure it's a number, parse int, then parse back to string
                            equation = equation.replace(key,String.valueOf(Integer.parseInt(t.getValue(key))));
                        } catch(NumberFormatException e1){
                            equation = equation.replace(key,"0");
                            Main.sendError(key + " does not have a numeric value. You should not be seeing this error.",false);
                        }
                    }
                    break;
                //In case anyone is stupid enough to add labels to the equation, replace with 0
                case LABEL:
                    for(String key: e.getKeys()){
                        equation = equation.replace(key,"0");
                    }
                    break;
                //Yesses are 1, Nos are 0
                case SWITCH:
                    for(String key: e.getKeys()){
                        if (t.getValue(key).equalsIgnoreCase("yes")){
                            equation = equation.replace(key,"1");
                        } else if (t.getValue(key).equalsIgnoreCase("no")){
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
                            equation = equation.replace(key, String.valueOf(Double.parseDouble(t.getValue(key))));
                        } catch (NumberFormatException e1){
                            equation = equation.replace(key,"0");
                            Main.sendError("Slider: " + key + " returned a non-numeric value.  You should not be seeing this error.",false);
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
            Main.sendError("Equation error in " + name + ": " + e.toString(),false);
        }
        return value;
    }

    /**
     * Takes the name of the equation, puts them all to lowercase, then capitalizes each word
     *
     * @return Properly formatted name of equation
     */
    public String getName() {
        //Break string appart
        String[] nameSplit = name.split(" ");
        StringBuilder b = new StringBuilder();
        //Put them all to lowercase
        for (String s: nameSplit){
            b.append(s.substring(0,1).toUpperCase()).append(s.substring(1).toLowerCase()).append(" ");
        }
        return b.toString();
    }
}
