package com.littlewhite.ZipFile;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.littlewhite.ReceiveFile.ReceiveActivity;

import java.io.File;
import java.util.concurrent.CountDownLatch;


public class ZipThread implements Runnable{

    private Context context;
    private Handler ZipHandler;
    private Handler NextStepHandler;
    private final CountDownLatch handlerInitLatch;
    //private File zipfile;
    public ZipThread(Context context,Handler NextStepHandler){
        this.context = context;
        this.handlerInitLatch = new CountDownLatch(1);
        this.NextStepHandler = NextStepHandler;
        //this.zipfile = new File(file);
    }

    public Handler getZipHandler() {
        try {
            handlerInitLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return ZipHandler;
    }

    @Override
    public void run() {
        Looper.prepare();
        this.ZipHandler = new ZipHandler(context,this.NextStepHandler);
        handlerInitLatch.countDown();
        Looper.loop();
    }
}
