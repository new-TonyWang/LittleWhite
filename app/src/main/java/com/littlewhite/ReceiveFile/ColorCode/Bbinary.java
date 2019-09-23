package com.littlewhite.ReceiveFile.ColorCode;

import android.os.Looper;

import com.littlewhite.ReceiveFile.ReceiveActivity;

public class Bbinary extends RGBbinary{


    public Bbinary(com.littlewhite.ReceiveFile.ReceiveActivity receiveActivity, QRCodeDecodeThread qrCodeDecodeThread) {
        super(receiveActivity, qrCodeDecodeThread);
    }

    @Override
    public void run(){
        Looper.prepare();
        this.handler = new BbinaryHandler(qrCodeDecodeThread.getHandler(),receiveActivity);
        handlerInitLatch.countDown();
        //Log.i(this.getClass().toString(),"启动");
        Looper.loop();
    }
}
