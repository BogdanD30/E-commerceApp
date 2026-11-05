package com.ecomm.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Opens JDBC connections. Settings come from environment variables so no credentials live in the
 * source code; the defaults match the database started by docker-compose.yml.
 */
public class DatabaseUtil {

    private static final String URL = env("DB_URL", "jdbc:postgresql://localhost:5433/ecomm_auto");
    private static final String USER = env("DB_USER", "ecomm");
    private static final String PASSWORD = env("DB_PASSWORD", "ecomm_dev");

    static {
        DriverManager.setLoginTimeout(5); // fail fast when the database is down instead of freezing the UI
    }

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return (value == null || value.isBlank()) ? fallback : value;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn.isValid(2);
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            return false;
        }
    }
}
