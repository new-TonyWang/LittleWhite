package com.littlewhite.ReceiveFile.SqllitUtil;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class SqllitData {
    private SqllitHelper helper;
    private String FileID;
    private SQLiteDatabase sqLiteDatabase;
    private final String sql = "UPDATE File_log SET ReceivedSymbolNum=? WHERE ID=?";
    public SqllitData(Context context){
        String dbname = "LittleWhite.db";
        helper = new SqllitHelper(context,dbname,null,1);
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
                    cursor.getString(3),
                    cursor.getLong(4),
                    cursor.getInt(5),
                    cursor.getInt(6)
            );
            fileInfos.add(fileInfo);
        }
        db.close();
        return fileInfos;
    }
    public void InsertNewFile(FileInfo fileInfo){
        SQLiteDatabase db = this.helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("FileName",fileInfo.getFileName());
        cv.put("HasComplete",false);
        //cv.put("TotalSymbolNum",fileInfo.getTotalSymbolNum());
        //cv.put("ReceivedSymbolNum",null);
        this.FileID = String.valueOf(db.insert("File_Logs",null,cv));

        db.close();
    }

    /**
     * 接收新文件的时候更新FECParameters
     * @param commonFecOTI
     * @param schemeSpecFecOTI
     */
    public void UpdateFECParameters(Long commonFecOTI,int schemeSpecFecOTI,int TotalSymbolNum){
        SQLiteDatabase db = this.helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("TotalSymbolNum",TotalSymbolNum);
        cv.put("commonFecOTI",commonFecOTI);
        cv.put("schemeSpecFecOTI",schemeSpecFecOTI);
        //String ID = String.valueOf(this.FileID);
        db.update("File_Logs",cv,"ID=?",new String[]{this.FileID});
        db.close();
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
     * @param builder
     */
    public void UpdateReceivedSymbolNum(StringBuilder builder){
    //String ReceivedSymbolNum = builder.toString();
    Object[] bindArray = new Object[]{builder.toString(),this.FileID};
    this.sqLiteDatabase.execSQL(sql,bindArray);
        //SQLiteStatement statement = new SQLiteStatement(this.sqLiteDatabase,sql,bindArray);
    }
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
    public void DeleteFileLog(int ID){
        SQLiteDatabase db = this.helper.getWritableDatabase();
        db.delete("File_Log","ID=?",new String[] {String.valueOf(ID)});
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
