/*
Name: Mayur Gunputh
Date: 11 Dec 2018
Project: G53MDP Coursework 2
RunLog.java
Class storing log details: date, distance and time
 */

package com.example.mayur.runningtracker;

public class RunLog {
    private String dateTime;
    private String distance;
    private long time;

    public RunLog() {
    }

    public RunLog(String dt, String dis, long t) {
        this.dateTime = dt;
        this.distance = dis;
        this.time = t;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}