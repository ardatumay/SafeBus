package com.bros.safebus.safebus;

import android.support.v7.app.AppCompatActivity;
import android.text.Editable;

import java.util.Arrays;

public class Parent {
    private String name;
    private String surname;
    private String email;
    private String password;
    private String Address;
    private long phone;
    private String key;
    private Child[] children;
    private String type;

    public Parent ()
    {

    }
    public Parent (String name, String surname, String email, String password, String Address, long phone, String key, String type)//constructor for only register page and includes only register page variables
    {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.password = password;
        this.Address = Address;
        this.phone = phone;
        this.key = key;
        this.type = type;
    }

    //full constructor
    public Parent(String name, String surname, String email, String password, String address, long phone, String key, Child[] children, String type) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.password = password;
        Address = address;
        this.phone = phone;
        this.key = key;
        this.children = children;
        this.type = type;
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
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
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
                ", Address='" + Address + '\'' +
                ", phone=" + phone +
                ", key='" + key + '\'' +
                ", children=" + Arrays.toString(children) +
                '}';
    }
}

