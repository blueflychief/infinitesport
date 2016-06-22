package com.infinite.com.infinitesport.goodlocation;

import android.text.TextUtils;
import android.util.Log;

import com.amap.api.maps.model.LatLng;
import com.infinite.com.infinitesport.util.MyLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016-06-20.
 */
public class FileUtil {
    private static final String TAG = "FileUtil";


    /**
     * 将信息记录到日志文件
     */
    public static void writeLog(File logFile, String mesInfo) throws IOException {
        if (logFile == null) {
            throw new IllegalStateException("-----logFile can not be null!");
        }
        Writer txtWriter = null;
        try {
            txtWriter = new FileWriter(logFile, true);
            txtWriter.write(mesInfo + "\n");
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        } finally {
            if (txtWriter != null)
                try {
                    txtWriter.close();
                } catch (IOException e) {
                    throw new RuntimeException("关闭失败！");
                }
        }
    }

    //循环读取每一行，返回ArrayList
    public static List<String> readLog(File logFile) {
        List<String> info = new ArrayList<>();
        if (!logFile.exists()) {
            throw new NullPointerException("-----logFile can not be null!");
        }
        Reader reader = null;
        try {
            reader = new FileReader(logFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader bf = new BufferedReader(reader);
        String line = "";
        try {
            while ((line = bf.readLine()) != null) {
                info.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return info;
    }

    //循环读取每一行，返回ArrayList
    public static List<String> readLogFromString(String log_string) {
        List<String> info = new ArrayList<>();
        if (!TextUtils.isEmpty(log_string)) {
            String[] l = log_string.split("\n");
            for (int i = 0, n = l.length; i < n; i++) {
                if (!TextUtils.isEmpty(l[i])) {
                    info.add(l[i]);
                }
            }
        }
        return info;
    }


    public static List<LatLng> getAllLatLng(List<String> log) {
        if (log == null) {
            return null;
        }
        List<LatLng> latLngs = new ArrayList<>();
        for (String info : log) {
            TrackPoint point = getTrackPoint(info);
            if (point != null) {
                latLngs.add(new LatLng(point.getLat(), point.getLng()));
            }
        }
        return latLngs;
    }


    //    String s = location.getTime() + "#"
//            + location.getLatitude() + "#"
//            + location.getLongitude() + "#"
//            + location.getAltitude() + "#"
//            + location.getBearing() + "#"
//            + location.getAccuracy();
    public static TrackPoint getTrackPoint(String log) {
        if (log == null) {
            return null;
        }
        TrackPoint point = null;
        if (log.contains("#")) {
            String[] l = log.split("#");
            if (l.length > 5) {
                point = new TrackPoint();
                point.setTime(Long.parseLong(l[0]));
                point.setLat(Double.parseDouble(l[1]));
                point.setLng(Double.parseDouble(l[2]));
                point.setAltitude(Double.parseDouble(l[3]));
                point.setBearing(Double.parseDouble(l[4]));
                point.setAccuracy(Float.parseFloat(l[5]));
            } else {
                MyLogger.i("-----错误数据：" + log);
            }
        } else {
            MyLogger.i("-----错误数据：" + log);
        }
        return point;
    }

    public static List<TrackPoint> getAllTrackPoint(List<String> log) {
        if (log == null) {
            return null;
        }
        List<TrackPoint> latLngs = new ArrayList<>();
        for (String info : log) {
            TrackPoint point = getTrackPoint(info);
            if (point != null) {
                latLngs.add(point);
            }
        }
        return latLngs;
    }
}
