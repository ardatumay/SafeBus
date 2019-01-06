package com.bros.safebus.safebus.models;

public class parentChild {

    private String name;
    private String key;
    private boolean notify;
    private boolean notifyHome;
    private boolean notifySchool;



    public parentChild() {
    }
     public parentChild(String fullName, String key, boolean notify, boolean notifyHome, boolean notifySchool) {// for register page. only includes register page variables
        this.name = fullName;
        this.key = key;
        this.notify = notify;
        this.notifyHome = notifyHome;
        this.notifySchool = notifySchool;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isNotify() {
        return notify;
    }

    public void setNotify(boolean notify) {
        this.notify = notify;
    }

    public boolean isNotifyHome() {
        return notifyHome;
    }

    public void setNotifyHome(boolean notifyHome) {
        this.notifyHome = notifyHome;
    }

    public boolean isNotifySchool() {
        return notifySchool;
    }

    public void setNotifySchool(boolean notifySchool) {
        this.notifySchool = notifySchool;
    }
}
