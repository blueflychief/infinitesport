package com.infinite.com.infinitesport.pathtrack;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.infinite.com.infinitesport.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PathTrackActivity extends AppCompatActivity implements LocationSource, View.OnClickListener {
    private static final String TAG = "MainActivity";
    private AMap mAmap = null;

    private Button bt_test;
    private Button bt_real;
    public MapView mMapView = null;
    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    //声明mLocationOption对象
    public AMapLocationClientOption mLocationOption = null;
    PolylineOptions polylineOptions = new PolylineOptions();
    private LocationSource.OnLocationChangedListener mOnLocationChangedListener = null;
    private final int LOCATION_INTERVAL = 5000;  //定位时间间隔
    private final int MARK_MOVE_INTERVAL = 40;  //移动间隔

    //行驶轨迹
    private List<LatLng> mMoveTrack;

    //古城---天安门---通州北苑
    private List<LatLng> mTestTrack = new ArrayList() {{
        add(new LatLng(39.90762942, 116.18968964));
        add(new LatLng(39.90789277, 116.39791489));
        add(new LatLng(39.90381065, 116.63738251));
    }};
    private Marker mMoveMarker;
    private File mTmpfile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_path_track);
        mMapView = (MapView) findViewById(R.id.map);
        bt_test = (Button) findViewById(R.id.bt_test);
        bt_real = (Button) findViewById(R.id.bt_real);
        bt_test.setOnClickListener(this);
        bt_real.setOnClickListener(this);
        initAMap(savedInstanceState);
        initLocationServer();
        initData();
        initLocation();

    }

    private void initData() {
        mMoveTrack = new ArrayList<>();
        SimpleDateFormat dateFormat =
                new SimpleDateFormat("yyyy-MM-dd");
        mTmpfile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "mypath", dateFormat.format(new Date()) + "_path.txt");
        Log.i(TAG, "------mTmpfile: " + mTmpfile);
        if (!mTmpfile.getParentFile().exists()) {
            mTmpfile.getParentFile().mkdir();
        }
        try {
            mTmpfile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "------mTmpfile: " + mTmpfile.exists());
    }

    private void initAMap(Bundle savedInstanceState) {
        Log.i(TAG, "------initAMap: " + mMapView);
        mMapView.onCreate(savedInstanceState);
        if (mAmap == null) {
            mAmap = mMapView.getMap();
        }
    }

    private void initLocationServer() {
        mAmap.setLocationSource(this);// 设置定位监听，这句不写无法点击定位和显示小蓝点
        mAmap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        mAmap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        // 设置定位的类型为定位模式：
        // 定位（AMap.LOCATION_TYPE_LOCATE）、
        // 跟随（AMap.LOCATION_TYPE_MAP_FOLLOW）
        // 地图根据面向方向旋转（AMap.LOCATION_TYPE_MAP_ROTATE）三种模式
        mAmap.setMyLocationType(AMap.LOCATION_TYPE_MAP_FOLLOW);
    }


    private void initLocation() {
        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(mAMapLocationListener);
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //设置是否只定位一次,默认为false
        mLocationOption.setOnceLocation(false);
        //设置是否强制刷新WIFI，默认为强制刷新
        mLocationOption.setWifiActiveScan(true);
        //设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setMockEnable(false);
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(LOCATION_INTERVAL);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();
    }

    //可以通过类implement方式实现AMapLocationListener接口，也可以通过创造接口类对象的方法实现
//以下为后者的举例：
    private AMapLocationListener mAMapLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(final AMapLocation amapLocation) {
            if (amapLocation != null) {
                if (amapLocation.getErrorCode() == 0) {
                    //定位成功回调信息，设置相关消息
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date = new Date(amapLocation.getTime());
                    Log.i(TAG, "onLocationChanged: " + df.format(date));//定位时间

                    Log.i(TAG, "onLocationChanged: " + amapLocation.getLocationType());//获取当前定位结果来源，如网络定位结果，详见定位类型表
                    Log.i(TAG, "onLocationChanged: " + amapLocation.getLatitude());//获取纬度
                    Log.i(TAG, "onLocationChanged: " + amapLocation.getLongitude());//获取经度
                    Log.i(TAG, "onLocationChanged: " + amapLocation.getAccuracy());//获取精度信息
                    Log.i(TAG, "onLocationChanged: " + amapLocation.getAddress());//地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
                    Log.i(TAG, "onLocationChanged: " + amapLocation.getCountry());//国家信息
                    Log.i(TAG, "onLocationChanged: " + amapLocation.getProvince());//省信息
                    Log.i(TAG, "onLocationChanged: " + amapLocation.getCity());//城市信息
                    Log.i(TAG, "onLocationChanged: " + amapLocation.getDistrict());//城区信息
                    Log.i(TAG, "onLocationChanged: " + amapLocation.getStreet());//街道信息
                    Log.i(TAG, "onLocationChanged: " + amapLocation.getStreetNum());//街道门牌号信息
                    Log.i(TAG, "onLocationChanged: " + amapLocation.getCityCode());//城市编码
                    Log.i(TAG, "onLocationChanged: " + amapLocation.getAdCode());//地区编码
                    Log.i(TAG, "onLocationChanged: " + amapLocation.getAoiName());//获取当前定位点的AOI信息

//                    mOnLocationChangedListener.onLocationChanged(amapLocation);// 显示系统小蓝点
                    mMoveTrack.add(new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude()));   //存储当前经纬度
                    String s = amapLocation.getLatitude() + "," + amapLocation.getLongitude();
                    whiterSchedule(mTmpfile, s);

                } else {
                    //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                    Log.e("AmapError", "location Error, ErrCode:"
                            + amapLocation.getErrorCode() + ", errInfo:"
                            + amapLocation.getErrorInfo());
                }
            }
        }
    };


    private void moveToCurrent(double lat, double lon, int rank) {
        CameraUpdate cu = CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(lat, lon), rank, 0, 0));
        mAmap.animateCamera(cu);
    }

    /**
     * 激活定位
     */
    @Override
    public void activate(LocationSource.OnLocationChangedListener listener) {
        mOnLocationChangedListener = listener;
    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        mOnLocationChangedListener = null;
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        mLocationClient = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        if (null != mLocationClient) {
            mLocationClient.onDestroy();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
        deactivate();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }


    private void whiterSchedule(File file, final String s) {
        //启动一个线程每5秒钟向日志文件写一次数据
        Executor exec = Executors.newScheduledThreadPool(1);
        exec.execute(new WriteThread(file, s));
    }


    /**
     * 围绕操场的跑道画圆，根据速度显示颜色
     */
    private void drawTrack(List<LatLng> mLatLngs) {
        mAmap.addPolyline(new PolylineOptions()
                .colorValues(CaculateUtil.caculateSpeedColor(mLatLngs, LOCATION_INTERVAL))
                .addAll(mLatLngs)
                .useGradient(true)
                .width(10));
        moveToCurrent(mLatLngs.get(0).latitude, mLatLngs.get(0).longitude, 16);
    }

    /**
     * 播放运动轨迹
     */
    private void initMoveMarker(PolylineOptions polylineOptions) {
        Polyline line = mAmap.addPolyline(polylineOptions);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.setFlat(true)
                .anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.marker))
                .position(polylineOptions.getPoints().get(0));
        mMoveMarker = mAmap.addMarker(markerOptions);
        mMoveMarker.setRotateAngle((float) CaculateUtil.getAngle(line, 0));
        moveLooper(line);
    }

    /**
     * 循环进行移动逻辑
     */
    public void moveLooper(final Polyline line) {
        new Thread() {
            public void run() {
                while (true) {
                    for (int i = 0; i < line.getPoints().size() - 1; i++) {

                        LatLng startPoint = line.getPoints().get(i);
                        LatLng endPoint = line.getPoints().get(i + 1);
                        mMoveMarker.setPosition(startPoint);

                        mMoveMarker.setRotateAngle((float) CaculateUtil.getAngle(startPoint,
                                endPoint));

                        double slope = CaculateUtil.getSlope(startPoint, endPoint);
                        // 是不是正向的标示（向上设为正向）
                        boolean isReverse = (startPoint.latitude > endPoint.latitude);

                        double intercept = CaculateUtil.getInterception(slope, startPoint);

                        double xMoveDistance = isReverse ? CaculateUtil.getXMoveDistance(slope)
                                : -1 * CaculateUtil.getXMoveDistance(slope);

                        for (double j = startPoint.latitude; !((j > endPoint.latitude) ^ isReverse);
                             j = j - xMoveDistance) {
                            LatLng latLng = null;
                            if (slope != Double.MAX_VALUE) {
                                latLng = new LatLng(j, (j - intercept) / slope);
                            } else {
                                latLng = new LatLng(j, startPoint.longitude);
                            }
                            mMoveMarker.setPosition(latLng);
                            moveToCurrent(latLng.latitude, latLng.longitude, 17);
                            try {
                                Thread.sleep(MARK_MOVE_INTERVAL);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }
            }

        }.start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_test:
                drawTrack(CaculateUtil.readLatLngs(CaculateUtil.coords));
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        initMoveMarker(CaculateUtil.getPolylineOptions(CaculateUtil.coords));
                    }
                }, 2000);
                break;
            case R.id.bt_real:
                drawTrack(FileUtil.logReader(mTmpfile));
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        initMoveMarker(FileUtil.logReader(mTmpfile));
//                    }
//                }, 2000);
                break;
        }
    }
}
