package com.infinite.com.infinitesport.goodlocation;

/**
 * Created by Administrator on 2016-06-21.
 */

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

import java.util.Iterator;
import java.util.Random;

/**
 * Utility class for easy access to the device location on Android
 */
public class SimpleGpsLocation {


    private GpsStatus.Listener mGpsStatusListener;

    /**
     * Callback that can be implemented in order to listen for events
     */
    public interface PositionListener {

        /**
         * Called whenever the device's position changes so that you can call {@link SimpleGpsLocation#getPosition()}
         */
        void onPositionChanged(Location location);

    }

    /**
     * The internal name of the provider for the coarse location
     */
    private static final String PROVIDER_COARSE = LocationManager.NETWORK_PROVIDER;
    /**
     * The internal name of the provider for the fine location
     */
    private static final String PROVIDER_FINE = LocationManager.GPS_PROVIDER;
    /**
     * The internal name of the provider for the fine location in passive mode
     */
    private static final String PROVIDER_FINE_PASSIVE = LocationManager.PASSIVE_PROVIDER;
    /**
     * The default interval to receive new location updates after (in milliseconds)
     */
    private static final long INTERVAL_DEFAULT = 10 * 60 * 1000;
    /**
     * The factor for conversion from kilometers to meters
     */
    private static final float KILOMETER_TO_METER = 1000.0f;
    /**
     * The factor for conversion from latitude to kilometers
     */
    private static final float LATITUDE_TO_KILOMETER = 111.133f;
    /**
     * The factor for conversion from longitude to kilometers at zero degree in latitude
     */
    private static final float LONGITUDE_TO_KILOMETER_AT_ZERO_LATITUDE = 111.320f;
    /**
     * The PRNG that is used for location blurring
     */
    private static final Random mRandom = new Random();
    private static final double SQUARE_ROOT_TWO = Math.sqrt(2);
    /**
     * The last location that was internally cached when creating new instances in the same process
     */
    private static Location mCachedPosition;
    /**
     * The LocationManager instance used to query the device location
     */
    private final LocationManager mLocationManager;
    /**
     * Whether a fine location should be required or coarse location can be used
     */
    private final boolean mRequireFine;
    /**
     * Whether passive mode shall be used or not
     */
    private final boolean mPassive;
    /**
     * The internal after which new location updates are requested (in milliseconds) where longer intervals save battery
     */
    private final long mInterval;
    /**
     * Whether to require a new location (`true`) or accept old (last known) locations as well (`false`)
     */
    private final boolean mRequireNewLocation;
    /**
     * The blur radius (in meters) that will be used to blur the location for privacy reasons
     */
    private int mBlurRadius;
    /**
     * The LocationListener instance used internally to listen for location updates
     */
    private LocationListener mLocationListener;
    /**
     * The current location with latitude, longitude, speed and altitude
     */

    private Criteria mCriteria;

    private Location mPosition;
    private PositionListener mListener;


    public SimpleGpsLocation(final Context context) {
        this(context, false);
    }


    public SimpleGpsLocation(final Context context, final boolean requireFine) {
        this(context, requireFine, false);
    }


    public SimpleGpsLocation(final Context context, final boolean requireFine, final boolean passive) {
        this(context, requireFine, passive, INTERVAL_DEFAULT);
    }

    public SimpleGpsLocation(final Context context, final boolean requireFine, final boolean passive, final long interval) {
        this(context, requireFine, passive, interval, false);
    }


    /**
     *
     * @param context
     * @param requireFine   是否精确定位
     * @param passive       是否被动定位
     * @param interval      定位时间间隔
     * @param requireNewLocation    是否使用缓存位置
     */
    public SimpleGpsLocation(final Context context, final boolean requireFine, final boolean passive, final long interval, final boolean requireNewLocation) {
        mLocationManager = (LocationManager) context.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        mRequireFine = requireFine;
        mPassive = passive;
        mInterval = interval;
        mRequireNewLocation = requireNewLocation;
        setCriteria();
        if (!mRequireNewLocation) {
            mPosition = getCachedPosition();
            cachePosition();
        }
    }

    /**
     * Attaches or detaches a listener that informs about certain events
     *
     * @param listener the `SimpleGpsLocation.PositionListener` instance to attach or `null` to detach
     */
    public void setPositionListener(final PositionListener listener) {
        mListener = listener;
    }


    public boolean hasLocationEnabled() {
        return hasLocationEnabled(getProviderName());
    }

    private boolean hasLocationEnabled(final String providerName) {
        try {
            return mLocationManager.isProviderEnabled(providerName);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 开始更新定位
     */
    public void beginUpdates() {
        if (mLocationListener != null) {
            endUpdates();
        }

        if (!mRequireNewLocation) {
            mPosition = getCachedPosition();
        }

        mLocationListener = createLocationListener();
        mGpsStatusListener = createGpsStatusListener();
        if (ActivityCompat.checkSelfPermission(MyApplication.getInstance(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MyApplication.getInstance(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //如果mPassive为true，则addGpsStatusListener无效
        mLocationManager.addGpsStatusListener(mGpsStatusListener);
        // 绑定监听，有4个参数
// 参数1，设备：有GPS_PROVIDER和NETWORK_PROVIDER两种
// 参数2，位置信息更新周期，单位毫秒
// 参数3，位置变化最小距离：当位置距离变化超过此值时，将更新位置信息
// 参数4，监听
// 备注：参数2和3，如果参数3不为0，则以参数3为准；参数3为0，则通过时间来定时更新；两者为0，则随时刷新
// 1秒更新一次，或最小位移变化超过1米更新一次；
// 注意：此处更新准确度非常低，推荐在service里面启动一个Thread，在run中sleep(10000);然后执行handler.sendMessage(),更新位置
        mLocationManager.requestLocationUpdates(getProviderName(), mInterval, 0, mLocationListener);

        MyLogger.i("-----GPS定位开始");
    }

    /**
     * 停止监听，为了省电
     */
    public void endUpdates() {
        if (mLocationListener != null) {
            if (ActivityCompat.checkSelfPermission(MyApplication.getInstance(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MyApplication.getInstance(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mLocationManager.removeUpdates(mLocationListener);
            mLocationListener = null;
        }

        mPosition = null;
        mCachedPosition = null;

        if (mGpsStatusListener != null) {
            mLocationManager.removeGpsStatusListener(mGpsStatusListener);
            mGpsStatusListener = null;
        }

        MyLogger.i("-----GPS定位停止");
    }

    /**
     * Blurs the specified location with the defined blur radius or returns an unchanged location if no blur radius is set
     *
     * @param originalLocation the original location received from the device
     * @return the blurred location
     */
    private Location blurWithRadius(final Location originalLocation) {
        if (mBlurRadius <= 0) {
            return originalLocation;
        } else {
            Location newLocation = new Location(originalLocation);

            double blurMeterLong = calculateRandomOffset(mBlurRadius) / SQUARE_ROOT_TWO;
            double blurMeterLat = calculateRandomOffset(mBlurRadius) / SQUARE_ROOT_TWO;

            newLocation.setLongitude(newLocation.getLongitude() + meterToLongitude(blurMeterLong, newLocation.getLatitude()));
            newLocation.setLatitude(newLocation.getLatitude() + meterToLatitude(blurMeterLat));

            return newLocation;
        }
    }

    /**
     * For any radius `n`, calculate a random offset in the range `[-n, n]`
     *
     * @param radius the radius
     * @return the random offset
     */
    private static int calculateRandomOffset(final int radius) {
        return mRandom.nextInt((radius + 1) * 2) - radius;
    }

    /**
     * Returns the current position as a Point instance
     *
     * @return the current location (if any) or `null`
     */
    public Point getPosition() {
        if (mPosition == null) {
            return null;
        } else {
            Location position = blurWithRadius(mPosition);
            return new Point(position.getLatitude(), position.getLongitude());
        }
    }

    /**
     * Returns the latitude of the current location
     *
     * @return the current latitude (if any) or `0`
     */
    public double getLatitude() {
        if (mPosition == null) {
            return 0.0f;
        } else {
            Location position = blurWithRadius(mPosition);
            return position.getLatitude();
        }
    }

    /**
     * Returns the longitude of the current location
     *
     * @return the current longitude (if any) or `0`
     */
    public double getLongitude() {
        if (mPosition == null) {
            return 0.0f;
        } else {
            Location position = blurWithRadius(mPosition);
            return position.getLongitude();
        }
    }

    /**
     * Returns the current speed
     *
     * @return the current speed (if detected) or `0`
     */
    public float getSpeed() {
        if (mPosition == null) {
            return 0.0f;
        } else {
            return mPosition.getSpeed();
        }
    }

    /**
     * Returns the current altitude
     *
     * @return the current altitude (if detected) or `0`
     */
    public double getAltitude() {
        if (mPosition == null) {
            return 0.0f;
        } else {
            return mPosition.getAltitude();
        }
    }

    /**
     * 涉及到隐私时可以设置模糊半径
     *
     * @param blurRadius the blur radius (in meters)
     */
    public void setBlurRadius(final int blurRadius) {
        mBlurRadius = blurRadius;
    }


    // 卫星状态监听
    private GpsStatus.Listener createGpsStatusListener() {
        return new GpsStatus.Listener() {

            @Override
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
    }


    /**
     * Creates a new LocationListener instance used internally to listen for location updates
     *
     * @return the new LocationListener instance
     */
    private LocationListener createLocationListener() {
        return new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                //此处拿到的是原始经纬度，需要进行火星坐标转换，高德和百度都有提供转换方法
                //高德转换  CoordinateConverter coordinateConverter = new CoordinateConverter(this);
                //         LatLng _latLng = coordinateConverter.coord(latLng).from(CoordinateConverter.CoordType.GPS).convert();
                mPosition = location;
                cachePosition();
                if (mListener != null) {
                    mListener.onPositionChanged(location);
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                MyLogger.i("---------GPS状态改变：" + provider);
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

            @Override
            public void onProviderEnabled(String provider) {
                MyLogger.i("---------GPS启动：" + provider);
            }

            @Override
            public void onProviderDisabled(String provider) {
                MyLogger.i("---------GPS禁用：" + provider);
            }

        };
    }


    private void setCriteria() {
        if (mCriteria == null) {
            mCriteria = new Criteria();
        }
        // 设置定位精确度 Criteria.ACCURACY_COARSE比较粗略，Criteria.ACCURACY_FINE则比较精细
        mCriteria.setAccuracy(Criteria.ACCURACY_FINE);
        // 设置是否要求速度
        mCriteria.setSpeedRequired(true);
        // 设置是否允许运营商收费
        mCriteria.setCostAllowed(false);
        // 设置是否需要方位信息
        mCriteria.setBearingRequired(true);
        // 设置是否需要海拔信息
        mCriteria.setAltitudeRequired(true);
        // 设置对电源的需求
        mCriteria.setPowerRequirement(Criteria.ACCURACY_HIGH);
    }

    private String getProviderName() {
        return getProviderName(mRequireFine);
    }


    private String getProviderName(final boolean requireFine) {
        // 如果是精确定位(GPS)
        if (requireFine) {
            // 指定是主动还是被动模式

            if (mPassive) {
                return PROVIDER_FINE_PASSIVE;
            } else {
                return PROVIDER_FINE;
            }
        }
        // 如果是精确和粗略定位
        else {
            //粗略定位（网络定位）
            if (hasLocationEnabled(PROVIDER_COARSE)) {
                if (mPassive) {
                    // 网络定位没有被动模式
                    throw new RuntimeException("There is no passive provider for the coarse location");
                } else {
                    return PROVIDER_COARSE;
                }
            }
            // 如果网络定位不可用
            else {
                // if we can use fine location (GPS)
                if (hasLocationEnabled(PROVIDER_FINE) || hasLocationEnabled(PROVIDER_FINE_PASSIVE)) {
                    // we have to use fine location (GPS) because coarse location (network) was not available
                    return getProviderName(true);
                }
                // no location is available so return the provider with the minimum permission level
                else {
                    return PROVIDER_COARSE;
                }
            }
        }
    }

    /**
     * 从缓存中返回最后一个位置
     *
     * @return the cached position
     */
    private Location getCachedPosition() {
        if (mCachedPosition != null) {
            return mCachedPosition;
        } else {
            try {
                if (ActivityCompat.checkSelfPermission(MyApplication.getInstance(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MyApplication.getInstance(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return null;
                }
                return mLocationManager.getLastKnownLocation(getProviderName());
            } catch (Exception e) {
                return null;
            }
        }
    }

    /**
     * Caches the current position
     */
    private void cachePosition() {
        if (mPosition != null) {
            mCachedPosition = mPosition;
        }
    }

    /**
     * Opens the device's settings screen where location access can be enabled
     *
     * @param context the Context reference to start the Intent from
     */
    public static void openSettings(final Context context) {
        context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }

    /**
     * Converts a difference in latitude to a difference in kilometers (rough estimation)
     *
     * @param latitude the latitude (difference)
     * @return the kilometers (difference)
     */
    public static double latitudeToKilometer(double latitude) {
        return latitude * LATITUDE_TO_KILOMETER;
    }

    /**
     * Converts a difference in kilometers to a difference in latitude (rough estimation)
     *
     * @param kilometer the kilometers (difference)
     * @return the latitude (difference)
     */
    public static double kilometerToLatitude(double kilometer) {
        return kilometer / latitudeToKilometer(1.0f);
    }

    /**
     * Converts a difference in latitude to a difference in meters (rough estimation)
     *
     * @param latitude the latitude (difference)
     * @return the meters (difference)
     */
    public static double latitudeToMeter(double latitude) {
        return latitudeToKilometer(latitude) * KILOMETER_TO_METER;
    }

    /**
     * Converts a difference in meters to a difference in latitude (rough estimation)
     *
     * @param meter the meters (difference)
     * @return the latitude (difference)
     */
    public static double meterToLatitude(double meter) {
        return meter / latitudeToMeter(1.0f);
    }

    /**
     * Converts a difference in longitude to a difference in kilometers (rough estimation)
     *
     * @param longitude the longitude (difference)
     * @param latitude  the latitude (absolute)
     * @return the kilometers (difference)
     */
    public static double longitudeToKilometer(double longitude, double latitude) {
        return longitude * LONGITUDE_TO_KILOMETER_AT_ZERO_LATITUDE * Math.cos(Math.toRadians(latitude));
    }

    /**
     * Converts a difference in kilometers to a difference in longitude (rough estimation)
     *
     * @param kilometer the kilometers (difference)
     * @param latitude  the latitude (absolute)
     * @return the longitude (difference)
     */
    public static double kilometerToLongitude(double kilometer, double latitude) {
        return kilometer / longitudeToKilometer(1.0f, latitude);
    }

    /**
     * Converts a difference in longitude to a difference in meters (rough estimation)
     *
     * @param longitude the longitude (difference)
     * @param latitude  the latitude (absolute)
     * @return the meters (difference)
     */
    public static double longitudeToMeter(double longitude, double latitude) {
        return longitudeToKilometer(longitude, latitude) * KILOMETER_TO_METER;
    }

    /**
     * Converts a difference in meters to a difference in longitude (rough estimation)
     *
     * @param meter    the meters (difference)
     * @param latitude the latitude (absolute)
     * @return the longitude (difference)
     */
    public static double meterToLongitude(double meter, double latitude) {
        return meter / longitudeToMeter(1.0f, latitude);
    }


    public static double calculateDistance(Point start, Point end) {
        return calculateDistance(start.latitude, start.longitude, end.latitude, end.longitude);
    }

    public static double calculateDistance(double startLatitude, double startLongitude, double endLatitude, double endLongitude) {
        float[] results = new float[3];
        Location.distanceBetween(startLatitude, startLongitude, endLatitude, endLongitude, results);
        return results[0];
    }

}