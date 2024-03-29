/******************************************************************************
 *  Class Name: Driver
 *  Author: Efe
 *
 * This is the model of Driver
 *
 ******************************************************************************/

package com.bros.safebus.safebus.models;

import com.bros.safebus.safebus.models.Child;

public class Driver {

    /******************************************************************************
     * Defining the variables that we need for the driver account
     * Author: Efe
     ******************************************************************************/
    private String name;
    private String surname;
    private String schoolAddress;
    private long phoneNumber;
    private String email;
    private String password;
    private String plateNumber;
    private String currentLocation;
    private String lastKnownLocation;
    private String key;
    private Child[] children;
    private String type;
    private boolean trackLocation;

    public Driver() {
    }

    /******************************************************************************
     * Full constructor of Driver
     * Author: Efe
     ******************************************************************************/
    public Driver(String name, String surname, String email, String password, String schoolAddress, long phoneNumber, String plateNumber, String key,String type, boolean trackLocation ) {
        this.name = name;
        this.surname = surname;
        this.schoolAddress = schoolAddress;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.password = password;
        this.plateNumber = plateNumber;
        this.key = key;
        this.type = type;
        this.trackLocation = trackLocation;
    }

    /******************************************************************************
     * Different constructor of driver
     * Author: Efe
     ******************************************************************************/
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


    /******************************************************************************
     * Getters and Setters for the variables
     * Author: Efe
     ******************************************************************************/
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

    public long getPhoneNumber() {
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

    public boolean isTrackLocation() {
        return trackLocation;
    }

    public void setTrackLocation(boolean trackLocation) {
        this.trackLocation = trackLocation;
    }
}
