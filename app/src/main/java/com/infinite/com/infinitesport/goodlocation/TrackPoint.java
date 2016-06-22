package com.infinite.com.infinitesport.goodlocation;

import java.io.Serializable;

/**
 * Created by Lsq on 6/22/2016.--3:07 PM
 */
public class TrackPoint implements Serializable {
    private long time;
    private double Lat;
    private double Lng;
    private float accuracy;
    private double altitude;
    private double bearing;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getLat() {
        return Lat;
    }

    public void setLat(double lat) {
        Lat = lat;
    }

    public double getLng() {
        return Lng;
    }

    public void setLng(double lng) {
        Lng = lng;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public double getBearing() {
        return bearing;
    }

    public void setBearing(double bearing) {
        this.bearing = bearing;
    }

    @Override
    public String toString() {
        return "TrackPoint{" +
                "time=" + time +
                ", Lat=" + Lat +
                ", Lng=" + Lng +
                ", accuracy=" + accuracy +
                ", altitude=" + altitude +
                ", bearing=" + bearing +
                '}';
    }
}
