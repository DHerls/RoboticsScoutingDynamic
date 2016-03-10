package org.fullmetalfalcons.scouting.sql;

import org.fullmetalfalcons.scouting.main.Main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.*;

/**
 * Handles low level communication with SQL databases
 *
 * Created by Dan on 2/17/2016.
 */
@SuppressWarnings("SameParameterValue")
class SqlUtil {

    //Holds a list of SQL error codes and their messages
    private static final HashMap<String, String> SQL_CODES = new HashMap<>();

    /**
     * Adds a column to an already existing table in a specified SQL database
     *
     * @param c Conenction to the databse
     * @param tableName Name of the table to add columns to
     * @param columnName Name of the column to add
     * @param type Type of column
     */
    public static void addColumn(Connection c, String tableName, String columnName, SqlType type){
        addColumn(c,tableName,columnName,type,0,true);
    }

    /**
     * Adds a column to an already existing table in a specified SQL database
     *
     * @param c Conenction to the databse
     * @param tableName Name of the table to add columns to
     * @param columnName Name of the column to add
     * @param type Type of column
     * @param length Max length of the column
     */
    public static void addColumn(Connection c,String tableName, String columnName, SqlType type, int length){
        addColumn(c,tableName,columnName,type,length,true);
    }

    /**
     * Adds a column to an already existing table in a specified SQL database
     *
     * @param c Conenction to the databse
     * @param tableName Name of the table to add columns to
     * @param columnName Name of the column to add
     * @param type Type of column
     * @param canBeNull Whether the column can be null
     */
    public static void addColumn(Connection c, String tableName, String columnName, SqlType type, boolean canBeNull){
        addColumn(c,tableName,columnName,type,0,canBeNull);
    }

    /**
     * Adds a column to an already existing table in a specified SQL database
     *
     * @param c Conenction to the databse
     * @param tableName Name of the table to add columns to
     * @param columnName Name of the column to add
     * @param type Type of column
     * @param length Max length of the column
     * @param canBeNull Whether the column can be null
     */
    @SuppressWarnings("WeakerAccess")
    public static void addColumn(Connection c, String tableName, String columnName, SqlType type, int length, boolean canBeNull){
        try {
            Statement statement = c.createStatement();
            String sql = "ALTER TABLE " + tableName +
                    " ADD " + columnName.toLowerCase() +
                    " " + type.getKeyword(length) +
                    (canBeNull? "":" NOT NULL DEFAULT " + (type.equals(SqlType.STRING)? "\'MISSING_NAME\'" : "0"));
            sql = sql.replace("\\","_");
            sql = sql.replace("/","_");
            statement.executeUpdate(sql);
            statement.close();
        } catch (SQLException e) {
            Main.sendError("Problem adding column", false, e);
        }

    }

    /**
     * Creates a table in the specified database with a single primary column
     *
     * @param c Connection to database
     * @param tableName Name of table
     * @param primaryColumn Name of primary column
     */
    public static void createTable(Connection c, String tableName, String primaryColumn) {
        try {
            Statement statement = c.createStatement();
            String sql = "CREATE TABLE " + tableName + " (" + primaryColumn + " INT PRIMARY KEY NOT NULL)";
            statement.executeUpdate(sql);
            statement.close();
        } catch (SQLException e) {
            Main.sendError("Problem creating table", false, e);
        }
    }

    /**
     * Checks if a team exists in a given table
     *
     * @param c Connection to database
     * @param tableName Name of table
     * @param teamNum Number of team to check
     * @return Whether the team exists in the table
     */
    public static boolean doesTeamRecordExist(Connection c,String tableName, int teamNum){
        try {
            Statement statement = c.createStatement();
            String sql = "SELECT * FROM " + tableName + " WHERE team_num=" + teamNum;
            ResultSet rs = statement.executeQuery(sql);
            return rs.next();
        } catch (SQLException e) {
            Main.sendError("Problem checking team record", false, e);
            return false;
        }
    }

    /**
     * Adds a new row in a table with given team values
     *
     * @param c Connection to database
     * @param tableName Name of table to insert into
     * @param records Data to be added
     */
    public static void addTeamRecord(Connection c, String tableName, Object[] records) {
        DatabaseMetaData meta;
        try {
            meta = c.getMetaData();
            ResultSet columnNameSet = meta.getColumns(null,null,tableName,null);
            String base = "INSERT INTO " + tableName + " VALUES(";
            while (columnNameSet.next()){
                base += "?, ";
            }
            base = base.substring(0,base.length()-2);
            base+=")";
            PreparedStatement statement = c.prepareStatement(base);
            for (int i = 0; i < records.length; i++) {
                Object o = records[i];
                if (o instanceof Object[]) {
                    statement.setString(i+1, getArrayString((Object[]) o));
                } else {
                    statement.setObject(i+1, o);
                }
            }
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            Main.sendError("Problem adding team record", false, e);
        }
    }

    /**
     * Convert array of strings to a single string enclosed by curly braces
     *
     * @param o String array to be converted
     * @return String of array
     */
    private static String getArrayString(Object[] o) {
        String result = "\'{";
        for (Object obj: o){
            result = result + obj.toString() + ", ";
        }
        result = result.substring(0,result.length()-2);
        result = result + "}\'";

        return result;
    }

    /**
     * Retrieve a sepcified team's record from a given database
     *
     * @param c Connection to database
     * @param tableName name of table
     * @param team_num number of team to retrieve
     * @return ResultSet of team data
     */
    public static ResultSet getTeamRecord(Connection c, String tableName, int team_num) {
        try {
            Statement statement = c.createStatement();
            return statement.executeQuery("SELECT * FROM " + tableName + " WHERE team_num=" +team_num);
        } catch (SQLException e) {
            Main.sendError("Problem retrieving team record",false, e);
            return null;
        }
    }

    /**
     * Convert string of array to an array object
     *
     * @param match_nums String to be spliced and converted
     * @return String array object
     */
    public static String[] getArrayFromString(String match_nums) {
        String substring = match_nums.replace("{","").replace("}","").replace("\'","");
        return substring.split(", ");
    }

    /**
     * Replaces an existing team's data in a given table with new data
     *
     * @param c Connection to database
     * @param tableName Name of table
     * @param records Records to be passed into database
     * @param teamNum Number of team data to be replaced
     */
    public static void updateTeamRecord(Connection c, String tableName, Object[] records, String teamNum) {
        DatabaseMetaData meta;
        try {
            meta = c.getMetaData();
            ResultSet columnNameSet = meta.getColumns(null,null,tableName,null);
            String base = "UPDATE " + tableName + " SET ";
            while (columnNameSet.next()){
                base += columnNameSet.getString("COLUMN_NAME") + "= ?, ";
            }
            base = base.substring(0,base.length()-2);
            base+= "WHERE team_num=" +teamNum;
            PreparedStatement statement = c.prepareStatement(base);
            for (int i = 0; i < records.length; i++) {
                Object o = records[i];
                if (o instanceof Object[]) {
                    statement.setString(i+1, getArrayString((Object[]) o));
                } else {
                    statement.setObject(i+1, o);
                }
            }
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            Main.sendError("Problem updating team record", false,e);
        }
    }

    /**
     * Retrieve all data from a given table in a database
     *
     * @param c Connection to database
     * @param tableName Table name
     * @return ResultSet containing all data
     */
    public static ResultSet retrieveAll(Connection c, String tableName) {
        try {
            Statement s = c.createStatement();
            return s.executeQuery("SELECT * FROM " + tableName);
        } catch (SQLException e) {
            Main.sendError("Problem reading database",false,e);
        }
        return null;
    }

    /**
     * Drop table from given database
     *
     * @param c Connection to database
     * @param tableName Name of table to drop
     */
    public static void clearData(Connection c, String tableName) {
        try {
            Statement s = c.createStatement();
            s.execute("DROP TABLE " + tableName);
        } catch (SQLException e) {
            Main.sendError("Problem deleting local database",false,e);
        }
    }

}
