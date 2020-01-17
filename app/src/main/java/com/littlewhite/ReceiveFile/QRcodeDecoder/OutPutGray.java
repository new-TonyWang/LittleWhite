package com.littlewhite.ReceiveFile.QRcodeDecoder;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.CountDownLatch;

public class OutPutGray implements Runnable {
    private Handler handler;
    private final CountDownLatch handlerInitLatch;

    public OutPutGray() {
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
        this.handler = new OutPutGrayHandler();
        handlerInitLatch.countDown();
        Looper.loop();
    }
}
