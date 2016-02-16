package org.fullmetalfalcons.scouting.sql;

import java.sql.*;

/**
 * Created by djher on 2/16/2016.
 */
public class SqlWriter {

    private static Connection c;
    private static final String DATABASE_NAME = "scouting.db";
    private static final String TABLE_NAME = "team_data";

    public static void write(){
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:" + DATABASE_NAME);
            System.out.println("Opened database successfully");

            stmt = c.createStatement();
            String sql = "CREATE TABLE " + TABLE_NAME +
                    "(TEAM_NUM INT PRIMARY KEY     NOT NULL," +
                    " TEAM_NAME           TEXT    NOT NULL, " +
                    " TEAM_COLOR            TEXT     NOT NULL);";
            stmt.executeUpdate(sql);
            stmt.close();
            DatabaseMetaData meta = c.getMetaData();
            ResultSet res = meta.getTables(null, null, TABLE_NAME,
                    new String[] {"TABLE"});

            while (res.next()) {
                System.out.println(
                        "   "+res.getString("TABLE_CAT")
                                + ", "+res.getString("TABLE_SCHEM")
                                + ", "+res.getString("TABLE_NAME")
                                + ", "+res.getString("TABLE_TYPE")
                                + ", "+res.getString("REMARKS"));
            }

            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        System.out.println("Table created successfully");
    }
}
