package com.littlewhite.ZipFile;


import android.os.Handler;
import android.os.Looper;

import com.littlewhite.ReceiveFile.SqllitUtil.SqllitData;
import com.littlewhite.SendReceive;
import java.util.concurrent.CountDownLatch;


public class ZipThread implements Runnable{

    private SendReceive SendReceive;
    private Handler ZipHandler;
    private SqllitData sqllitData;
   // private Handler NextStepHandler;
    private final CountDownLatch handlerInitLatch;
    //private File zipfile;
    public ZipThread(SendReceive SendReceive,SqllitData sqllitData){
        this.SendReceive  = SendReceive;
        this.sqllitData = sqllitData;
        this.handlerInitLatch = new CountDownLatch(1);
      //  this.NextStepHandler = NextStepHandler;
        //this.zipfile = new File(file);
    }
    public ZipThread(SendReceive SendReceive){
        this.SendReceive  = SendReceive;
        //this.sqllitData = sqllitData;
        this.handlerInitLatch = new CountDownLatch(1);
        //  this.NextStepHandler = NextStepHandler;
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
        this.ZipHandler = new ZipHandler(SendReceive,sqllitData);
        handlerInitLatch.countDown();
        Looper.loop();
    }
}
