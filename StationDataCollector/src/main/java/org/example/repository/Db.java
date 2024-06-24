package org.example.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Db {

    private static String dbUrl = "jdbc:postgresql://localhost:30011/stationdb";

    public static void setDbUrl(String dbUrl) {
        Db.dbUrl = "jdbc:postgresql://" + dbUrl + "/stationdb";
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, "postgres", "postgres");
    }
}
