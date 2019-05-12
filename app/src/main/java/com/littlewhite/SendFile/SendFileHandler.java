package com.littlewhite.SendFile;

import android.content.ContentValues;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.littlewhite.R;
import com.littlewhite.ReceiveFile.SqllitUtil.SqllitData;
import com.littlewhite.ZipFile.ZipThread;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class SendFileHandler extends Handler {
    //private SendConfigs Configs;
    private FFMPEGThread ffmpegThread;
    private RaptorEncoder raptorEncoder;
    private SendFileActivity sendFileActivity;
    //private SqllitData sqllitData;
    private ZipThread zipThread;
    public SendFileHandler(SendFileActivity sendFileActivity){
        this.sendFileActivity = sendFileActivity;
       // this.sqllitData = new SqllitData(this.sendFileActivity);
        this.ffmpegThread = new FFMPEGThread(this.sendFileActivity);
        new Thread(this.ffmpegThread).start();
        this.raptorEncoder = new RaptorEncoder(this.sendFileActivity,this.ffmpegThread);
        this.zipThread = new ZipThread(this.raptorEncoder);
        new Thread(this.raptorEncoder).start();
        new Thread(this.zipThread).start();

    }

    @Override
    public void handleMessage(Message message) {
        switch (message.what) {
            case R.id.finish:
               // MediaScannerConnection mediaScannerConnection = new MediaScannerConnection(this.sendFileActivity,null);
               // this.sendFileActivity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(this.sendFileActivity.getExternalFilesDir("").getAbsolutePath())));
                this.sendFileActivity.playVideo((String)message.obj);

                sendToZipFinish();
               // mediaScannerConnection.connect();
               // mediaScannerConnection.scanFile(this.sendFileActivity.getExternalFilesDir("").getAbsolutePath(), "video/*");
                break;
            case R.id.RaptorEncodePhase:
                this.sendFileActivity.StartRaptorEncode();
                break;
            case R.id.FFmpegPhase:
                this.sendFileActivity.StartFFmpeg();
                break;
            case R.id.failed:
                break;
        }
    }
    public void StartProgress(SendConfigs sendConfigs){
        sendToFFmpeg(sendConfigs);
        sendToRaptor(sendConfigs);
        sendToZip(sendConfigs);
    }
    private void sendToZip(SendConfigs sendConfigs){
        Message message = Message.obtain(zipThread.getZipHandler(),R.id.ZipFile ,sendConfigs);
        message.sendToTarget();
    }
    private void sendToZipFinish(){
        Message message = obtainMessage(R.id.finish);
        zipThread.getZipHandler().sendMessageAtFrontOfQueue(message);
    }
    private void sendToFFmpeg(SendConfigs sendConfigs){
        Message message = Message.obtain(ffmpegThread.getHandler(), R.id.Init,sendConfigs);
        message.sendToTarget();
    }
    private void sendToRaptor(SendConfigs sendConfigs){
        Message message = Message.obtain(raptorEncoder.getHandler(), R.id.Init,sendConfigs);
        message.sendToTarget();
    }


}
