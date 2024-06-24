package org.example.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConfig {

    private static final String URL = "jdbc:postgresql://localhost:30012/stationdb";
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "postgres";

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
}
