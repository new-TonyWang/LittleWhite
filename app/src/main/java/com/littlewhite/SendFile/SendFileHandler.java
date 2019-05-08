package com.littlewhite.SendFile;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.littlewhite.ReceiveFile.SqllitUtil.SqllitData;
import com.littlewhite.ZipFile.ZipThread;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class SendFileHandler extends Handler {
    private SendConfigs Configs;
    private FFMPEGThread ffmpegThread;
    private RaptorEncoder raptorEncoder;
    private SendFileActivity sendFileActivity;
    private SqllitData sqllitData;
    private ZipThread zipThread;
    public SendFileHandler(SendFileActivity sendFileActivity){
        this.sendFileActivity = sendFileActivity;
        this.sqllitData = new SqllitData(this.sendFileActivity);
        this.ffmpegThread = new FFMPEGThread(this.sendFileActivity);
        this.raptorEncoder = new RaptorEncoder(this.sendFileActivity,this.ffmpegThread);
        this.zipThread = new ZipThread(this.raptorEncoder,this.sqllitData);
    }

    @Override
    public void handleMessage(Message message) {
        switch (message.what) {

        }
    }


}
