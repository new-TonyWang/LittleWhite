package com.littlewhite.ReceiveFile;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import net.fec.openrq.ArrayDataDecoder;
import net.fec.openrq.parameters.FECParameters;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class RaptorQDecoder implements Runnable {
    private FECParameters fecParameters;
    private ArrayDataDecoder arrayDataDecoder;
    private ReceiveActivity receiveActivity;
    private File receiveFile;
    private Handler RaptorDecoderHandler;
    private final CountDownLatch handlerInitLatch;
    public RaptorQDecoder(ReceiveActivity receiveActivity){
        this.receiveActivity = receiveActivity;
        this.receiveFile = initReceiveFile();
        this.handlerInitLatch = new CountDownLatch(1);
    }
    private File initReceiveFile(){
        File DOWNLOADSDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) ;//外存DownLoad目录
        if(!DOWNLOADSDir.exists()){
            DOWNLOADSDir.mkdir();
        }
        File DownloadFileDir = new File(DOWNLOADSDir.getAbsolutePath()+"/QRCodes");
        if(!DownloadFileDir.exists()){
            DownloadFileDir.mkdir();
        }
       // Log.i(this.getClass().toString(),"启动");
        File receiveFile = new File(DownloadFileDir.getAbsolutePath()+"/tmp"+System.currentTimeMillis());//后续利用数据库加上端点续传
        if(!DownloadFileDir.exists()){
            try {
                DownloadFileDir.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return receiveFile;
    }
    public Handler getHandler(){
        try {
            handlerInitLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return this.RaptorDecoderHandler;
    }
    @Override
    public void run() {
        Looper.prepare();
        this.RaptorDecoderHandler = new RaptorQDecoderHandler(receiveActivity,this.fecParameters,this.arrayDataDecoder,this.receiveFile);
        this.handlerInitLatch.countDown();
        Looper.loop();

    }
}
