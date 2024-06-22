package org.example.repository;

import org.example.model.Station;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StationRepository {

    private static final String FIND_ALL_STATIONS = "SELECT * FROM station";
    private final Db db;

    public StationRepository() {
        // Use the station database URL
        this.db = new Db("jdbc:postgresql://localhost:30002/stationdb");
    }

    public List<Station> findAll() {
        List<Station> stations = new ArrayList<>();

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_ALL_STATIONS);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                stations.add(new Station(
                        rs.getInt("id"),
                        rs.getString("db_url"),
                        rs.getFloat("lat"),
                        rs.getFloat("lng")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stations;
    }
}
