package com.syla.models;

import com.google.firebase.firestore.PropertyName;

public class Users {
    private double lat;
    private double lng;
    private String name;
    private String userId;
    private boolean isActive;
    private boolean isRemoved;
    private boolean isDeleted;
    private boolean isIgnore;

    @PropertyName(value="isIgnore")
    public boolean isIgnore() {
        return isIgnore;
    }

    @PropertyName(value="isIgnore")
    public void setIgnore(boolean ignore) {
        isIgnore = ignore;
    }

    @PropertyName(value="isDeleted")
    public boolean isDeleted() {
        return isDeleted;
    }

    @PropertyName(value="isDeleted")
    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    private boolean isSaved;
    private boolean isAdmin;

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Users(){}

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @PropertyName(value="isActive")
    public boolean isActive() {
        return isActive;
    }

    @PropertyName(value="isActive")
    public void setActive(boolean active) {
        isActive = active;
    }

    @PropertyName(value="isRemoved")
    public boolean isRemoved() {
        return isRemoved;
    }

    @PropertyName(value="isRemoved")
    public void setRemoved(boolean removed) {
        isRemoved = removed;
    }

    @PropertyName(value="isSaved")
    public boolean isSaved() {
        return isSaved;
    }

    @PropertyName(value="isSaved")
    public void setSaved(boolean saved) {
        isSaved = saved;
    }
}
