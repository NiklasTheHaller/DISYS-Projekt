package org.example.repository;

import org.example.config.DbConfig;

import java.sql.Connection;
import java.sql.SQLException;

public class Db {

    private final DbConfig dbConfig;

    public Db() {
        this.dbConfig = new DbConfig();
    }

    public Connection getConnection() throws SQLException {
        return dbConfig.getConnection();
    }
}
