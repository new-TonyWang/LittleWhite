package com.littlewhite.SendFile;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.littlewhite.SendReceive;

import java.io.File;
import java.util.concurrent.CountDownLatch;

import static android.content.ContentValues.TAG;

public class RaptorEncoder extends SendReceive<RaptorEncoderHandler> implements Runnable {
    private SendFileActivity sendFileActivity;
    private FFMPEGThread ffmpegThread;
   // private RaptorEncoderHandler raptorEncoderHandler;
    private final CountDownLatch handlerInitLatch;


    public RaptorEncoder(SendFileActivity sendFileActivity, FFMPEGThread ffmpegThread) {
        this.sendFileActivity = sendFileActivity;
        this.ffmpegThread = ffmpegThread;
        this.handlerInitLatch = new CountDownLatch(1);
    }

    @Override
    public RaptorEncoderHandler getHandler(){
        try {
            handlerInitLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return super.getHandler();
    }
    @Override
    public void run() {
        Looper.prepare();
        handler = new RaptorEncoderHandler(this.sendFileActivity, this.ffmpegThread.getHandler());
        handlerInitLatch.countDown();
        Looper.loop();

    }
}
