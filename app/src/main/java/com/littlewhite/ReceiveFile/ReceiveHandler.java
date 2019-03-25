package com.littlewhite.ReceiveFile;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.littlewhite.Camera.CameraManager;
import com.littlewhite.R;
import com.littlewhite.ReceiveFile.QRcodeDecoder.MultiDecoder;

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
        state = State.SUCCESS;
        this.cameraManager.startPreview();
        restartPreviewAndDecode();

    }
    @Override
    public void handleMessage(Message message) {
            switch(message.what){
                case R.id.update_progress:
                    receiveActivity.setTotalQRnum(message.arg2);
                    receiveActivity.UpgradeProgress(message.arg1);

                    break;
                case R.id.finish:
                    Message finish = obtainMessage(R.id.finish);
                    multiDecoder.getHandler().sendMessageAtFrontOfQueue(finish);//发送消息到对第一个位置
                   // Looper.myLooper().quit();
                    receiveActivity.TransmissionComplete();
                    break;
                case R.id.stop:
                    break;
            }
    }
    private void restartPreviewAndDecode() {
        if (state == State.SUCCESS) {
            state = State.PREVIEW;
            cameraManager.requestPreviewFrameWithBuffer(multiDecoder.getHandler(), R.id.decode);

        }
    }
}
