package org.example.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConfig {

    private String url;
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "postgres";

    public DbConfig(String url) {
        this.url = url;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, USERNAME, PASSWORD);
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
