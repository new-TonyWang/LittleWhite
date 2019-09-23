package com.littlewhite.ReceiveFile.ColorCode;

import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.CountDownLatch;
import com.littlewhite.ReceiveFile.ReceiveActivity;

public class RGBbinary extends Thread{
    protected Handler handler;
    protected ReceiveActivity ReceiveActivity;
    protected QRCodeDecodeThread qrCodeDecodeThread;
    protected ReceiveActivity receiveActivity;
    protected final CountDownLatch handlerInitLatch;

    public RGBbinary( ReceiveActivity receiveActivity,QRCodeDecodeThread qrCodeDecodeThread) {
        this.qrCodeDecodeThread = qrCodeDecodeThread;
        ReceiveActivity = receiveActivity;
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

    }
}
