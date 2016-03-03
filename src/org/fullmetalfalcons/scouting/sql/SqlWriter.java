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

    public static void write(String sqlLocation) {
        try {
            Class.forName("org.sqlite.JDBC");
            File file = new File((sqlLocation.isEmpty()? sqlLocation: sqlLocation.charAt(sqlLocation.length()-1)=='/'?sqlLocation:sqlLocation+"/") + DATABASE_NAME);
            if (file.getParentFile() != null) {
                //noinspection ResultOfMethodCallIgnored
                file.getParentFile().mkdirs();
            }
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();
            c = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());

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

            ResultSet rs = SqlUtil.test(c,TABLE_NAME);
            while (rs!=null && rs.next()){
                System.out.println(rs.getInt("team_num") + ":" + rs.getInt("human_uses_gestures_no")*1.0/rs.getInt("num_matches"));
                System.out.println(rs.getInt("human_uses_gestures_no") + ":" + rs.getInt("num_matches"));
            }


            c.close();

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void updateRecords() {
        for (Team t: Main.getTeams()){
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
            records.add(matches.toArray(new String[matches.size()]));
            for(Element e: Main.getElements()){
                switch (e.getType()){

                    case SEGMENTED_CONTROL:
                        for (int i = 0; i<e.getArguments().length;i++){
                            if (t.getValue(e.getKeys()[0]).equalsIgnoreCase(e.getArguments()[i])){
                                records.add(1+teamSet.getInt(e.getColumnValues()[i]));
                            } else {
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
                            if (t.getValue(e.getKeys()[i]).equalsIgnoreCase("yes")){
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
            double total = 0.0;

            for (Equation e: Main.getEquations()){
                double value = e.evaluate(t) + teamSet.getDouble(e.getColumnValue());
                records.add(value);
                total +=value;
            }

            records.add(total);

            SqlUtil.updateTeamRecord(c,TABLE_NAME, records.toArray(new Object[records.size()]),t.getValue("team_num"));

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
                        if (e.getKeys()[0].equals("human_uses_gestures")){
                        }
                        if (t.getValue(e.getKeys()[0]).equalsIgnoreCase(value)){
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
                        if (t.getValue(key).equalsIgnoreCase("yes")){
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
