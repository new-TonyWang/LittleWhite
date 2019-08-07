package com.littlewhite.ReceiveFile.QRcodeDecoder;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.littlewhite.ColorCode.HSVColorTable;
import com.littlewhite.ReceiveFile.RaptorQDecoder;
import com.littlewhite.ReceiveFile.ReceiveActivity;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

import static com.google.zxing.EncodeHintType.ERROR_CORRECTION;

public class DecoderThread implements Runnable {

    private HashMap<DecodeHintType, Object> decodeHints = new HashMap<DecodeHintType, Object>();
    private Handler handler;
    private com.littlewhite.ReceiveFile.ReceiveActivity ReceiveActivity;
    private SharedPreferences sharedPreferences;
    private RaptorQDecoder raptorQDecoder ;
    private final CountDownLatch handlerInitLatch;
    public DecoderThread (ReceiveActivity ReceiveActivity,RaptorQDecoder raptorQDecoder){
        this.raptorQDecoder = raptorQDecoder;
        this.ReceiveActivity = ReceiveActivity;
       // this.sharedPreferences = this.ReceiveActivity.getSharedPreferences("data", Context.MODE_PRIVATE);//获取context的存储对象，用于读取设置
        setHints();
        this.handlerInitLatch = new CountDownLatch(1);
    }
    private void setHints(){
       //decodeHints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
        //decodeHints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.);
        decodeHints.put(DecodeHintType.FILEDATA,Boolean.TRUE);
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
        this.handler = new DecoderHandler(decodeHints,raptorQDecoder.getHandler(),ReceiveActivity,new HSVColorTable());
        handlerInitLatch.countDown();
        //Log.i(this.getClass().toString(),"启动");
        Looper.loop();
    }
}
