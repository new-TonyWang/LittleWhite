package com.littlewhite.History;

import android.content.Context;
import android.os.Environment;

import com.littlewhite.ReceiveFile.SqllitUtil.FileInfo;
import com.littlewhite.ReceiveFile.SqllitUtil.SqllitData;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class HistoryManager {
    private SqllitData sqllitData;
    private File DownLoadPath;
    public HistoryManager(Context context){
    this.sqllitData = new SqllitData(context);
    this.DownLoadPath = initReceivePath();
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
    public List<FileInfo> SearchHistory(){
       return sqllitData.SearchHistory();
    }
    public void DeleteFileLog(LinkedList ID ){
        StringBuilder IDS = new StringBuilder();
        for(int i = 0;i<ID.size()-1;i++){
        IDS.append(ID.poll());
        IDS.append(",");
        }
        IDS.append(ID.poll());
        this.sqllitData.DeleteFileLog(IDS);
    }
    public void DeleteZeroProgressFile(){

    }
}
