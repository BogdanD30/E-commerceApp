package com.ecomm.model;

import java.time.LocalDateTime;


public class CarModel {
    private int modelId;
    private int makeId;
    private String modelName;
    private LocalDateTime createdAt;

    private String makeName;

     private String bodyType; // SEDAN, SUV, COUPE, CONVERTIBLE, WAGON, HATCHBACK, TRUCK

   public CarModel() {
    }

    public CarModel(int makeId, String modelName) {
        this.makeId = makeId;
        this.modelName = modelName;
    }

    public CarModel(int modelId, int makeId, String modelName, LocalDateTime createdAt, String bodyType) {
        this.modelId = modelId;
        this.makeId = makeId;
        this.modelName = modelName;
        this.createdAt = createdAt;
        this.bodyType=bodyType;
    }

    public int getModelId() {
        return modelId;
    }

    public void setModelId(int modelId) {
        this.modelId = modelId;
    }

    public int getMakeId() {
        return makeId;
    }

    public void setMakeId(int makeId) {
        this.makeId = makeId;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getMakeName() {
        return makeName;
    }

    public void setMakeName(String makeName) {
        this.makeName = makeName;
    }
    public String getBodyType() {
        return bodyType;
    }
    public void setBodyType(String bodyType) {
        this.bodyType = bodyType;
    }

    public String getFullName() {
        return (makeName != null ? makeName + " " : "") + modelName;
    }

    @Override
    public String toString() {
        return modelName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CarModel carModel = (CarModel) o;
        return modelId == carModel.modelId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(modelId);
    }
}