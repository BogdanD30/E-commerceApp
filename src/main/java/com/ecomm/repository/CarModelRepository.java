package com.ecomm.repository;

import com.ecomm.model.CarModel;
import com.ecomm.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class CarModelRepository {


    public boolean create(CarModel carModel) {
        String sql = "INSERT INTO car_models (make_id, model_name) VALUES (?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, carModel.getMakeId());
            pstmt.setString(2, carModel.getModelName());
            pstmt.setString(3, carModel.getBodyType());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        carModel.setModelId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error creating car model: " + e.getMessage());
        }
        return false;
    }


    public Optional<CarModel> findById(int modelId) {
        String sql = "SELECT cm.*, cmk.make_name " +
                "FROM car_models cm " +
                "JOIN car_makes cmk ON cm.make_id = cmk.make_id " +
                "WHERE cm.model_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, modelId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(extractCarModelFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding car model by ID: " + e.getMessage());
        }
        return Optional.empty();
    }


    public List<CarModel> findByMakeId(int makeId) {
        List<CarModel> carModels = new ArrayList<>();
        String sql = "SELECT cm.*, cmk.make_name " +
                "FROM car_models cm " +
                "JOIN car_makes cmk ON cm.make_id = cmk.make_id " +
                "WHERE cm.make_id = ? " +
                "ORDER BY cm.model_name ASC";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, makeId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                carModels.add(extractCarModelFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding car models by make ID: " + e.getMessage());
        }
        return carModels;
    }


    public Optional<CarModel> findByMakeIdAndModelName(int makeId, String modelName) {
        String sql = "SELECT cm.*, cmk.make_name " +
                "FROM car_models cm " +
                "JOIN car_makes cmk ON cm.make_id = cmk.make_id " +
                "WHERE cm.make_id = ? AND cm.model_name = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, makeId);
            pstmt.setString(2, modelName);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(extractCarModelFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding car model by make ID and name: " + e.getMessage());
        }
        return Optional.empty();
    }


    public List<CarModel> findAll() {
        List<CarModel> carModels = new ArrayList<>();
        String sql = "SELECT cm.*, cmk.make_name " +
                "FROM car_models cm " +
                "JOIN car_makes cmk ON cm.make_id = cmk.make_id " +
                "ORDER BY cmk.make_name ASC, cm.model_name ASC";

        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                carModels.add(extractCarModelFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all car models: " + e.getMessage());
        }
        return carModels;
    }


    public List<CarModel> searchByName(String searchTerm) {
        List<CarModel> carModels = new ArrayList<>();
        String sql = "SELECT cm.*, cmk.make_name " +
                "FROM car_models cm " +
                "JOIN car_makes cmk ON cm.make_id = cmk.make_id " +
                "WHERE cm.model_name ILIKE ? " +
                "ORDER BY cmk.make_name ASC, cm.model_name ASC";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + searchTerm + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                carModels.add(extractCarModelFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error searching car models: " + e.getMessage());
        }
        return carModels;
    }


    public List<CarModel> searchByMakeAndModelName(String makeSearch, String modelSearch) {
        List<CarModel> carModels = new ArrayList<>();
        String sql = "SELECT cm.*, cmk.make_name " +
                "FROM car_models cm " +
                "JOIN car_makes cmk ON cm.make_id = cmk.make_id " +
                "WHERE cmk.make_name ILIKE ? AND cm.model_name ILIKE ? " +
                "ORDER BY cmk.make_name ASC, cm.model_name ASC";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + makeSearch + "%");
            pstmt.setString(2, "%" + modelSearch + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                carModels.add(extractCarModelFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error searching car models by make and model: " + e.getMessage());
        }
        return carModels;
    }


    public boolean update(CarModel carModel) {
        String sql = "UPDATE car_models SET make_id = ?, model_name = ? WHERE model_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, carModel.getMakeId());
            pstmt.setString(2, carModel.getModelName());
            pstmt.setInt(3, carModel.getModelId());
            pstmt.setString(4,carModel.getBodyType());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating car model: " + e.getMessage());
        }
        return false;
    }


    public boolean delete(int modelId) {
        String sql = "DELETE FROM car_models WHERE model_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, modelId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting car model: " + e.getMessage());
        }
        return false;
    }


    public boolean exists(int modelId) {
        String sql = "SELECT COUNT(*) FROM car_models WHERE model_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, modelId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking car model existence: " + e.getMessage());
        }
        return false;
    }


    public int countByMakeId(int makeId) {
        String sql = "SELECT COUNT(*) FROM car_models WHERE make_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, makeId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting models by make: " + e.getMessage());
        }
        return 0;
    }


    public int count() {
        String sql = "SELECT COUNT(*) FROM car_models";

        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting car models: " + e.getMessage());
        }
        return 0;
    }


    private CarModel extractCarModelFromResultSet(ResultSet rs) throws SQLException {
        CarModel carModel = new CarModel();
        carModel.setModelId(rs.getInt("model_id"));
        carModel.setMakeId(rs.getInt("make_id"));
        carModel.setModelName(rs.getString("model_name"));
        carModel.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        carModel.setMakeName(rs.getString("make_name"));
        carModel.setBodyType(rs.getString("body_type"));
        return carModel;
    }

    public List <CarModel> findByBodyType(String bodyType){
        List<CarModel>carModels=new ArrayList<>();
        String sql="SELECT cm.*, cmk.make_name " +
                "FROM car_models cm " +
                "JOIN car_makes cmk ON cm.make_id = cmk.make_id " +
                "WHERE cm.body_type = ? " +
                "ORDER BY cmk.make_name ASC, cm.model_name ASC";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, bodyType);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                carModels.add(extractCarModelFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding models by body type: " + e.getMessage());
        }
        return carModels;
    }
}