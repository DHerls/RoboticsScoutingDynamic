package org.fullmetalfalcons.scouting.teams;

import com.dd.plist.NSDictionary;

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

    private final NSDictionary dictionary;


    public Team(NSDictionary dictionary){
        this.dictionary = dictionary;
    }

    /**
     * Returns the value in the dictionary based on the key provided
     *
     * @param key Key associated with the desired value
     * @return The value associated with the key in String form
     * @throws NullPointerException
     */
    public String getValue(String key) throws NullPointerException{
        return dictionary.get(key).toJavaObject().toString().trim();
    }
}
