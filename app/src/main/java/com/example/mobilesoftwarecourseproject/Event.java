package com.example.mobilesoftwarecourseproject;

import java.io.Serializable;

public class Event implements Serializable {

    private String text;
    private String mobileurl;

    public Event(String text, String mobileurl) {
        this.text = text;
        this.mobileurl = mobileurl;
    }

    public String getText() {
        return text;
    }

    public String getMobileurl() {
        return mobileurl;
    }


}
