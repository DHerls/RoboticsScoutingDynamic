package org.fullmetalfalcons.scouting.sql;

import org.fullmetalfalcons.scouting.elements.Element;
import org.fullmetalfalcons.scouting.main.Main;

import java.sql.*;

/**
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
                SqlUtil.addColumn(c, TABLE_NAME, "team_name", SqlType.STRING, false);
                SqlUtil.addColumn(c, TABLE_NAME, "team_color", SqlType.STRING, false);
                SqlUtil.addColumn(c, TABLE_NAME, "num_matches", SqlType.INTEGER, false);
                SqlUtil.addColumn(c, TABLE_NAME, "match_nums", SqlType.STRING, false);
                addElementColumns();
            } else {
                Main.log("Table " + TABLE_NAME + " exists");
            }

            System.out.println(SqlUtil.doesTeamRecordExist(c,TABLE_NAME,4557));
            System.out.println(SqlUtil.doesTeamRecordExist(c,TABLE_NAME,3));

            updateRecords();

            c.close();

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    private static void updateRecords() {

    }

    private static void addElementColumns() {
        for (Element e: Main.getElements()){
            switch(e.getType()){
                case SEGMENTED_CONTROL:
                    for (String s: e.getArguments()){
                        SqlUtil.addColumn(c,TABLE_NAME,e.getKeys()[0]+"_"+s,SqlType.getType(e));
                    }
                    break;
                case TEXTFIELD:
                    if (e.getArguments()[0].equalsIgnoreCase("number") || e.getArguments()[0].equalsIgnoreCase("decimal")){
                        SqlUtil.addColumn(c,TABLE_NAME,e.getKeys()[0],SqlType.getType(e));
                    }
                    break;
                case STEPPER:
                    SqlUtil.addColumn(c,TABLE_NAME,e.getKeys()[0],SqlType.getType(e));
                    break;
                case LABEL:
                    break;
                case SWITCH:
                    SqlUtil.addColumn(c,TABLE_NAME,e.getKeys()[0]+"_yes",SqlType.getType(e));
                    SqlUtil.addColumn(c,TABLE_NAME,e.getKeys()[0]+"_no",SqlType.getType(e));
                    break;
                case SPACE:
                    break;
                case SLIDER:
                    SqlUtil.addColumn(c,TABLE_NAME,e.getKeys()[0],SqlType.getType(e));
                    break;
            }
        }
    }
}
