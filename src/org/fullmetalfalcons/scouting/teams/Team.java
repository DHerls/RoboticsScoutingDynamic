package org.fullmetalfalcons.scouting.teams;

import com.dd.plist.NSDictionary;
import org.fullmetalfalcons.scouting.main.Main;

/**
 * Holds team data, basically just a fancy wrapper for a HashMap
 *
 * Created by Dan on 1/11/2016.
 */
public class Team {

    //These are keys which should never change
    public static final String MATCH_KEY = "match_num";
    public static final String COLOR_KEY = "team_color";
    public static final String NUMBER_KEY = "team_num";
    private final String fileName;

    private final NSDictionary dictionary;


    public Team(NSDictionary dictionary, String fileName){
        this.dictionary = dictionary;
        this.fileName = fileName;
    }

    /**
     * Returns the value in the dictionary based on the key provided
     *
     * @param key Key associated with the desired value
     * @return The value associated with the key in String form
     * @throws NullPointerException
     */
    public String getStringValue(String key){
        try {

            return dictionary.get(key).toJavaObject().toString().trim();


        } catch (NullPointerException e){
            Main.log(key);
            Main.sendError(String.format("File %s is missing key %s, defaulting to 0",fileName,key),true,e);
            return "";

        }


    }

    public int getIntValue(String key){
        try {

            return Integer.parseInt(dictionary.get(key).toJavaObject().toString().trim());


        } catch (NullPointerException e){
            Main.sendError(String.format("File %s is missing key %s, defaulting to 0",fileName,key),false,e);
            return 0;
        }catch (NumberFormatException e){
            Main.sendError(String.format("Key %s in file %s is not an Integer",key,fileName),false,e);

        }

        return 0;


    }

    public double getDoubleValue(String key){
        try {

            return Double.parseDouble(dictionary.get(key).toJavaObject().toString().trim());


        } catch (NullPointerException e){
            Main.sendError(String.format("File %s is missing key %s, defaulting to 0",fileName,key),false,e);
            return 0.0;
        }catch (NumberFormatException e){
            Main.sendError(String.format("Key %s in file %s is not an Integer",key,fileName),false,e);
            return 0.0;
        }


    }

    public Number getNumberValue(String key){
        try {

            return Double.parseDouble(dictionary.get(key).toJavaObject().toString().trim());


        } catch (NullPointerException e){
            Main.sendError(String.format("File %s is missing key %s, defaulting to 0",fileName,key),false,e);
            return 0.0;

        } catch (NumberFormatException e){
            try {
                return Integer.parseInt(dictionary.get(key).toJavaObject().toString().trim());
            } catch (NumberFormatException e1){
                Main.sendError(String.format("Key %s in file %s is not a number",key,fileName),false,e);
                return 0.0;
            }
        }


    }

    public Object getValue(String key){
        try {

            return Double.parseDouble(dictionary.get(key).toJavaObject().toString().trim());


        } catch (NullPointerException e){
            Main.sendError(String.format("File %s is missing key %s, defaulting to 0",fileName,key),false,e);
            return 0.0;

        } catch (NumberFormatException e){
            try {
                return Integer.parseInt(dictionary.get(key).toJavaObject().toString().trim());
            } catch (NumberFormatException e1){
                return dictionary.get(key).toJavaObject().toString().trim();
            }
        }


    }

    public String getFileName() {
        return fileName;
    }

}
