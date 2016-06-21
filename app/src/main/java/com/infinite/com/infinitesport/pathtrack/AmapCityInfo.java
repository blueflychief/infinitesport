package com.infinite.com.infinitesport.pathtrack;

/**
 * Created by Administrator on 2015/12/9.
 */
public class AmapCityInfo {
    private String city;
    private String province;
    private String address;
    private String city_code;
    private double geoLat;
    private double geoLng;
    private long time_stamp;
    private String time;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public long getTime_stamp() {
        return time_stamp;
    }

    public void setTime_stamp(long time_stamp) {
        this.time_stamp = time_stamp;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity_code() {
        return city_code;
    }

    public void setCity_code(String city_code) {
        this.city_code = city_code;
    }

    public double getGeoLat() {
        return geoLat;
    }

    public void setGeoLat(double geoLat) {
        this.geoLat = geoLat;
    }

    public double getGeoLng() {
        return geoLng;
    }

    public void setGeoLng(double geoLng) {
        this.geoLng = geoLng;
    }


    @Override
    public String toString() {
        return "AmapCityInfo{" +
                "city='" + city + '\'' +
                "\nprovince='" + province + '\'' +
                "\naddress='" + address + '\'' +
                "\ncity_code='" + city_code + '\'' +
                "\ngeoLat=" + geoLat +
                "\ngeoLng=" + geoLng +
                "\ntime_stamp=" + time_stamp +
                "\ntime='" + time + '\'' +
                '}';
    }
}
