package org.example.repository;

import org.example.model.Charge;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ChargeRepository {

    private static final String FIND_CHARGES_BY_CUSTOMER_ID = "SELECT * FROM charge WHERE customer_id = ?";
    private final Db db;

    public ChargeRepository() {
        this.db = new Db();
    }

    public List<Charge> findChargesByCustomerId(int customerId) {
        List<Charge> charges = new ArrayList<>();

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_CHARGES_BY_CUSTOMER_ID)) {

            stmt.setInt(1, customerId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    charges.add(new Charge(
                            rs.getInt("id"),
                            rs.getFloat("kwh"),
                            rs.getInt("customer_id")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return charges;
    }
}
