package com.littlewhite.ReceiveFile.SqllitUtil;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static android.content.ContentValues.TAG;

public class SqllitHelper extends SQLiteOpenHelper {
    //private SQLiteDatabase database;
    //private boolean HasCreated;
    public SqllitHelper( Context context,  String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table File_Logs(ID INTEGER primary key AUTOINCREMENT,FileName varchar(30) NOT NULL,TotalSymbolNum Integer,ReceivedNum Integer,FECParameters blob,HasComplete boolean)";
//输出创建数据库的日志信息
        Log.i(TAG, "create Database------------->");
//execSQL函数用于执行SQL语句
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //输出更新数据库的日志信息
        Log.i(TAG, "update Database------------->");
    }

}
