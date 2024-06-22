package org.example.repository;

import org.example.model.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerRepository {

    private static final String FIND_CUSTOMER_BY_ID = "SELECT * FROM customer WHERE id = ?";
    private final Db db;

    public CustomerRepository() {
        // Use the customer database URL
        this.db = new Db("jdbc:postgresql://localhost:30001/customerdb");
    }

    public Customer findCustomerById(int customerId) {
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_CUSTOMER_BY_ID)) {

            stmt.setInt(1, customerId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Customer(
                            rs.getInt("id"),
                            rs.getString("first_name"),
                            rs.getString("last_name")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
