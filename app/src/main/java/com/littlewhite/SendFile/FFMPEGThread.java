package com.littlewhite.SendFile;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.CountDownLatch;

public class FFMPEGThread implements Runnable {
    private SendFileActivity sendFileActivity;
    private final CountDownLatch handlerInitLatch;
    private FFMPEGHandler ffmpegHandler;
    public FFMPEGThread(SendFileActivity sendFileActivity) {
        this.sendFileActivity = sendFileActivity;
        this.handlerInitLatch = new CountDownLatch(1);
    }
    public FFMPEGHandler getHandler(){
        try {
            handlerInitLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return this.ffmpegHandler;
    }

    @Override
    public void run() {
        Looper.prepare();
        this.ffmpegHandler = new FFMPEGHandler(this.sendFileActivity);
        handlerInitLatch.countDown();
        Looper.loop();

    }
}
