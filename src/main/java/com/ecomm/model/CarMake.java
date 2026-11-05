package com.ecomm.model;

import java.time.LocalDateTime;

public class CarMake {
    private int makeId;
    private String makeName;
    private LocalDateTime createdAt;

    public CarMake() {
    }

    public CarMake(String makeName) {
        this.makeName = makeName;
    }

    public CarMake(int makeId, String makeName, LocalDateTime createdAt) {
        this.makeId = makeId;
        this.makeName = makeName;
        this.createdAt = createdAt;
    }

    public int getMakeId() {
        return makeId;
    }

    public void setMakeId(int makeId) {
        this.makeId = makeId;
    }

    public String getMakeName() {
        return makeName;
    }

    public void setMakeName(String makeName) {
        this.makeName = makeName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return makeName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CarMake carMake = (CarMake) o;
        return makeId == carMake.makeId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(makeId);
    }
}