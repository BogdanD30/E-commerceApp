package com.ecomm.repository;

import com.ecomm.model.CarListing;
import com.ecomm.util.DatabaseUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CarListingRepository {

    /** Every read query selects the same columns so extractListingFromResultSet works for all of them. */
    private static final String BASE_SELECT =
            "SELECT cl.*, cm.model_name, cm.body_type, cmk.make_name, u.email " +
                    "FROM car_listings cl " +
                    "JOIN car_models cm ON cl.model_id = cm.model_id " +
                    "JOIN car_makes cmk ON cm.make_id = cmk.make_id " +
                    "JOIN users u ON cl.user_id = u.user_id ";

    @FunctionalInterface
    private interface ParamBinder {
        void bind(PreparedStatement pstmt) throws SQLException;
    }

    public boolean create(CarListing listing) {
        String sql = "INSERT INTO car_listings (user_id, model_id, title, description, price, year, mileage, " +
                "fuel_type, transmission, color, location) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, listing.getUserId());
            pstmt.setInt(2, listing.getModelId());
            pstmt.setString(3, listing.getTitle());
            pstmt.setString(4, listing.getDescription());
            pstmt.setBigDecimal(5, listing.getPrice());
            pstmt.setInt(6, listing.getYear());
            pstmt.setObject(7, listing.getMileage());
            pstmt.setString(8, listing.getFuelType());
            pstmt.setString(9, listing.getTransmission());
            pstmt.setString(10, listing.getColor());
            pstmt.setString(11, listing.getLocation());

            if (pstmt.executeUpdate() > 0) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        listing.setListingId(keys.getInt("listing_id"));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error creating listing: " + e.getMessage());
        }
        return false;
    }

    public Optional<CarListing> findById(int listingId) {
        List<CarListing> result = query(BASE_SELECT + "WHERE cl.listing_id = ?", p -> p.setInt(1, listingId));
        return result.stream().findFirst();
    }

    public List<CarListing> findAllActive() {
        return query(BASE_SELECT + "WHERE cl.is_active = true ORDER BY cl.created_at DESC", p -> { });
    }

    public List<CarListing> findAll() {
        return query(BASE_SELECT + "ORDER BY cl.created_at DESC", p -> { });
    }

    public List<CarListing> findByUserId(int userId) {
        return query(BASE_SELECT + "WHERE cl.user_id = ? ORDER BY cl.created_at DESC", p -> p.setInt(1, userId));
    }

    public List<CarListing> findActiveByUserId(int userId) {
        return query(BASE_SELECT + "WHERE cl.user_id = ? AND cl.is_active = true ORDER BY cl.created_at DESC",
                p -> p.setInt(1, userId));
    }

    public List<CarListing> findByModelId(int modelId) {
        return query(BASE_SELECT + "WHERE cl.model_id = ? AND cl.is_active = true ORDER BY cl.created_at DESC",
                p -> p.setInt(1, modelId));
    }

    public List<CarListing> searchByKeyword(String keyword) {
        String pattern = "%" + keyword + "%";
        return query(BASE_SELECT + "WHERE cl.is_active = true AND (cl.title ILIKE ? OR cl.description ILIKE ?) " +
                "ORDER BY cl.created_at DESC", p -> {
            p.setString(1, pattern);
            p.setString(2, pattern);
        });
    }

    public List<CarListing> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return query(BASE_SELECT + "WHERE cl.is_active = true AND cl.price BETWEEN ? AND ? ORDER BY cl.price ASC", p -> {
            p.setBigDecimal(1, minPrice);
            p.setBigDecimal(2, maxPrice);
        });
    }

    public List<CarListing> findRecent(int limit) {
        return query(BASE_SELECT + "WHERE cl.is_active = true ORDER BY cl.created_at DESC LIMIT ?",
                p -> p.setInt(1, limit));
    }

    public List<CarListing> findAllWithPagination(int page, int pageSize) {
        return query(BASE_SELECT + "WHERE cl.is_active = true ORDER BY cl.created_at DESC LIMIT ? OFFSET ?", p -> {
            p.setInt(1, pageSize);
            p.setInt(2, (page - 1) * pageSize);
        });
    }

    /** Active listings matching every filter that is non-null/non-empty. */
    public List<CarListing> searchWithFilters(String keyword, String makeSearch, String modelSearch,
                                              Integer minYear, Integer maxYear,
                                              BigDecimal minPrice, BigDecimal maxPrice,
                                              String fuelType, String transmission,
                                              Integer maxMileage, String location, String bodyType) {
        StringBuilder sql = new StringBuilder(BASE_SELECT + "WHERE cl.is_active = true");
        List<Object> params = new ArrayList<>();

        if (notEmpty(keyword)) {
            sql.append(" AND (cl.title ILIKE ? OR cl.description ILIKE ?)");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }
        if (notEmpty(makeSearch)) {
            sql.append(" AND cmk.make_name ILIKE ?");
            params.add("%" + makeSearch + "%");
        }
        if (notEmpty(modelSearch)) {
            sql.append(" AND cm.model_name ILIKE ?");
            params.add("%" + modelSearch + "%");
        }
        if (minYear != null) {
            sql.append(" AND cl.year >= ?");
            params.add(minYear);
        }
        if (maxYear != null) {
            sql.append(" AND cl.year <= ?");
            params.add(maxYear);
        }
        if (minPrice != null) {
            sql.append(" AND cl.price >= ?");
            params.add(minPrice);
        }
        if (maxPrice != null) {
            sql.append(" AND cl.price <= ?");
            params.add(maxPrice);
        }
        if (notEmpty(fuelType)) {
            sql.append(" AND cl.fuel_type = ?");
            params.add(fuelType);
        }
        if (notEmpty(transmission)) {
            sql.append(" AND cl.transmission = ?");
            params.add(transmission);
        }
        if (maxMileage != null) {
            sql.append(" AND cl.mileage <= ?");
            params.add(maxMileage);
        }
        if (notEmpty(location)) {
            sql.append(" AND cl.location ILIKE ?");
            params.add("%" + location + "%");
        }
        if (notEmpty(bodyType)) {
            sql.append(" AND cm.body_type = ?");
            params.add(bodyType);
        }
        sql.append(" ORDER BY cl.created_at DESC");

        return query(sql.toString(), p -> {
            for (int i = 0; i < params.size(); i++) {
                p.setObject(i + 1, params.get(i));
            }
        });
    }

    public boolean update(CarListing listing) {
        String sql = "UPDATE car_listings SET model_id = ?, title = ?, description = ?, price = ?, year = ?, " +
                "mileage = ?, fuel_type = ?, transmission = ?, color = ?, location = ?, " +
                "updated_at = CURRENT_TIMESTAMP WHERE listing_id = ?";
        return update(sql, p -> {
            p.setInt(1, listing.getModelId());
            p.setString(2, listing.getTitle());
            p.setString(3, listing.getDescription());
            p.setBigDecimal(4, listing.getPrice());
            p.setInt(5, listing.getYear());
            p.setObject(6, listing.getMileage());
            p.setString(7, listing.getFuelType());
            p.setString(8, listing.getTransmission());
            p.setString(9, listing.getColor());
            p.setString(10, listing.getLocation());
            p.setInt(11, listing.getListingId());
        });
    }

    public boolean deactivate(int listingId) {
        return update("UPDATE car_listings SET is_active = false, updated_at = CURRENT_TIMESTAMP WHERE listing_id = ?",
                p -> p.setInt(1, listingId));
    }

    public boolean activate(int listingId) {
        return update("UPDATE car_listings SET is_active = true, updated_at = CURRENT_TIMESTAMP WHERE listing_id = ?",
                p -> p.setInt(1, listingId));
    }

    public boolean delete(int listingId) {
        return update("DELETE FROM car_listings WHERE listing_id = ?", p -> p.setInt(1, listingId));
    }

    public boolean exists(int listingId) {
        return count("SELECT COUNT(*) FROM car_listings WHERE listing_id = ?", p -> p.setInt(1, listingId)) > 0;
    }

    public int countActive() {
        return count("SELECT COUNT(*) FROM car_listings WHERE is_active = true", p -> { });
    }

    public int countByUserId(int userId) {
        return count("SELECT COUNT(*) FROM car_listings WHERE user_id = ?", p -> p.setInt(1, userId));
    }

    private static boolean notEmpty(String s) {
        return s != null && !s.isBlank();
    }

    private List<CarListing> query(String sql, ParamBinder binder) {
        List<CarListing> listings = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            binder.bind(pstmt);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    listings.add(extractListingFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error running listing query: " + e.getMessage());
        }
        return listings;
    }

    private boolean update(String sql, ParamBinder binder) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            binder.bind(pstmt);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating listing data: " + e.getMessage());
        }
        return false;
    }

    private int count(String sql, ParamBinder binder) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            binder.bind(pstmt);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            System.err.println("Error counting listings: " + e.getMessage());
        }
        return 0;
    }

    private CarListing extractListingFromResultSet(ResultSet rs) throws SQLException {
        CarListing listing = new CarListing();
        listing.setListingId(rs.getInt("listing_id"));
        listing.setUserId(rs.getInt("user_id"));
        listing.setModelId(rs.getInt("model_id"));
        listing.setTitle(rs.getString("title"));
        listing.setDescription(rs.getString("description"));
        listing.setPrice(rs.getBigDecimal("price"));
        listing.setYear(rs.getInt("year"));
        listing.setMileage((Integer) rs.getObject("mileage"));
        listing.setFuelType(rs.getString("fuel_type"));
        listing.setTransmission(rs.getString("transmission"));
        listing.setColor(rs.getString("color"));
        listing.setLocation(rs.getString("location"));
        listing.setActive(rs.getBoolean("is_active"));
        listing.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        listing.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        listing.setModelName(rs.getString("model_name"));
        listing.setMakeName(rs.getString("make_name"));
        listing.setSellerEmail(rs.getString("email"));
        listing.setBodyType(rs.getString("body_type"));
        return listing;
    }
}
