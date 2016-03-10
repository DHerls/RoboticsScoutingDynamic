package org.fullmetalfalcons.scouting.sql;

import org.fullmetalfalcons.scouting.main.Main;

import java.sql.*;
import java.util.Arrays;
import java.util.Iterator;

/**
 *
 * Created by Dan on 2/17/2016.
 */
@SuppressWarnings("SameParameterValue")
class SqlUtil {

    @SuppressWarnings("UnusedReturnValue")
    public static boolean addColumn(Connection c, String tableName, String columnName, SqlType type){
        return addColumn(c,tableName,columnName,type,0,true);
    }

    public static boolean addColumn(Connection c,String tableName, String columnName, SqlType type, int length){
        return addColumn(c,tableName,columnName,type,length,true);
    }

    @SuppressWarnings("UnusedReturnValue")
    public static boolean addColumn(Connection c, String tableName, String columnName, SqlType type, boolean canBeNull){
        return addColumn(c,tableName,columnName,type,0,canBeNull);
    }

    @SuppressWarnings("WeakerAccess")
    public static boolean addColumn(Connection c, String tableName, String columnName, SqlType type, int length, boolean canBeNull){
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
        DatabaseMetaData meta;
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
//            System.out.println(sql);
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
        String substring = match_nums.replace("{","").replace("}","").replace("\'","");
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
            Statement s = c.createStatement();
            String base = "UPDATE " + tableName + " SET "+columnNameSet.getString("COLUMN_NAME")+" = ? WHERE team_num = " + teamNum;
            while (columnNameSet.next()){
                Object o = iterator.next();
                if (o instanceof Object[]){
                    base = base.replace("?",getArrayString((Object[]) o));
                    //System.out.println(Arrays.toString((String[]) o));
                    //System.out.println(getArrayString((Object[]) o));
                } else if (o instanceof Integer) {
                    base = base.replace("?",o.toString());
                } else if (o instanceof String){
                    base = base.replace("?","\'"+o+"\'");

                } else if (o instanceof Double){
                    base = base.replace("?",o.toString());
                }


            }
            s.execute(base);
            s.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static ResultSet retrieveAll(Connection c, String tableName) {
        try {
            Statement s = c.createStatement();
            return s.executeQuery("SELECT * FROM " + tableName);
        } catch (SQLException e) {
            Main.sendError("Problem reading local database " + e.getMessage(), true);
        }
        return null;
    }
}
