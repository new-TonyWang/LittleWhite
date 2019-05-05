package com.littlewhite.ReceiveFile;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.littlewhite.ReceiveFile.SqllitUtil.FileInfo;
import com.littlewhite.ReceiveFile.SqllitUtil.SqllitData;
import com.littlewhite.ZipFile.ZipThread;

import net.fec.openrq.ArrayDataDecoder;
import net.fec.openrq.parameters.FECParameters;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class RaptorQDecoder implements Runnable {
   // private FECParameters fecParameters;
   // private ArrayDataDecoder arrayDataDecoder;
    private ReceiveActivity receiveActivity;
    private File receivePath;
    private Handler RaptorDecoderHandler;
    private final CountDownLatch handlerInitLatch;
    private SqllitData sqllitData;
    private ZipThread zipThread;
    public RaptorQDecoder(ReceiveActivity receiveActivity,ZipThread zipThread){
        this.receiveActivity = receiveActivity;
        this.receivePath = initReceivePath();
        this.sqllitData = new SqllitData(this.receiveActivity);
        this.zipThread = zipThread;
       /* if(fileInfo == null){//接收新文件
       // this.receiveFile = initReceiveFile(ReceivePath);
        //FileInfo NewFile = new FileInfo(receiveFile.getName());
       // InsertNewFile(NewFile);
        }
        else{//继续接收老文件
         //   this.receiveFile =RestartReceiveFile(ReceivePath,fileInfo.getFileName());
        }
        */
        this.handlerInitLatch = new CountDownLatch(1);
    }
    private File initReceivePath() {
        File DOWNLOADSDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);//外存DownLoad目录
        if (!DOWNLOADSDir.exists()) {
            DOWNLOADSDir.mkdir();
        }
        File DownloadFileDir = new File(DOWNLOADSDir.getAbsolutePath() + "/QRCodes");
        if (!DownloadFileDir.exists()) {
            DownloadFileDir.mkdir();
        }
        return DownloadFileDir;
    }
    /*private File initReceiveFile(File ReceivePath){
       // Log.i(this.getClass().toString(),"启动");
        File receiveFile = new File(ReceivePath.getAbsolutePath()+"/tmp"+System.currentTimeMillis());//后续利用数据库加上端点续传
        if(!ReceivePath.exists()){
            try {
                ReceivePath.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return receiveFile;
    }*/
    /**
     * 断点续传的时候用
     * @param ReceivePath
     * @param FileName
     * @return
     */
    private File RestartReceiveFile(File ReceivePath,String FileName){
        // Log.i(this.getClass().toString(),"启动");
        return new File(ReceivePath.getAbsolutePath()+"/"+FileName);
    }

    public Handler getHandler(){
        try {
            handlerInitLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return this.RaptorDecoderHandler;
    }

    /**
     * 获取存在数据库中的文件信息
     * @return
     */
    private List<FileInfo> getList(){
     return this.sqllitData.SearchUnComplete();

    }
    @Override
    public void run() {
        Looper.prepare();
        List list = getList();
        this.RaptorDecoderHandler = new RaptorQDecoderHandler(receiveActivity,this.receivePath,this.sqllitData,list,zipThread);
        this.handlerInitLatch.countDown();
        Looper.loop();

    }
}
