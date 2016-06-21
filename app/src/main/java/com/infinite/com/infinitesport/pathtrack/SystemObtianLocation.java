package com.infinite.com.infinitesport.pathtrack;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;

import com.infinite.com.infinitesport.MyApplication;
import com.infinite.com.infinitesport.util.MyLogger;

import java.text.SimpleDateFormat;
import java.util.Iterator;

/**
 * Created by Lsq on 6/21/2016.--10:24 AM
 */
public class SystemObtianLocation {
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private LocationManager mLocationManager;
    private Criteria mCriteria;

    public SystemObtianLocation() {
    }

    private static class SystemObtianLocationHolder {
        private static final SystemObtianLocation INSTANCE = new SystemObtianLocation();
    }

    public static SystemObtianLocation getInstance() {
        return SystemObtianLocationHolder.INSTANCE;
    }


    public void initLocation() {
        mLocationManager = (LocationManager) MyApplication.getInstance().getSystemService(Context.LOCATION_SERVICE);
        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // 返回开启GPS导航设置界面
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            MyApplication.getInstance().startActivity(intent);
            return;
        }
        setCriteria();
        if (ActivityCompat.checkSelfPermission(MyApplication.getInstance(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MyApplication.getInstance(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // 监听状态
        mLocationManager.addGpsStatusListener(mStatusListener);
// 绑定监听，有4个参数
// 参数1，设备：有GPS_PROVIDER和NETWORK_PROVIDER两种
// 参数2，位置信息更新周期，单位毫秒
// 参数3，位置变化最小距离：当位置距离变化超过此值时，将更新位置信息
// 参数4，监听
// 备注：参数2和3，如果参数3不为0，则以参数3为准；参数3为0，则通过时间来定时更新；两者为0，则随时刷新
// 1秒更新一次，或最小位移变化超过1米更新一次；
// 注意：此处更新准确度非常低，推荐在service里面启动一个Thread，在run中sleep(10000);然后执行handler.sendMessage(),更新位置
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 50, mLocationListener);

    }


    // 状态监听
    private GpsStatus.Listener mStatusListener = new GpsStatus.Listener() {
        public void onGpsStatusChanged(int event) {
            switch (event) {
                // 第一次定位
                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    MyLogger.i("--------第一次定位");
                    break;
                // 卫星状态改变
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    MyLogger.i("---------卫星状态改变");
                    // 获取当前状态
                    GpsStatus gpsStatus = mLocationManager.getGpsStatus(null);
                    // 获取卫星颗数的默认最大值
                    int maxSatellites = gpsStatus.getMaxSatellites();
                    // 创建一个迭代器保存所有卫星
                    Iterator<GpsSatellite> iters = gpsStatus.getSatellites().iterator();
                    int count = 0;
                    while (iters.hasNext() && count <= maxSatellites) {
                        GpsSatellite s = iters.next();
//                        MyLogger.i("------%%%%%%%%%%%卫星信息%%%%%%%%%%%------");
//                        MyLogger.i("--------getAzimuth------" + s.getAzimuth());
//                        MyLogger.i("--------getElevation------" + s.getElevation());
//                        MyLogger.i("--------getPrn------" + s.getPrn());
//                        MyLogger.i("--------getSnr------" + s.getSnr());
//                        MyLogger.i("------%%%%%%%%%%%卫星信息%%%%%%%%%%%------");
                        count++;
                    }
                    MyLogger.i("---------搜索到：" + count + "颗卫星");


                    // 为获取地理位置信息时设置查询条件
                    String bestProvider =  mLocationManager.getBestProvider(mCriteria, true);
                    // 获取位置信息
                    // 如果不设置查询要求，getLastKnownLocation方法传人的参数为LocationManager.GPS_PROVIDER
                    if (ActivityCompat.checkSelfPermission(MyApplication.getInstance(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(MyApplication.getInstance(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    Location location = mLocationManager.getLastKnownLocation(bestProvider);
                    printLocationInfo(location);
                    break;
                // 定位启动
                case GpsStatus.GPS_EVENT_STARTED:
                    MyLogger.i("--------定位启动");
                    break;
                // 定位结束
                case GpsStatus.GPS_EVENT_STOPPED:
                    MyLogger.i("---------定位结束");
                    break;
            }
        }
    };


    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            printLocationInfo(location);
        }

        //GPS状态改变
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                // GPS状态为可见时
                case LocationProvider.AVAILABLE:
                    MyLogger.i("------当前GPS状态为可见状态");
                    break;
                // GPS状态为服务区外时
                case LocationProvider.OUT_OF_SERVICE:
                    MyLogger.i("-------当前GPS状态为服务区外状态");
                    break;
                // GPS状态为暂停服务时
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    MyLogger.i("--------当前GPS状态为暂停服务状态");
                    break;
            }
        }

        //GPS启用时
        @Override
        public void onProviderEnabled(String provider) {
            MyLogger.i("---------GPS启动");
            getLocationInfo(provider);
        }

        //GPS禁用时
        @Override
        public void onProviderDisabled(String provider) {
            MyLogger.i("---------GPS禁用");
            getLocationInfo(provider);
        }
    };

    private void getLocationInfo(String provider) {
        if (ActivityCompat.checkSelfPermission(MyApplication.getInstance(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MyApplication.getInstance(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = mLocationManager.getLastKnownLocation(provider);
        printLocationInfo(location);
    }

    private void printLocationInfo(Location location) {
        if (location!=null) {
            MyLogger.i("---------定位成功");
            MyLogger.i("---------纬度" + location.getLatitude());
            MyLogger.i("---------经度" + location.getLongitude());
            MyLogger.i("---------时间" + location.getTime());
            MyLogger.i("---------时间" + sdf.format(location.getTime()));
            MyLogger.i("---------精度" + location.getAccuracy());
            MyLogger.i("---------海拔" + location.getAltitude());
        }
    }

    private void setCriteria() {
        if (mCriteria == null) {
            mCriteria = new Criteria();
        }
        // 设置定位精确度 Criteria.ACCURACY_COARSE比较粗略，Criteria.ACCURACY_FINE则比较精细
        mCriteria.setAccuracy(Criteria.ACCURACY_FINE);
        // 设置是否要求速度
        mCriteria.setSpeedRequired(false);
        // 设置是否允许运营商收费
        mCriteria.setCostAllowed(false);
        // 设置是否需要方位信息
        mCriteria.setBearingRequired(false);
        // 设置是否需要海拔信息
        mCriteria.setAltitudeRequired(false);
        // 设置对电源的需求
        mCriteria.setPowerRequirement(Criteria.POWER_LOW);
    }
}
