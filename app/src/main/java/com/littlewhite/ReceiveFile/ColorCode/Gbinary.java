package com.littlewhite.ReceiveFile.ColorCode;

import android.os.Looper;

import com.littlewhite.ReceiveFile.ReceiveActivity;

public class Gbinary extends RGBbinary{


    public Gbinary(com.littlewhite.ReceiveFile.ReceiveActivity receiveActivity, QRCodeDecodeThread qrCodeDecodeThread) {
        super(receiveActivity, qrCodeDecodeThread);
    }

    @Override
    public void run(){
        Looper.prepare();
        this.handler = new GbinaryHandler(qrCodeDecodeThread.getHandler(),receiveActivity);
        handlerInitLatch.countDown();
        //Log.i(this.getClass().toString(),"启动");
        Looper.loop();
    }
}
