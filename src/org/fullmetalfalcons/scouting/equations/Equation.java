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
    private String name;

    public Equation(String line){
        String[] splitLine = line.split("=");
        name = splitLine[0];
        equation = splitLine[1];
    }

    public double evaluate(Team t){
        for (Element e: Main.getElements()){
            switch(e.getType()){

                case SEGMENTED_CONTROL:
                    for (String key: e.getKeys()){
                        equation = equation.replace(key.toLowerCase(),"1");
                    }
                    break;
                case TEXTFIELD:
                    for(String key: e.getKeys()){
                        try{
                            //To make sure it's a number, parse double, then parse back to string
                            equation = equation.replace(key,String.valueOf(Double.parseDouble(t.getValue(key))));
                        } catch(NumberFormatException e1){
                            Main.sendError("Fuck");
                        }
                    }
                    break;
                case STEPPER:
                    for(String key: e.getKeys()){
                        try{
                            //To make sure it's a number, parse int, then parse back to string
                            equation = equation.replace(key,String.valueOf(Integer.parseInt(t.getValue(key))));
                        } catch(NumberFormatException e1){
                            Main.sendError("Fuck");
                        }
                    }
                    break;
                case LABEL:
                    break;
                case SWITCH:
                    for(String key: e.getKeys()){
                        if (t.getValue(key).toLowerCase().trim().equals("yes")){
                            equation = equation.replace(key,"1");
                        } else {
                            equation = equation.replace(key,"0");
                        }
                    }
                    break;
            }
        }
        Double value = 0.0;
        try {
            Expr expr = Parser.parse(equation);
            value = expr.value();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public String getName() {
        String[] nameSplit = name.split(" ");
        StringBuilder b = new StringBuilder();
        for (String s: nameSplit){
            b.append(s.toLowerCase()).append(" ");
        }
        String end = b.toString();
        return end.substring(0,1).toUpperCase()+end.substring(1);
    }
}
