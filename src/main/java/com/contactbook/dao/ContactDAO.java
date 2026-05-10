package com.contactbook.dao;


import com.contactbook.config.DatabaseConfig;
import com.contactbook.model.Contact;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ContactDAO {

    // ==================== CRUD ====================

    public void add(Contact c) throws SQLException {
        String sql = """
            INSERT INTO contacts (first_name, last_name, email, phone, address) 
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, c.getFirstName());
            ps.setString(2, c.getLastName());
            ps.setString(3, c.getEmail());
            ps.setString(4, c.getPhone());
            ps.setString(5, c.getAddress());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) c.setId(keys.getInt(1));
            }
        }
    }

    public Optional<Contact> findById(int id) throws SQLException {
        String sql = "SELECT * FROM contacts WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    // ==================== LIKE SEARCH ====================

    public List<Contact> search(String keyword) throws SQLException {
        // Multi-column LIKE with parameterized wildcards
        String sql = """
            SELECT * FROM contacts 
            WHERE first_name LIKE ? 
               OR last_name LIKE ? 
               OR email LIKE ? 
               OR phone LIKE ? 
            ORDER BY last_name, first_name
            """;

        List<Contact> results = new ArrayList<>();
        String pattern = "%" + keyword + "%";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 1; i <= 4; i++) {
                ps.setString(i, pattern);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) results.add(mapRow(rs));
            }
        }
        return results;
    }

    // ==================== PAGINATION (LIMIT/OFFSET) ====================

    public List<Contact> findAllPaginated(int limit, int offset) throws SQLException {
        String sql = """
            SELECT * FROM contacts 
            ORDER BY last_name, first_name 
            LIMIT ? OFFSET ?
            """;

        List<Contact> results = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);
            ps.setInt(2, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) results.add(mapRow(rs));
            }
        }
        return results;
    }

    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM contacts";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // ==================== UPDATE / DELETE ====================

    public boolean update(Contact c) throws SQLException {
        String sql = """
            UPDATE contacts 
            SET first_name=?, last_name=?, email=?, phone=?, address=? 
            WHERE id=?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, c.getFirstName());
            ps.setString(2, c.getLastName());
            ps.setString(3, c.getEmail());
            ps.setString(4, c.getPhone());
            ps.setString(5, c.getAddress());
            ps.setInt(6, c.getId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM contacts WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // ==================== ResultSetMetaData ====================

    public void printResultSetMetaData() throws SQLException {
        // Use LIMIT 0 for schema-only, no data transfer
        String sql = "SELECT * FROM contacts LIMIT 0";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();

            System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
            System.out.println("║              RESULTSET METADATA (contacts)                     ║");
            System.out.println("╠════════════════════════════════════════════════════════════════╣");
            System.out.printf("║ %-4s %-16s %-16s %-8s %-10s ║%n",
                    "Col#", "Name", "Type", "Size", "Nullable");
            System.out.println("╠════════════════════════════════════════════════════════════════╣");

            for (int i = 1; i <= colCount; i++) {
                String nullable = meta.isNullable(i) == ResultSetMetaData.columnNullable
                        ? "YES" : "NO";
                System.out.printf("║ %-4d %-16s %-16s %-8d %-10s ║%n",
                        i,
                        meta.getColumnName(i),
                        meta.getColumnTypeName(i),
                        meta.getColumnDisplaySize(i),
                        nullable
                );
            }
            System.out.println("╚════════════════════════════════════════════════════════════════╝");
        }
    }

    // ==================== MAPPING ====================

    private Contact mapRow(ResultSet rs) throws SQLException {
        Contact c = new Contact();
        c.setId(rs.getInt("id"));
        c.setFirstName(rs.getString("first_name"));
        c.setLastName(rs.getString("last_name"));
        c.setEmail(rs.getString("email"));
        c.setPhone(rs.getString("phone"));
        c.setAddress(rs.getString("address"));

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) c.setCreatedAt(ts.toLocalDateTime());

        return c;
    }
}