/******************************************************************************
 *  Class Name: Parent
 *  Author: Efe
 *
 * This is the model of Parent
 *
 ******************************************************************************/

package com.bros.safebus.safebus.models;

import com.bros.safebus.safebus.models.Child;

import java.util.Arrays;

public class Parent {
    /******************************************************************************
     * Defining the variables that we need for the Parent account
     * Author: Efe
     ******************************************************************************/
    private String name;
    private String surname;
    private String email;
    private String password;
    private String address;
    private long phone;
    private String key;
    private Child[] children;
    private String type;

    public Parent ()
    {

    }

    /******************************************************************************
     * Another constructor of Parent for registering
     * Author: Efe
     ******************************************************************************/
    public Parent (String name, String surname, String email, String password, String address, long phone, String key, String type)
    {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.password = password;
        this.address = address;
        this.phone = phone;
        this.key = key;
        this.type = type;
    }

    /******************************************************************************
     * Full constructor of Parent
     * Author: Efe
     ******************************************************************************/
    public Parent(String name, String surname, String email, String password, String address, long phone, String key, Child[] children, String type)
    {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.password = password;
        address = address;
        this.phone = phone;
        this.key = key;
        this.children = children;
        this.type = type;
    }
    /******************************************************************************
     * Getters and Setters for the variables
     * Author: Efe
     ******************************************************************************/
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        address = address;
    }

    public long getPhone() {
        return phone;
    }

    public void setPhone(int phone) {
        this.phone = phone;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    @Override
    public String toString() {
        return "Parent{" +
                "name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", Address='" + address + '\'' +
                ", phone=" + phone +
                ", key='" + key + '\'' +
                ", children=" + Arrays.toString(children) +
                '}';
    }
}

