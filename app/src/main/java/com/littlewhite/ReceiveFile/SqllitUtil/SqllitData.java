package com.littlewhite.ReceiveFile.SqllitUtil;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SqllitData {
    private SqllitHelper helper;
    private String FileID;
    private SQLiteDatabase sqLiteDatabase;
    //private final String sql = "UPDATE File_Logs SET ReceivedSymbolNum=? ,ReceivedNum=? WHERE ID=?";
    private File DownLoadPath = initReceivePath();
    public SqllitData(Context context){
        File path = initReceivePath();
        String dbname = path.getAbsolutePath()+"/LittleWhite.db";
        helper = new SqllitHelper(context,dbname,null,1);
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

    /**
     *获取历史纪录
     * @param
     * @return
     */
    public List<FileInfo> SearchHistory(){
        SQLiteDatabase db = this.helper.getWritableDatabase();
        ArrayList<FileInfo> fileInfos = new ArrayList<>();
       //Cursor cursor = db.query("File_Logs",new String[]{"ID","filename","TotalSymbolNum","ReceivedSymbolNum"},,new String[]{"1"},null,null,null);
        Cursor cursor = db.rawQuery("select * from File_Logs",null);
        while (cursor.moveToNext()){
          //int ID = cursor.getInt(0);
            FileInfo fileInfo = new FileInfo(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getInt(2),
                    cursor.getInt(3),
                    cursor.getBlob(4),
                    cursor.getInt(5)
            );
            fileInfos.add(fileInfo);
        }
        cursor.close();
        db.close();
        return fileInfos;
    }
    public List<FileInfo> SearchUnComplete(){
        SQLiteDatabase db = this.helper.getWritableDatabase();
        ArrayList<FileInfo> fileInfos = new ArrayList<>();
        //Cursor cursor = db.query("File_Logs",new String[]{"ID","filename","TotalSymbolNum","ReceivedSymbolNum"},,new String[]{"1"},null,null,null);
        Cursor cursor = db.rawQuery("select * from File_Logs where HasComplete= "+String.valueOf(0),null);
       // db.
        while (cursor.moveToNext()){
            //int ID = cursor.getInt(0);
            FileInfo fileInfo = new FileInfo(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getInt(2),
                    cursor.getInt(3),
                    cursor.getBlob(4),
                    0
            );
            fileInfos.add(fileInfo);
        }
        cursor.close();
        db.close();
        return fileInfos;
    }
    public void InsertNewFile(String fileInfo){
        SQLiteDatabase db = this.helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("FileName",fileInfo);
        cv.put("HasComplete",0);
        //cv.put("TotalSymbolNum",fileInfo.getTotalSymbolNum());
        //cv.put("ReceivedSymbolNum",null);
        this.FileID = String.valueOf(db.insert("File_Logs",null,cv));
        db.close();
    }

    /**
     * 接收新文件的时候更新FECParameters
     *
     *
     */
    public void UpdateFECParameters(byte[] FECParameters,int TotalSymbolNum){
        this.sqLiteDatabase = this.helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("FECParameters",FECParameters);
        cv.put("TotalSymbolNum",TotalSymbolNum);
        //String ID = String.valueOf(this.FileID);
        this.sqLiteDatabase.update("File_Logs",cv,"ID=?",new String[]{this.FileID});
        //db.close();
    }
    /*public void Update(Long commonFecOTI,int schemeSpecFecOTI){
        SQLiteDatabase db = this.helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("commonFecOTI",commonFecOTI);
        cv.put("schemeSpecFecOTI",schemeSpecFecOTI);
        //String ID = String.valueOf(this.FileID);
        db.update("File_Logs",cv,"ID=?",new String[]{this.FileID});
        db.close();
    }*/

    /**
     * 更新接收到的数据包。
     * 这里已经开始进行文件接收，理论上sqLiteDatabase不会再改变，
     *
     *//*
    public void UpdateReceivedSymbolNum(StringBuilder builder,int ReceivedNum){
    //String ReceivedSymbolNum = builder.toString();
    Object[] bindArray = new Object[]{builder.toString(),ReceivedNum,this.FileID};
    this.sqLiteDatabase.execSQL(sql,bindArray);
        //SQLiteStatement statement = new SQLiteStatement(this.sqLiteDatabase,sql,bindArray);
    }
    */
    public void SetDataBase(){
        this.sqLiteDatabase = this.helper.getWritableDatabase();
    }
    /**
    *配合UpdateReceivedSymbolNum使用
     */
    public void CloseSqLiteDatabase(){
        this.sqLiteDatabase.close();
    }

    /**
     * 文件未完成传输，但是本地文件已经被删除,或者手动删除历史纪录
     */
    public void DeleteFileLog(StringBuilder IDS ){
        SQLiteDatabase db = this.helper.getWritableDatabase();
        StringBuilder deletesql = new StringBuilder();
        deletesql.append("delete from File_Logs where ID IN(");
        deletesql.append(IDS);
        deletesql.append(")");
        db.rawQuery(deletesql.toString(),null);
        db.close();
    }
    public void DeleteEmptyFile(){//删除了数据库和本地文件的空文件，也删除了不在本地储存的数据库数据
        ArrayList<String> filenames = new ArrayList<String>();
        SQLiteDatabase db = this.helper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select FileName from File_Logs where FECParameters is null",null);
        while (cursor.moveToNext()){
            filenames.add(cursor.getString(0));
        }
        File[] files = this.DownLoadPath.listFiles();
       // LinkedList<String> Deletefile = new LinkedList<>();
        StringBuilder Deletefile = new StringBuilder();
        for (int i = 0;i<files.length-1;i++) {
            String Filename =files[i].getName();
            //Filename=
            Deletefile.append("'");
            Deletefile.append(Filename);
            for(String name:filenames) {

                if(name.equals(Filename)){
                    files[i].delete();
                    break;
                }
            }
            Deletefile.append("'");
            Deletefile.append(",");
        }
        Deletefile.append("'");
        Deletefile.append(files[files.length-1].getName());
        Deletefile.append("'");
        StringBuilder deletesql = new StringBuilder();
        deletesql.append("delete from File_Logs where FECParameters is null or FileName NOT IN(");
        deletesql.append(Deletefile.toString());
        deletesql.append(")");
        db.rawQuery(deletesql.toString(),null);
        cursor.close();
        db.close();
    }
    public void PickFile(int ID){
        this.sqLiteDatabase = helper.getWritableDatabase();
        this.FileID = String.valueOf(ID);
    }
    public void Complete(){
       // this.sqLiteDatabase.rawQuery("UPDATE File_Logs SET HasComplete= '1' WHERE ID=?",new String[]{this.FileID});
        ContentValues cv = new ContentValues();
       // cv.put("FECParameters",FECParameters);
        cv.put("HasComplete","1");
        this.sqLiteDatabase.update("File_Logs",cv,"ID=?",new String[]{this.FileID});
    }

    /**
     * 解压文件之后对数据库中的数据进行重命名
     */
    public void Changename(String name){
        ContentValues cv = new ContentValues();
        cv.put("FileName",name);
        this.sqLiteDatabase.update("File_Logs",cv,"ID=?",new String[]{this.FileID});
    }
        /*
    public void InsertNewFile(FileInfo fileInfo){
        SQLiteDatabase db = this.helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("FileName",fileInfo.getFileName());
        cv.put("");
        db.insert();
    }*/
}
