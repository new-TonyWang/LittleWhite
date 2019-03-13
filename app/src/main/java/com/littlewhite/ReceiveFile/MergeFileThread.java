package com.littlewhite.ReceiveFile;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;


public class MergeFileThread implements Runnable {
    private Handler handler;
    private File outputfile;
    private ReceiveActivity receiveActivity;
    private final CountDownLatch handlerInitLatch;
    public MergeFileThread(ReceiveActivity receiveActivity) {
        File DOWNLOADSDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) ;//外存DownLoad目录
        if(!DOWNLOADSDir.exists()){
            DOWNLOADSDir.mkdir();
        }
       File DownloadFileDir = new File(DOWNLOADSDir.getAbsolutePath()+"/QRCodes");
        if(!DownloadFileDir.exists()){
            DownloadFileDir.mkdir();
        }
        Log.i(this.getClass().toString(),"启动");
        File receiveFile = new File(DownloadFileDir.getAbsolutePath()+"/tmp"+System.currentTimeMillis());//后续利用数据库加上端点续传
        if(!DownloadFileDir.exists()){
            try {
                DownloadFileDir.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.outputfile = receiveFile;
        this.receiveActivity = receiveActivity;
        this.handlerInitLatch = new CountDownLatch(1);
    }
    public Handler getHandler(){
        try {
            handlerInitLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return this.handler;
    }
    @Override
    public void run() {
        Looper.prepare();
        this.handler = new MergeFileHandler(receiveActivity.getReceiveHandler(),this.outputfile);
        handlerInitLatch.countDown();
        Looper.loop();
    }
}
