package com.littlewhite.ReceiveFile;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


import com.littlewhite.Camera.newCameraManager;
import com.littlewhite.R;
import com.littlewhite.ReceiveFile.ColorCode.Bbinary;
import com.littlewhite.ReceiveFile.ColorCode.Gbinary;
import com.littlewhite.ReceiveFile.ColorCode.QRCodeDecodeThread;
import com.littlewhite.ReceiveFile.ColorCode.Rbinary;
import com.littlewhite.ReceiveFile.QRcodeDecoder.DecoderThread;

import com.littlewhite.ReceiveFile.SqllitUtil.SqllitData;
import com.littlewhite.ZipFile.ZipThread;

public class ReceiveHandler extends Handler {
    private ReceiveActivity receiveActivity;
   // private MergeFileThread mergeFile;
    //private MultiDecoder multiDecoder;
    private newCameraManager cameraManager;
    private DecoderThread decoderThread;
    private RaptorQDecoder raptorQDecoder;
    private ZipThread zipThread;
    private SqllitData sqllitData;
    private QRCodeDecodeThread qrCodeDecodeThread;
    private Rbinary rbinary;
    private Gbinary gbinary;
    private Bbinary bbinary;
   // private SharedPreferences sharedPreferences;
    private State state;
    private enum State {
        PREVIEW,
        SUCCESS,
        DONE
    }
    public ReceiveHandler(ReceiveActivity receiveActivity, newCameraManager cameraManager,int iscolor)  {
        Log.i(this.getClass().toString(),"启动");
        this.receiveActivity = receiveActivity;
        this.cameraManager = cameraManager;
        this.sqllitData = new SqllitData(this.receiveActivity);//启动数据库，记录接收到的文件信息
        this.zipThread = new ZipThread(this.receiveActivity,sqllitData);//压缩文件线程
        /*
        this.mergeFile =  new MergeFileThread(this.receiveActivity);
        this.multiDecoder = new MultiDecoder(this.receiveActivity,this.mergeFile);
        Thread MergeThread = new Thread(this.mergeFile);
        Thread MultiDecoder = new Thread(this.multiDecoder);
       // MergeThread.start();
        //MultiDecoder.start();
        */

        this.raptorQDecoder = new RaptorQDecoder(this.receiveActivity,this.zipThread,sqllitData);//RaptorQ线程
        this.qrCodeDecodeThread = new QRCodeDecodeThread(this.receiveActivity,this.raptorQDecoder);
        this.rbinary = new Rbinary(this.receiveActivity,this.qrCodeDecodeThread);
        this.gbinary = new Gbinary(this.receiveActivity,this.qrCodeDecodeThread);
        this.bbinary = new Bbinary(this.receiveActivity,this.qrCodeDecodeThread);
        this.decoderThread = new DecoderThread(this.receiveActivity,this.raptorQDecoder,this.rbinary,this.gbinary,this.bbinary);//二维码解析线程
        Thread raptorQDecoderThread = new Thread(this.raptorQDecoder);
        Thread DecoderThread = new Thread(this.decoderThread);
        Thread ZipThread = new Thread(this.zipThread);
        Thread rbin = new Thread(this.rbinary);
        Thread gbin = new Thread(this.gbinary);
        Thread bbin = new Thread(this.bbinary);
        Thread qrcodedecode = new Thread(this.qrCodeDecodeThread);
        ZipThread.start();
        raptorQDecoderThread.start();
        qrcodedecode.start();
        rbin.start();
        gbin.start();
        bbin.start();
        DecoderThread.start();
        state = State.SUCCESS;
        this.cameraManager.startPreview();
        switch(iscolor) {
            case R.id.BW:
                restartPreviewAndDecode();//解码黑白
                break;
            case R.id.HSV:
                restartPreviewAndDecodeColorcode();//解码HSV
                break;
            case R.id.RGB:
                restartPreviewAndDecodeRGB();//解码RGB
                break;
        }


    }
    @Override
    public void handleMessage(Message message) {
            switch(message.what){
                case R.id.Init://初始化进度
                    receiveActivity.setTotalQRnum(message.arg1);
                  // receiveActivity.UpgradeProgress(message.arg1);
                    break;
                case R.id.update_progress://更新进度
                   // receiveActivity.setTotalQRnum(message.arg2);
                    receiveActivity.UpgradeProgress(message.arg1);
                    break;
                case R.id.finish://传输完成
                    // Looper.myLooper().quit();
                    Bundle bundle = message.getData();
                    receiveActivity.TransmissionComplete(bundle);
                    break;
                    case R.id.RaptorDecodeFile://Raptor码开始生成文件，可以将其他线程关闭
                        this.receiveActivity.RaptorCalculationStart();
                        Message finish = obtainMessage(R.id.finish);
                        decoderThread.getHandler().sendMessageAtFrontOfQueue(finish);//发送消息到对第一个位置
                        break;
                 //case R.id.
                case R.id.stop:
                    break;
            }
    }
    private void restartPreviewAndDecode() {
        if (state == State.SUCCESS) {
            state = State.PREVIEW;
            //cameraManager.requestPreviewFrameWithBuffer(multiDecoder.getHandler(), R.id.decode);
            cameraManager.requestPreviewFrameWithBuffer(decoderThread.getHandler(), R.id.decode);
           // cameraManager.requestPreviewFrame(decoderThread.getHandler(), R.id.decode);
        }
    }
    private void restartPreviewAndDecodeColorcode() {
        if (state == State.SUCCESS) {
            state = State.PREVIEW;
            //cameraManager.requestPreviewFrameWithBuffer(multiDecoder.getHandler(), R.id.decode);
           cameraManager.requestPreviewFrameWithBuffer(decoderThread.getHandler(), R.id.decodeColor);
            //cameraManager.requestPreviewFrame(decoderThread.getHandler(), R.id.decode);
        }
    }
    private void restartPreviewAndDecodeRGB() {
        if (state == State.SUCCESS) {
            state = State.PREVIEW;
            //cameraManager.requestPreviewFrameWithBuffer(multiDecoder.getHandler(), R.id.decode);
            cameraManager.requestPreviewFrameWithBuffer(decoderThread.getHandler(), R.id.decodeRGB);
            //cameraManager.requestPreviewFrame(decoderThread.getHandler(), R.id.decode);
        }
    }
    public void quitSynchronously() {
        state = State.DONE;
        cameraManager.stopPreview();
        Message stop = Message.obtain(decoderThread.getHandler(), R.id.stop);
        stop.sendToTarget();

        // Be absolutely sure we don't send any queued up messages
        //removeMessages(R.id.decode_succeeded);
        //removeMessages(R.id.decode_failed);
    }
}
