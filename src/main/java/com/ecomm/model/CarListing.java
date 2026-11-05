package com.ecomm.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CarListing {
    private int listingId;
    private int userId;
    private int modelId;
    private String title;
    private String description;
    private BigDecimal price;
    private int year;
    private Integer mileage;
    private String fuelType;
    private String transmission;
    private String color;
    private String location;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String bodyType;
    private String makeName;
    private String modelName;
    private String sellerEmail;

    public CarListing() {
    }

    public CarListing(int userId, int modelId, String title, String description,
                      BigDecimal price, int year) {
        this.userId = userId;
        this.modelId = modelId;
        this.title = title;
        this.description = description;
        this.price = price;
        this.year = year;
        this.isActive = true;
    }

    public int getListingId() {
        return listingId;
    }

    public void setListingId(int listingId) {
        this.listingId = listingId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getModelId() {
        return modelId;
    }

    public void setModelId(int modelId) {
        this.modelId = modelId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Integer getMileage() {
        return mileage;
    }

    public void setMileage(Integer mileage) {
        this.mileage = mileage;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    public String getTransmission() {
        return transmission;
    }

    public void setTransmission(String transmission) {
        this.transmission = transmission;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getMakeName() {
        return makeName;
    }

    public void setMakeName(String makeName) {
        this.makeName = makeName;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getSellerEmail() {
        return sellerEmail;
    }

    public void setSellerEmail(String sellerEmail) {
        this.sellerEmail = sellerEmail;
    }
    public void setBodyType(String bodyType) {
        this.bodyType = bodyType;
    }
    public String getBodyType() {
        return bodyType;
    }

    public String getFormattedPrice() {
        return "€" + String.format("%,.2f", price);
    }

    public String getFullCarName() {
        return year + " " + (makeName != null ? makeName + " " : "") +
                (modelName != null ? modelName : "");
    }

    public String getFormattedMileage() {
        return mileage != null ? String.format("%,d km", mileage) : "N/A";
    }

    public boolean isNew() {
        if (createdAt == null) return false;
        return createdAt.isAfter(LocalDateTime.now().minusDays(7));
    }

    @Override
    public String toString() {
        return "CarListing{" +
                "listingId=" + listingId +
                ", title='" + title + '\'' +
                ", price=" + price +
                ", year=" + year +
                ", makeName='" + makeName + '\'' +
                ", modelName='" + modelName + '\'' +
                ", isActive=" + isActive +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CarListing that = (CarListing) o;
        return listingId == that.listingId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(listingId);
    }
}