package org.fullmetalfalcons.scouting.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * Created by Dan on 2/17/2016.
 */
public class SqlUtil {

    public static boolean addColumn(Connection c,String tableName, String columnName, SqlType type){
        return addColumn(c,tableName,columnName,type,0,true);
    }

    public static boolean addColumn(Connection c,String tableName, String columnName, SqlType type, int length){
        return addColumn(c,tableName,columnName,type,length,true);
    }

    public static boolean addColumn(Connection c,String tableName, String columnName, SqlType type, boolean canBeNull){
        return addColumn(c,tableName,columnName,type,0,canBeNull);
    }

    public static boolean addColumn(Connection c,String tableName, String columnName, SqlType type, int length, boolean canBeNull){
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
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

    }

    public static void createTable(Connection c, String tableName, String primaryColumn) {
        try {
            Statement statement = c.createStatement();
            String sql = "CREATE TABLE " + tableName + " (" + primaryColumn + " INT PRIMARY KEY NOT NULL)";
            statement.executeUpdate(sql);
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean doesTeamRecordExist(Connection c,String tableName, int teamNum){
        try {
            Statement statement = c.createStatement();
            String sql = "SELECT * FROM " + tableName + " WHERE team_num=" + teamNum;
            ResultSet rs = statement.executeQuery(sql);
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
