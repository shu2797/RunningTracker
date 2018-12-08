package com.example.mayur.runningtracker;

public class RunLog {
    private String dateTime;
    private String distance;
    private String time;

    public RunLog(){

    }

    public RunLog(String dt, String dis, String t){
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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}