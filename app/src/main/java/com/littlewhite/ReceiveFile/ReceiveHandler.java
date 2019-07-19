package com.littlewhite.ReceiveFile;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.littlewhite.Camera.CameraManager;
import com.littlewhite.Camera.newCameraManager;
import com.littlewhite.R;
import com.littlewhite.ReceiveFile.QRcodeDecoder.DecoderThread;
import com.littlewhite.ReceiveFile.QRcodeDecoder.MultiDecoder;
import com.littlewhite.ReceiveFile.SqllitUtil.FileInfo;
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
    private SharedPreferences sharedPreferences;
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
        this.sqllitData = new SqllitData(this.receiveActivity);
        this.zipThread = new ZipThread(this.receiveActivity,sqllitData);
        /*
        this.mergeFile =  new MergeFileThread(this.receiveActivity);
        this.multiDecoder = new MultiDecoder(this.receiveActivity,this.mergeFile);
        Thread MergeThread = new Thread(this.mergeFile);
        Thread MultiDecoder = new Thread(this.multiDecoder);
       // MergeThread.start();
        //MultiDecoder.start();
        */
        this.raptorQDecoder = new RaptorQDecoder(this.receiveActivity,this.zipThread,sqllitData);
        this.decoderThread = new DecoderThread(this.receiveActivity,this.raptorQDecoder);
        Thread raptorQDecoderThread = new Thread(this.raptorQDecoder);
        Thread DecoderThread = new Thread(this.decoderThread);
        Thread ZipThread = new Thread(this.zipThread);
        ZipThread.start();
        raptorQDecoderThread.start();
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
                case R.id.Init:
                    receiveActivity.setTotalQRnum(message.arg1);
                  // receiveActivity.UpgradeProgress(message.arg1);
                    break;
                case R.id.update_progress:
                   // receiveActivity.setTotalQRnum(message.arg2);
                    receiveActivity.UpgradeProgress(message.arg1);
                    break;
                case R.id.finish:
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
