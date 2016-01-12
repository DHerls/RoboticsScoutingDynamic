package org.fullmetalfalcons.scouting.main;

import com.dd.plist.NSDictionary;
import org.fullmetalfalcons.scouting.elements.Element;
import org.fullmetalfalcons.scouting.exceptions.ElementParseException;
import org.fullmetalfalcons.scouting.fileio.Reader;
import org.fullmetalfalcons.scouting.fileio.Writer;
import org.fullmetalfalcons.scouting.teams.Team;

import javax.swing.*;
import java.util.ArrayList;

/**
 * Main class
 *
 * Created by Dan on 1/11/2016.
 */
public class Main {

    private static final ArrayList<Element> ELEMENTS = new ArrayList<>();
    private static final ArrayList<Team> TEAMS = new ArrayList<>();
    //Console spam
    private static final boolean DEBUG = true;

    public static void main(String args[]){
        try {
            log("Program Starting");
            log("Starting to load configuration");
            Reader.loadConfig();
            log(ELEMENTS.size() + " elements loaded");
            log("Starting to load plists");
            Reader.loadPlists();
            log(TEAMS.size() + " teams loaded");
            log("Starting to write file");
            Writer.write();
            log("Results saved to " + Writer.FILENAME);
            log("Exiting program");
            System.exit(0);
        } catch(Exception e){
            sendError("Unknown error occurred: " + e.toString());
            System.exit(-1);
        }


    }

    public static void addElement(String line){

        try {
            Element e = new Element(line);
            debug("Element of type " + e.getType().toString() + " created");
            ELEMENTS.add(e);
        } catch (ElementParseException e) {
            sendError("Config error: " + e.getMessage());
        }
    }

    public static void addTeam(NSDictionary dictionary){
        Team t = new Team(dictionary);
        debug("Team " + t.getValue(Team.NUMBER_KEY) + " loaded");
        TEAMS.add(t);
    }

    public static ArrayList<Element> getElements(){
        return ELEMENTS;
    }

    public static ArrayList<Team> getTeams() {
        return TEAMS;
    }

    /**
     * Sends an error message to the user with a JOptionPane
     *
     * @param message error message to send
     */
    public static void sendError(String message){
        try {
            JOptionPane.showMessageDialog(null, message,
                    "You done messed up", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e){
            JOptionPane.showMessageDialog(null, "An error occurred while displaying an error",
                    "Yo Dawg!", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void debug(String message){
        if (DEBUG){
            System.out.println(message);
        }
    }

    public static void log(String message){
        System.out.println(message);
    }
}
