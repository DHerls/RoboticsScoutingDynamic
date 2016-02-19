package org.fullmetalfalcons.scouting.sql;

import org.fullmetalfalcons.scouting.elements.Element;
import org.fullmetalfalcons.scouting.elements.ElementType;
import org.fullmetalfalcons.scouting.main.Main;
import org.fullmetalfalcons.scouting.teams.Team;

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

    public static void write() {
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:" + DATABASE_NAME);

            DatabaseMetaData meta = c.getMetaData();
            ResultSet res = meta.getTables(null, null, TABLE_NAME,
                    new String[]{"TABLE"});


            if (!res.next() || res.getString("TABLE_NAME") == null) {
                Main.log("Table doesn't exist, creating one");
                SqlUtil.createTable(c, TABLE_NAME, "team_num");
                SqlUtil.addColumn(c, TABLE_NAME, "team_color", SqlType.STRING, false);
                SqlUtil.addColumn(c, TABLE_NAME, "num_matches", SqlType.INTEGER, false);
                SqlUtil.addColumn(c, TABLE_NAME, "match_nums", SqlType.STRING, true);
                addElementColumns();
            } else {
                Main.log("Table " + TABLE_NAME + " exists");
            }

            updateRecords();

            c.close();

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    private static void updateRecords() {
        ArrayList<Object> records = new ArrayList<>();

        for (Team t: Main.getTeams()){
            records.clear();
            if (SqlUtil.doesTeamRecordExist(c,TABLE_NAME,Integer.parseInt(t.getValue("team_num")))){
                updateTeamRecord(t);
            } else {
                addNewTeam(t);

            }
        }

    }

    private static void updateTeamRecord(Team t) {
        ArrayList<Object> records = new ArrayList<>();
        ResultSet teamSet = SqlUtil.getTeamRecord(c, TABLE_NAME, Integer.parseInt(t.getValue("team_num")));
        try {
            ArrayList<String> matches = new ArrayList<>(Arrays.asList(SqlUtil.getArrayFromString(teamSet.getString("match_nums"))));
            for (String m: matches) {
                if (t.getValue("match_num").equals(m)) {
                    return;
                }
            }

            records.add(Integer.parseInt(t.getValue("team_num")));
            records.add(t.getValue("team_color"));
            records.add(1+teamSet.getInt("num_matches"));
            matches.add(t.getValue("match_num"));
            records.add(matches.toArray(new String[0]));
            for(Element e: Main.getElements()){
                switch (e.getType()){

                    case SEGMENTED_CONTROL:
                        for (int i = 0; i<e.getArguments().length;i++){
                            if (t.getValue(e.getKeys()[0]).equals(e.getArguments()[i])){
                                System.out.println("add One");
                                records.add(1+teamSet.getInt(e.getColumnValues()[i]));
                            } else {
                                System.out.println("Don't add one");
                                records.add(teamSet.getInt(e.getColumnValues()[i]));
                            }
                        }
                        break;
                    case TEXTFIELD:
                        if (e.getArguments()[0].equalsIgnoreCase("number") || e.getArguments()[0].equalsIgnoreCase("decimal")){
                            try {
                                records.add(Integer.parseInt(t.getValue(e.getKeys()[0])) + teamSet.getInt(e.getColumnValues()[0]));
                            } catch (NumberFormatException e1){
                                records.add(Double.parseDouble(t.getValue(e.getKeys()[0]))+ teamSet.getDouble(e.getColumnValues()[0]));
                            }
                        }
                        break;
                    case STEPPER:
                        records.add(Integer.parseInt(t.getValue(e.getKeys()[0])) + teamSet.getInt(e.getColumnValues()[0]));
                        break;
                    case LABEL:
                        break;
                    case SWITCH:
                        for (int i = 0; i<e.getKeys().length;i++){
                            if (t.getValue(e.getKeys()[i]).equals("yes")){
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
                        records.add(Double.parseDouble(t.getValue(e.getKeys()[0])) + teamSet.getDouble(e.getColumnValues()[0]));
                        break;
                }
            }

                SqlUtil.updateTeamRecord(c,TABLE_NAME,records.toArray(new Object[0]),t.getValue("team_num"));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void addNewTeam(Team t) {
        ArrayList<Object> records = new ArrayList<>();
        records.add(Integer.parseInt(t.getValue("team_num")));
        records.add(t.getValue("team_color"));
        records.add(1);
        Integer[] matchNums = {Integer.parseInt(t.getValue("match_num"))};
        records.add(matchNums);
        for(Element e: Main.getElements()){
            switch (e.getType()){

                case SEGMENTED_CONTROL:
                    for (String value: e.getArguments()){
                        if (t.getValue(e.getKeys()[0]).equals(value)){
                            records.add(1);
                        } else {
                            records.add(0);
                        }
                    }
                    break;
                case TEXTFIELD:
                    if (e.getArguments()[0].equalsIgnoreCase("number") || e.getArguments()[0].equalsIgnoreCase("decimal")){
                        try {
                            records.add(Integer.parseInt(t.getValue(e.getKeys()[0])));
                        } catch (NumberFormatException e1){
                            records.add(Double.parseDouble(t.getValue(e.getKeys()[0])));
                        }
                    }
                    break;
                case STEPPER:
                    records.add(Integer.parseInt(t.getValue(e.getKeys()[0])));
                    break;
                case LABEL:
                    break;
                case SWITCH:
                    for (String key: e.getKeys()){
                        if (t.getValue(key).equals("yes")){
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
                    records.add(Double.parseDouble(t.getValue(e.getKeys()[0])));
                    break;
            }
        }

        SqlUtil.addTeamRecord(c,TABLE_NAME,records.toArray(new Object[0]));
    }

    private static void addElementColumns() {
        for (Element e: Main.getElements()){
            for (String s : e.getColumnValues()){
                SqlUtil.addColumn(c,TABLE_NAME,s,SqlType.getType(e));
            }
        }
    }
}
