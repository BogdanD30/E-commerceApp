package com.ecomm.repository;

import com.ecomm.model.CarMake;
import com.ecomm.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class CarMakeRepository {


    public boolean create(CarMake carMake) {
        String sql = "INSERT INTO car_makes (make_name) VALUES (?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, carMake.getMakeName());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        carMake.setMakeId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error creating car make: " + e.getMessage());
        }
        return false;
    }


    public Optional<CarMake> findById(int makeId) {
        String sql = "SELECT * FROM car_makes WHERE make_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, makeId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(extractCarMakeFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding car make by ID: " + e.getMessage());
        }
        return Optional.empty();
    }


    public Optional<CarMake> findByName(String makeName) {
        String sql = "SELECT * FROM car_makes WHERE make_name = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, makeName);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(extractCarMakeFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding car make by name: " + e.getMessage());
        }
        return Optional.empty();
    }


    public List<CarMake> findAll() {
        List<CarMake> carMakes = new ArrayList<>();
        String sql = "SELECT * FROM car_makes ORDER BY make_name ASC";

        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                carMakes.add(extractCarMakeFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all car makes: " + e.getMessage());
        }
        return carMakes;
    }


    public List<CarMake> searchByName(String searchTerm) {
        List<CarMake> carMakes = new ArrayList<>();
        String sql = "SELECT * FROM car_makes WHERE make_name ILIKE ? ORDER BY make_name ASC";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + searchTerm + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                carMakes.add(extractCarMakeFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error searching car makes: " + e.getMessage());
        }
        return carMakes;
    }


    public boolean update(CarMake carMake) {
        String sql = "UPDATE car_makes SET make_name = ? WHERE make_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, carMake.getMakeName());
            pstmt.setInt(2, carMake.getMakeId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating car make: " + e.getMessage());
        }
        return false;
    }


    public boolean delete(int makeId) {
        String sql = "DELETE FROM car_makes WHERE make_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, makeId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting car make: " + e.getMessage());
        }
        return false;
    }


    public boolean exists(int makeId) {
        String sql = "SELECT COUNT(*) FROM car_makes WHERE make_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, makeId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking car make existence: " + e.getMessage());
        }
        return false;
    }


    public boolean existsByName(String makeName) {
        String sql = "SELECT COUNT(*) FROM car_makes WHERE make_name = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, makeName);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking car make name existence: " + e.getMessage());
        }
        return false;
    }


    public int count() {
        String sql = "SELECT COUNT(*) FROM car_makes";

        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting car makes: " + e.getMessage());
        }
        return 0;
    }


    private CarMake extractCarMakeFromResultSet(ResultSet rs) throws SQLException {
        CarMake carMake = new CarMake();
        carMake.setMakeId(rs.getInt("make_id"));
        carMake.setMakeName(rs.getString("make_name"));
        carMake.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return carMake;
    }
}