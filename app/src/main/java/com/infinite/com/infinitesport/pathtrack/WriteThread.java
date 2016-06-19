package com.infinite.com.infinitesport.pathtrack;

import java.io.File;
import java.io.IOException;

/**
 * Created by Administrator on 2016-06-20.
 */
public class WriteThread implements Runnable {
    private String info;
    private File file;

    public WriteThread(File file, String info) {
        this.info = info;
        this.file = file;
    }

    @Override
    public void run() {
        try {
            FileUtil.logMsg(file, info);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
