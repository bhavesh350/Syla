package com.syla.models;

import java.util.List;

public class Rooms {

    private long roomCreateTime;
    private String roomName;
    private String userName;
    private int count;
    private String roomId;
    private double lat;
    private double lng;

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

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public Rooms() {
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    private List<Users> users;

    public long getRoomCreateTime() {
        return roomCreateTime;
    }

    public void setRoomCreateTime(long roomCreateTime) {
        this.roomCreateTime = roomCreateTime;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public List<Users> getUsers() {
        return users;
    }

    public void setUsers(List<Users> users) {
        this.users = users;
    }

    @Override
    public String toString() {
        return getRoomName() + "\n" + getUserName() + "\n";
    }
}
