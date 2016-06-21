package com.infinite.com.infinitesport.goodlocation;

/**
 * Created by Administrator on 2016-06-21.
 */

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;

import com.infinite.com.infinitesport.MyApplication;
import com.infinite.com.infinitesport.util.MyLogger;

import java.text.SimpleDateFormat;
import java.util.Random;

/**
 * Wrapper for two coordinates (latitude and longitude)
 */
public class Point implements Parcelable {

    /**
     * The latitude of the point
     */
    public final double latitude;
    /**
     * The longitude of the point
     */
    public final double longitude;

    /**
     * Constructs a new point from the given coordinates
     *
     * @param lat the latitude
     * @param lon the longitude
     */
    public Point(double lat, double lon) {
        latitude = lat;
        longitude = lon;
    }

    @Override
    public String toString() {
        return "(" + latitude + ", " + longitude + ")";
    }

    public static final Creator<Point> CREATOR = new Creator<Point>() {

        @Override
        public Point createFromParcel(Parcel in) {
            return new Point(in);
        }

        @Override
        public Point[] newArray(int size) {
            return new Point[size];
        }

    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeDouble(latitude);
        out.writeDouble(longitude);
    }

    private Point(Parcel in) {
        latitude = in.readDouble();
        longitude = in.readDouble();
    }


}