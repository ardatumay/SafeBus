package com.bros.safebus.safebus;

import java.util.HashMap;

public class Driver {
    private String name;
    private String surname;
    private String schoolAddress;
    private int phoneNumber;
    private String email;
    private String password;
    private String plateNumber;
    private String currentLocation;
    private String lastKnownLocation;//tek bi string olarak mı yoksa ikili hashmap tarzında mı. virgülle ayrılıyor
    private String key;
    private Child[] children;
    private String type;

    public Driver() {
    }

    public Driver(String name, String surname, String email, String password, String schoolAddress, int phoneNumber, String plateNumber, String key,String type) {
        this.name = name;
        this.surname = surname;
        this.schoolAddress = schoolAddress;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.password = password;
        this.plateNumber = plateNumber;
        this.key = key;
        this.type = type;
    }

    public Driver(String name, String surname, String schoolAddress, int phoneNumber, String email, String plateNumber, String currentLocation, String lastKnownLocation,String type) {
        this.name = name;
        this.surname = surname;
        this.schoolAddress = schoolAddress;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.plateNumber = plateNumber;
        this.currentLocation = currentLocation;
        this.lastKnownLocation = lastKnownLocation;
        this.type = type;
    }



    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Child[] getChildren() {
        return children;
    }

    public void setChildren(Child[] children) {
        this.children = children;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
    }

    public String getLastKnownLocation() {
        return lastKnownLocation;
    }

    public void setLastKnownLocation(String lastKnownLocation) {
        this.lastKnownLocation = lastKnownLocation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getSchoolAddress() {
        return schoolAddress;
    }

    public void setSchoolAddress(String schoolAddress) {
        this.schoolAddress = schoolAddress;
    }

    public int getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(int phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
