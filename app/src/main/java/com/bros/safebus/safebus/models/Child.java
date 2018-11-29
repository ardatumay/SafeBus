package com.bros.safebus.safebus.models;

public class Child {

    private String password;
    private String name;
    private String surname;
    private String schoolAddress;
    private long phone;
    private String parentKey;
    private String email;
    private String busPlate;
    private String key;
    private String currentLocation;
    private String lastKnownLocation;//tek bi string olarak mı yoksa ikili hashmap tarzında mı. virgülle ayrılıyor
    private String type;

    public Child() {
    }

    //full constructor
    public Child(String name, String surname, String schoolAddress, long phone, String parentKey, String email, String busPlate, String key, String currentLocation, String lastKnownLocation) {
        this.name = name;
        this.surname = surname;
        this.schoolAddress = schoolAddress;
        this.phone = phone;
        this.parentKey = parentKey;
        this.email = email;
        this.busPlate = busPlate;
        this.key = key;
        this.currentLocation = currentLocation;
        this.lastKnownLocation = lastKnownLocation;
    }

    public Child(String name, String surname, String email, String password, String schoolAddress, long phone, String key, String parentKey, String type) {// for register page. only includes register page variables
        this.name = name;
        this.surname = surname;
        this.schoolAddress = schoolAddress;
        this.phone = phone;
        this.email = email;
        this.password = password;
        this.key = key;
        this.type = type;
        this.parentKey = parentKey;
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

    public long getPhone() {
        return phone;
    }

    public void setPhone(long phone) {
        this.phone = phone;
    }

    public String getParentKey() {
        return parentKey;
    }

    public void setParentKey(String parentKey) {
        this.parentKey = parentKey;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBusPlate() {
        return busPlate;
    }

    public void setBusPlate(String busPlate) {
        this.busPlate = busPlate;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    @Override
    public String toString() {
        return "Child{" +
                "password='" + password + '\'' +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", schoolAddress='" + schoolAddress + '\'' +
                ", phone=" + phone +
                ", parentKey='" + parentKey + '\'' +
                ", email='" + email + '\'' +
                ", busPlate='" + busPlate + '\'' +
                ", key='" + key + '\'' +
                ", currentLocation='" + currentLocation + '\'' +
                ", lastKnownLocation='" + lastKnownLocation + '\'' +
                ", type='" + type + '\'' +
                '}';
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
}