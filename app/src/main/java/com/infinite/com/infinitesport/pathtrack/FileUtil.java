package com.infinite.com.infinitesport.pathtrack;

import android.util.Log;

import com.amap.api.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016-06-20.
 */
public class FileUtil {
    private static final String TAG = "FileUtil";
    private static SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 将信息记录到日志文件
     *
     * @param logFile 日志文件
     * @param mesInfo 信息
     * @throws IOException
     */
    public static void logMsg(File logFile, String mesInfo) throws IOException {
        if (logFile == null) {
            throw new IllegalStateException("logFile can not be null!");
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


    public void readFile01() throws IOException {
        FileReader fr = new FileReader("E:/phsftp/evdokey/evdokey_201103221556.txt");
        BufferedReader br = new BufferedReader(fr);
        String line = "";
        String[] arrs = null;
        while ((line = br.readLine()) != null) {
            arrs = line.split(",");
            System.out.println(arrs[0] + " : " + arrs[1] + " : " + arrs[2]);
        }
        br.close();
        fr.close();
    }


    public static List<LatLng> logReader(File logFile) {
        List<LatLng> mMoveTrack = new ArrayList<>();
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
                mMoveTrack.add(new LatLng(Double.parseDouble(line.split(",")[0]), Double.parseDouble(line.split(",")[1])));
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
        return mMoveTrack;
    }

    public static List<LatLng> logReaderArray(File logFile) {
        List<LatLng> mMoveTrack = new ArrayList<>();
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
                mMoveTrack.add(new LatLng(Double.parseDouble(line.split(",")[0]), Double.parseDouble(line.split(",")[1])));
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
        return mMoveTrack;
    }
}
