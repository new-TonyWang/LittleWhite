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
    this.DownLoadPath = initReceivePath(context);
    }

    private File initReceivePath(Context context) {
        File DOWNLOADSDir = context.getExternalFilesDir("");//外存DownLoad目录
        if (!DOWNLOADSDir.exists()) {
            DOWNLOADSDir.mkdir();
        }
        File DOWNLOADDir = context.getExternalFilesDir("receive");//外存DownLoad目录
        if (!DOWNLOADDir.exists()) {
            DOWNLOADDir.mkdir();
        }
        return DOWNLOADSDir;
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
