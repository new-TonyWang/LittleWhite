package com.littlewhite.ReceiveFile;

import android.nfc.Tag;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.littlewhite.Camera.CameraManager;
import com.littlewhite.R;

import java.io.IOException;

public class ReceiveHandler extends Handler {
    private ReceiveActivity receiveActivity;
    private MergeFileThread mergeFile;
    private MultiDecoder multiDecoder;
    private CameraManager cameraManager;
    private State state;
    private enum State {
        PREVIEW,
        SUCCESS,
        DONE
    }
    public ReceiveHandler(ReceiveActivity receiveActivity,CameraManager cameraManager)  {
        Log.i(this.getClass().toString(),"启动");
        this.receiveActivity = receiveActivity;
        this.cameraManager = cameraManager;
        this.mergeFile =  new MergeFileThread(this.receiveActivity);
        this.multiDecoder = new MultiDecoder(this.receiveActivity,this.mergeFile);
        Thread MergeThread = new Thread(this.mergeFile);
        Thread MultiDecoder = new Thread(this.multiDecoder);
        MergeThread.start();
        MultiDecoder.start();
        this.cameraManager.startPreview();
        state = State.SUCCESS;
        restartPreviewAndDecode();
    }
    @Override
    public void handleMessage(Message message) {
            switch(message.what){
                case R.id.update_progress:
                    receiveActivity.UpgradeProgress(message.arg1);
                    receiveActivity.setTotalQRnum(message.arg2);
                    break;
                case R.id.finish:
                    Message finish = obtainMessage(R.id.finish);
                    mergeFile.getHandler().sendMessageAtFrontOfQueue(finish);//发送消息到对第一个位置
                    Looper.myLooper().quit();
                    receiveActivity.TransmissionComplete();
                    break;
                case R.id.stop:
                    break;
            }
    }
    private void restartPreviewAndDecode() {
        if (state == State.SUCCESS) {
            state = State.PREVIEW;
            cameraManager.requestPreviewFrame(multiDecoder.getHandler(), R.id.decode);

        }
    }
}
