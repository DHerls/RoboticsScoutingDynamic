package org.fullmetalfalcons.scouting.sql;

import org.fullmetalfalcons.scouting.elements.Element;
import org.fullmetalfalcons.scouting.equations.Equation;
import org.fullmetalfalcons.scouting.main.Main;
import org.fullmetalfalcons.scouting.teams.Team;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * Created by djher on 2/16/2016.
 */
public class SqlWriter {

    private static Connection c;
    private static final String DATABASE_NAME = "scouting.db";
    private static final String TABLE_NAME = "team_data";

    /**
     * Writes the current team data to an SQLite database at the location specified
     * @param sqlLocation Folder to write the database in
     */

    public static void write(String sqlLocation) {
        try {
            //Driver to read SQLite databaes
            Class.forName("org.sqlite.JDBC");
            File file = new File((sqlLocation.isEmpty()? sqlLocation: sqlLocation.charAt(sqlLocation.length()-1)=='/'?sqlLocation:sqlLocation+"/") + DATABASE_NAME);
            if (file.getParentFile() != null) {
                //noinspection ResultOfMethodCallIgnored
                file.getParentFile().mkdirs();
            }
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();
            //Open the database for read/write
            c = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());

            //Get database metadata
            DatabaseMetaData meta = c.getMetaData();
            ResultSet res = meta.getTables(null, null, TABLE_NAME,
                    new String[]{"TABLE"});

            //If there are no tables in the database
            if (!res.next() || res.getString("TABLE_NAME") == null) {
                //Create table with a bunch of blank columns
                Main.log("Table doesn't exist, creating one");
                SqlUtil.createTable(c, TABLE_NAME, Team.NUMBER_KEY);
                SqlUtil.addColumn(c, TABLE_NAME, Team.COLOR_KEY, SqlType.STRING, false);
                SqlUtil.addColumn(c, TABLE_NAME, "num_matches", SqlType.INTEGER, false);
                SqlUtil.addColumn(c, TABLE_NAME, "match_nums", SqlType.STRING, true);
                addElementColumns();
            } else {
                Main.log("Table " + TABLE_NAME + " exists");
            }

            updateRecords();

            c.close();

        } catch (ClassNotFoundException e) {
            Main.sendError("Cannot find driver to read database", false);
        } catch (SQLException e) {
            Main.sendError("Error getting data from database", false);
        } catch (IOException e) {
            Main.sendError("Cannot find database specified", false);
        }
    }

    /**
     * Put team data in the database
     */
    private static void updateRecords() {
        for (Team t: Main.getTeams()){
            //If the team already exists
            if (SqlUtil.doesTeamRecordExist(c,TABLE_NAME,t.getIntValue(Team.NUMBER_KEY))){
                //Update team record
                updateTeamRecord(t);
            } else {
                //Create a new row in database
                addNewTeam(t);

            }
        }

    }

    /**
     * Called if a team exists and new data is to be added
     *
     * @param t The team to be added
     */
    private static void updateTeamRecord(Team t) {
        ArrayList<Object> records = new ArrayList<>();
        //Get existing team data
        ResultSet teamSet = SqlUtil.getTeamRecord(c, TABLE_NAME, t.getIntValue(Team.NUMBER_KEY));
        try {
            ArrayList<String> matches = new ArrayList<>(Arrays.asList(SqlUtil.getArrayFromString(teamSet.getString("match_nums"))));
            for (String m: matches) {
                //If the match number already exists
                if (t.getValue(Team.MATCH_KEY).equals(m)) {
                    return;
                }
            }

            records.add(t.getIntValue(Team.NUMBER_KEY));
            records.add(t.getValue(Team.COLOR_KEY));
            records.add(1+teamSet.getInt("num_matches"));
            matches.add(t.getStringValue(Team.MATCH_KEY));
            records.add(matches.toArray(new String[matches.size()]));
            for(Element e: Main.getElements()){
                switch (e.getType()){

                    case SEGMENTED_CONTROL:
                        for (int i = 0; i<e.getArguments().length;i++){
                            if (t.getStringValue(e.getKeys()[0]).equalsIgnoreCase(e.getArguments()[i])){
                                records.add(1+teamSet.getInt(e.getColumnValues()[i]));
                            } else {
                                records.add(teamSet.getInt(e.getColumnValues()[i]));
                            }
                        }
                        break;
                    case TEXTFIELD:
                        if (e.getArguments()[0].equalsIgnoreCase("number")) {
                            records.add(t.getIntValue(e.getKeys()[0]) + teamSet.getInt(e.getColumnValues()[0]));

                        } else if (e.getArguments()[0].equalsIgnoreCase("decimal")){
                            records.add(t.getDoubleValue(e.getKeys()[0]) + teamSet.getInt(e.getColumnValues()[0]));
                        }
                        break;
                    case STEPPER:
                        records.add(t.getIntValue(e.getKeys()[0]) + teamSet.getInt(e.getColumnValues()[0]));
                        break;
                    case LABEL:
                        break;
                    case SWITCH:
                        for (int i = 0; i<e.getKeys().length;i++){
                            if (t.getStringValue(e.getKeys()[i]).equalsIgnoreCase("yes")){
                                records.add(1 + teamSet.getInt(e.getColumnValues()[i*2]));
                                records.add(teamSet.getInt(e.getColumnValues()[i*2+1]));
                            } else {
                                records.add(teamSet.getInt(e.getColumnValues()[i*2]));
                                records.add(1 + teamSet.getInt(e.getColumnValues()[i*2+1]));
                            }
                        }
                        break;
                    case SPACE:
                        break;
                    case SLIDER:
                        records.add(t.getDoubleValue(e.getKeys()[0]) + teamSet.getDouble(e.getColumnValues()[0]));
                        break;
                }
            }
            double total = 0.0;

            for (Equation e: Main.getEquations()){
                double value = e.evaluate(t) + teamSet.getDouble(e.getColumnValue());
                records.add(value);
                total +=value;
            }

            records.add(total);

            SqlUtil.updateTeamRecord(c,TABLE_NAME, records.toArray(new Object[records.size()]),t.getStringValue(Team.NUMBER_KEY));

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NullPointerException e){
            Main.sendError("Cannot read team data for team " + t.getValue(Team.NUMBER_KEY),false);
        }
    }

    private static void addNewTeam(Team t) {
        ArrayList<Object> records = new ArrayList<>();
        records.add(t.getIntValue(Team.NUMBER_KEY));
        records.add(t.getValue(Team.COLOR_KEY));
        records.add(1);
        Integer[] matchNums = {t.getIntValue(Team.MATCH_KEY)};
        records.add(matchNums);
        for(Element e: Main.getElements()){
            switch (e.getType()){

                case SEGMENTED_CONTROL:
                    for (String value: e.getArguments()){
                        if (e.getKeys()[0].equals("human_uses_gestures")){
                        }
                        if (t.getStringValue(e.getKeys()[0]).equalsIgnoreCase(value)){
                            records.add(1);
                        } else {
                            records.add(0);
                        }
                    }
                    break;
                case TEXTFIELD:
                    if (e.getArguments()[0].equalsIgnoreCase("number") || e.getArguments()[0].equalsIgnoreCase("decimal")){
                            records.add(t.getNumberValue(e.getKeys()[0]));
                    }
                    break;
                case STEPPER:
                    records.add(t.getIntValue(e.getKeys()[0]));
                    break;
                case LABEL:
                    break;
                case SWITCH:
                    for (String key: e.getKeys()){
                        if (t.getStringValue(key).equalsIgnoreCase("yes")){
                            records.add(1);
                            records.add(0);
                        } else {
                            records.add(0);
                            records.add(1);
                        }
                    }
                    break;
                case SPACE:
                    break;
                case SLIDER:
                    records.add(t.getDoubleValue(e.getKeys()[0]));
                    break;
            }
        }
        double total = 0.0;
        for (Equation e: Main.getEquations()){
            double value = e.evaluate(t);
            total+=value;
            records.add(value);
        }
        records.add(total);

        SqlUtil.addTeamRecord(c,TABLE_NAME, records.toArray(new Object[records.size()]));
    }

    private static void addElementColumns() {
        for (Element e: Main.getElements()){
            for (String s : e.getColumnValues()){
                SqlUtil.addColumn(c,TABLE_NAME,s,SqlType.getType(e));
            }
        }

        for (Equation e: Main.getEquations()){
            SqlUtil.addColumn(c,TABLE_NAME,e.getColumnValue(),SqlType.DECIMAL);
        }

        SqlUtil.addColumn(c,TABLE_NAME,"grand_total",SqlType.DECIMAL);
    }
}
