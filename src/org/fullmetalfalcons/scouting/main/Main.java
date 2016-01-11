package org.fullmetalfalcons.scouting.main;

import com.dd.plist.NSDictionary;
import org.fullmetalfalcons.scouting.elements.Element;
import org.fullmetalfalcons.scouting.exceptions.ElementParseException;
import org.fullmetalfalcons.scouting.fileio.Reader;
import org.fullmetalfalcons.scouting.teams.Team;

import java.util.ArrayList;

/**
 * Created by Dan on 1/11/2016.
 */
public class Main {

    private static final ArrayList<Element> ELEMENTS = new ArrayList<>();
    private static final ArrayList<Team> TEAMS = new ArrayList<>();

    public static void main(String args[]){
        Reader.loadConfig();
        Reader.loadPlists();
    }

    public static void addElement(String line){

        try {
            Element e = new Element(line);
            ELEMENTS.add(e);
        } catch (ElementParseException e) {
            e.printStackTrace();
        }
    }

    public static void addTeam(NSDictionary dictionary){
        Team t = new Team(dictionary);
        TEAMS.add(t);
    }
}
