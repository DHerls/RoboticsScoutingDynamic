package org.fullmetalfalcons.scouting.teams;

import com.dd.plist.NSDictionary;

/**
 * Holds team data
 *
 * Created by Dan on 1/11/2016.
 */
public class Team {
    public static final String MATCH_KEY = "match_num";
    public static final String COLOR_KEY = "team_color";
    public static final String NUMBER_KEY = "team_num";

    private NSDictionary dictionary;


    public Team(NSDictionary dictionary){
        this.dictionary = dictionary;
    }

    public Object getValue(String key) {
        return dictionary.get(key).toJavaObject();
    }
}
