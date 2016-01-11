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
 * Created by djher on 1/11/2016.
 */
public class Reader {

    private static final String CONFIG_FILE_NAME = "config.txt";
    private static final String PLIST_PATH = "plists/";

    public static void loadConfig(){
        try (BufferedReader reader = new BufferedReader(new FileReader(CONFIG_FILE_NAME));){

            String line;

            while((line=reader.readLine())!=null){
                if (!line.isEmpty() && line.length()>2 && !line.substring(0,2).equals("##")){
                    //System.out.println(line);
                    Main.addElement(line);
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static void loadPlists(){
        File plistDirectory = new File(PLIST_PATH);
        //Only retrieve files that end in ".plist"
        File[] plistFiles = plistDirectory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".plist");
            }
        });

        for (File f: plistFiles){
            try {
                NSDictionary rootDict = (NSDictionary) PropertyListParser.parse(f);
                Main.addTeam(rootDict);
            } catch (IOException | PropertyListFormatException | ParserConfigurationException | ParseException | SAXException e) {
                e.printStackTrace();
            }
        }
    }
}
