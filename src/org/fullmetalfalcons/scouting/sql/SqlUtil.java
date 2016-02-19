package org.fullmetalfalcons.scouting.sql;

import org.fullmetalfalcons.scouting.elements.Element;
import org.fullmetalfalcons.scouting.main.Main;
import org.fullmetalfalcons.scouting.teams.Team;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

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

    public static void addTeamRecord(Connection c, String tableName, Object[] records) {
        DatabaseMetaData meta = null;
        String sql = "INSERT INTO " + tableName + " (";
        try {
            meta = c.getMetaData();
            ResultSet columnNameSet = meta.getColumns(null,null,tableName,null);
            while (columnNameSet.next()){
                sql = sql + columnNameSet.getString("COLUMN_NAME") + ", ";
            }
            sql = sql.substring(0,sql.length()-2);
            sql = sql + ") VALUES (";
            for (Object o: records){
                if (o instanceof Object[]){
                    sql = sql + getArrayString((Object[]) o);
                } else if (o instanceof Integer) {
                    sql = sql + o;
                } else if (o instanceof String){
                    sql = sql + "\'"+o+"\'";
                } else if (o instanceof Double){
                    sql = sql + o;
                }
                sql = sql + ", ";
            }

            sql = sql.substring(0,sql.length()-2);
            sql = sql + ")";
            Statement statement = c.createStatement();
            statement.executeUpdate(sql);
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static String getArrayString(Object[] o) {
        String result = "\'{";
        for (Object obj: o){
            result = result + obj.toString() + ", ";
        }
        result = result.substring(0,result.length()-2);
        result = result + "}\'";

        return result;
    }

    public static ResultSet getTeamRecord(Connection c, String tableName, int team_num) {
        try {
            Statement statement = c.createStatement();
            return statement.executeQuery("SELECT * FROM " + tableName + " WHERE team_num=" +team_num);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String[] getArrayFromString(String match_nums) {
        String substring = match_nums.substring(1,match_nums.length()-1);
        return substring.split(", ");
    }

    public static void updateTeamRecord(Connection c, String tableName, Object[] records, String teamNum) {
        DatabaseMetaData meta = null;
        try {
            meta = c.getMetaData();
            ResultSet columnNameSet = meta.getColumns(null,null,tableName,null);
            PreparedStatement statement;
            Iterable<Object> oi = Arrays.asList(records);
            Iterator<Object> iterator = oi.iterator();
            while (columnNameSet.next()){
                statement = c.prepareStatement("UPDATE " + tableName + " SET "+columnNameSet.getString("COLUMN_NAME")+" = ? WHERE team_num = " + teamNum);
                Object o = iterator.next();
                if (o instanceof Object[]){
                    statement.setString(1,getArrayString((Object[]) o));
                } else if (o instanceof Integer) {
                    statement.setString(1,o.toString());
                } else if (o instanceof String){
                    statement.setString(1,"\'"+o+"\'");
                } else if (o instanceof Double){
                    statement.setString(1,o.toString());
                }
                statement.execute();
                statement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
