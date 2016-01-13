package org.fullmetalfalcons.scouting.fileio;

import com.dd.plist.NSDictionary;
import com.dd.plist.PropertyListFormatException;
import com.dd.plist.PropertyListParser;
import org.fullmetalfalcons.scouting.main.Main;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.text.ParseException;

/**
 * Handles file input
 *
 * Created by Dan on 1/11/2016.
 */
public class Reader {

    private static final String CONFIG_FILE_NAME = "config.txt";

    /**
     *Loads the config file and assigns the lines to elements
     */
    public static void loadConfig(){
        try (BufferedReader reader = new BufferedReader(new FileReader(CONFIG_FILE_NAME))){
            Main.debug("Config file " + CONFIG_FILE_NAME + " loaded");
            String line;
            //While there are still lines in the file
            while((line=reader.readLine())!=null){
                //If the line does not start with ##, which indicates a comment, and @ which indicated an eqation
                if (line.length()>2 && !line.substring(0,2).equals("##") && line.charAt(0)!='@'){
                    //Attempt to add an Element to the main array
                    Main.addElement(line);
                }

            }

        } catch (IOException e) {
            Main.sendError("Something is very wrong with the config file. It's probably missing. Try and find it.");
            System.exit(-1);
        }


    }

    /**
     * Loads .plist files from the specified directory and creates Team Objects based off of them
     * @param arg The location of the directory which contains plists
     */
    public static void loadPlists(String arg){
        File plistDirectory = new File(arg);
        //Only retrieve files that end in ".plist"
        File[] plistFiles = plistDirectory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".plist");
            }
        });
        Main.debug(plistFiles.length + " plists discovered");
        for (File f: plistFiles){
            Main.debug("Loading plist " + f.getName());
            try {
                //Attempt to load the plist into an NSDictionary, which is basically a HashMap
                NSDictionary rootDict = (NSDictionary) PropertyListParser.parse(f);
                Main.debug("Discovered " + rootDict.size() + " key/value pairs");

                //If it successfully creates an NSDictionary, it passes it to a Team object
                Main.addTeam(rootDict);
            } catch (IOException | PropertyListFormatException | ParserConfigurationException | ParseException | SAXException e) {
                Main.sendError("An error has occurred with one of the plists: " + f.getName() + "\n" + e.getLocalizedMessage());
            } catch (IllegalArgumentException | IndexOutOfBoundsException e){
                Main.sendError("Someone has changed the plists... and they did a bad job");
            }
        }
    }
}
