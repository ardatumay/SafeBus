package com.bros.safebus.safebus;

import android.support.v7.app.AppCompatActivity;
import android.text.Editable;

public class Parent {
    private String name;
    private String surname;
    private String email;
    private String password;
    private String Address;
    private int phone ;

    public Parent ()
    {

    }
    public Parent (String name, String surname, String email, String password, String Address, int phone)
    {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.password = password;
        this.Address = Address;
        this.phone = phone;
    }

    public String getName ()
    {
        return name;
    }

    public void SetName (String name)
    {
        this.name = name;
    }

    public String getSurname ()
    {
        return surname;
    }
    public void setSurname()
    {
        this.surname = surname;
    }

    public String getEmail ()
    {
        return email;
    }
    public void setEmail ()
    {
        this.email = email;
    }

    public String getPassword ()
    {
        return password;
    }
    public void setPassword()
    {
        this.password = password;
    }

    public String getAddress ()
    {
        return Address;
    }
    public void setAddress()
    {
        this.Address = Address;
    }

    public int getPhone ()
    {
        return phone;
    }
    public void setPhone()
    {
        this.phone = phone;
    }

}

