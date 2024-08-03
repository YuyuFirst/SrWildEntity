package com.yuyu.srwildentity.JDBC;

import org.bukkit.ChatColor;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;


/**
 * @author 峰。
 * @version 1.0
 * @project SrDisassemble
 * @date 2024/6/19 16:05:42
 * @description JDBC
 */
public class JdbcSqlClass {
    private static String driver;
    private static String url;
    private static String user;
    private static String password;



    public static List<UUID> getUUIDByAreaAndEntity(String area, String entity){
        List<UUID> uuids = new ArrayList<>();
        Connection connection = openConnection();
        String sql = "select * from areaentity where area = '"+area+"' and entityname = '"+entity+"'";
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                uuids.add(UUID.fromString(resultSet.getString("uuid")));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return uuids;
    }


    public static Connection openConnection() {
        //注册驱动
        try {
            Class.forName(JdbcSqlClass.driver);
            //获取连接
            Connection conn = DriverManager.getConnection(JdbcSqlClass.url, JdbcSqlClass.user, JdbcSqlClass.password);
            return conn;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static String getDriver() {
        return driver;
    }

    public static String getUrl() {
        return url;
    }

    public static String getUser() {
        return user;
    }

    public static String getPassword() {
        return password;
    }

    public static void setDriver(String driver) {
        JdbcSqlClass.driver = driver;
    }

    public static void setUrl(String url) {
        JdbcSqlClass.url = url;
    }

    public static void setUser(String user) {
        JdbcSqlClass.user = user;
    }

    public static void setPassword(String password) {
        JdbcSqlClass.password = password;
    }


    public static void initTable() {
        try {
            Connection connection = openConnection();

            Statement stmt = connection.createStatement();

            //执行Sql语句
            String createTable = " create table if not exists areaentity\t(  " +
                    "area       varchar(255) null comment '区域名称',\n" +
                    "    entityname varchar(255) null,\n" +
                    "    uuid       varchar(255) null comment '实体uuid')";

            boolean rs = stmt.execute(createTable);



            String sql = "select column_name\n" +
                    "from INFORMATION_SCHEMA.COLUMNS\n" +
                    "where TABLE_NAME = 'areaentity'";

            //手动注入所有列
            List<String> columns = new ArrayList<>();
            columns.add("area");
            columns.add("entityname");
            columns.add("uuid");

            try {
                ResultSet resultSet = stmt.executeQuery(sql);
                List<String> lists = new ArrayList<>();
                int index = 0;
                while (resultSet.next()) {
                    String columnName = resultSet.getString("column_name");
                    lists.add(columnName);
                    index++;
                }
                List<String> missColumn = new ArrayList<>();
                for (String columnName : columns) {
                    if (!lists.contains(columnName)) {
                        missColumn.add(columnName);
                    }
                }
                //新增列
                for (String columnName : missColumn) {
                    addColumn(columnName);
                }

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }


            connection.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static int  addColumn(String columns) {
        String sql = null;
        if (columns.equals("area")) {
            sql = "ALTER TABLE areaentity\n" +
                    "ADD area varchar(255)  null  comment '区域名'";
        } else if (columns.equals("entityname")) {
            sql = "ALTER TABLE areaentity\n" +
                    "ADD      entityname varchar(255) null comment '打开gui的名称'";
        } else if (columns.equals("uuid")) {
            sql = "ALTER TABLE areaentity\n" +
                    "ADD   uuid       varchar(255) null comment '实体uuid'";
        }

        if (sql != null) {
            try {
                Statement stmt = openConnection().createStatement();
                int i = stmt.executeUpdate(sql);
                return i;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return 0;
    }

    public static void setChartseUtf8() {
        try {
            Statement connection = openConnection().createStatement();

            //执行Sql语句

            String sql = "ALTER TABLE areaentity CONVERT TO CHARACTER SET utf8mb4;";
            boolean rs = connection.execute(sql);

            connection.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void deleteData(){
        //先清空数据库,再插入
        String sqlDelete = "DELETE FROM areaentity";

        Connection connection = openConnection();

        try {
            Statement statement = connection.createStatement();
            boolean execute = statement.execute(sqlDelete);
            if (execute){
                System.out.println(ChatColor.RED+"区域实体数据清空");
            }
            statement.close();
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public static void saveData(HashMap<UUID, String> hashMap){
        Connection connection = openConnection();
        try {
            Statement statement = connection.createStatement();
            for (UUID uuid : hashMap.keySet()) {
                String s = hashMap.get(uuid);
                String[] split = s.split("/");
                String area = split[0];
                String entity = split[1];
                String uuidString = uuid.toString();
                String sql = "INSERT INTO areaentity(area,entityname,uuid) VALUES('"+area+"','"+entity+"','"+uuidString+"')";
                statement.executeUpdate(sql);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
