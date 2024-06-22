package org.example.repository;

import org.example.config.DbConfig;

import java.sql.Connection;
import java.sql.SQLException;

public class Db {

    private DbConfig dbConfig;

    public Db(String url) {
        this.dbConfig = new DbConfig(url);
    }

    public Connection getConnection() throws SQLException {
        return dbConfig.getConnection();
    }
}
