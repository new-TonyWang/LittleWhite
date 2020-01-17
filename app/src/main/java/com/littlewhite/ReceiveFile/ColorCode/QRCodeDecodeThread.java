package com.littlewhite.ReceiveFile.ColorCode;

import android.os.Handler;
import android.os.Looper;

import com.littlewhite.ColorCode.HSVColorTable;
import com.littlewhite.ReceiveFile.QRcodeDecoder.DecoderHandler;
import com.littlewhite.ReceiveFile.RaptorQDecoder;
import com.littlewhite.ReceiveFile.ReceiveActivity;

import java.util.concurrent.CountDownLatch;

/**
 * 该类是用于解析将RGB三通道分别二值化得到的三张二维码
 */
public class QRCodeDecodeThread extends Thread {
    private Handler handler;
    private ReceiveActivity activity;
    private RaptorQDecoder raptorQDecoder ;
    private final CountDownLatch handlerInitLatch;

    public QRCodeDecodeThread(ReceiveActivity activity, RaptorQDecoder raptorQDecoder) {
        this.activity = activity;
        this.raptorQDecoder = raptorQDecoder;
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
        this.handler = new QRCodeDecodeHandler(raptorQDecoder.getHandler(),activity);
        handlerInitLatch.countDown();
        //Log.i(this.getClass().toString(),"启动");
        Looper.loop();
    }

}
