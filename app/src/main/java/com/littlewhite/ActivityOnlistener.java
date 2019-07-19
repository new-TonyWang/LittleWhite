package com.littlewhite;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.littlewhite.FileManager.FileManager;
import com.littlewhite.ReceiveFile.ReceiveActivity;
import com.littlewhite.ReceiveFile.SqllitUtil.SqllitData;
import com.littlewhite.SendFile.SendFileActivity;

import java.io.File;
import java.util.ArrayList;

public class ActivityOnlistener extends AppCompatActivity implements View.OnClickListener {
    //private SqllitData sqllitData = new SqllitData(this);
    protected  ArrayList<Button> list = new ArrayList<>();
    protected static final int REQUEST_CODE_GET_FILE_PATH = 1;
    protected static final int FILE_SELECT_CODE = 0;
    @Override
    public void onClick(View v) {
        setButtonsNotEnable();
        switch(v.getId()){
            case R.id.settings:
               Intent settings = new Intent(this,SettingsActivity.class);//跳转到设置界面
              startActivity(settings);
                //Intent settings = new Intent(this, FileManager.class);//跳转到设置界面
               // startActivity(settings);
                break;
            case R.id.receive:
                Intent action = new Intent(this,ActionActivity.class);
                //action.putExtras();
                startActivity(action);
                break;
            case R.id.send:
                Intent sendselect = new Intent(this, SendFileSelection.class);
                startActivity(sendselect);
                break;
            case R.id.newStart:
                Intent send = new Intent(this, SendFileActivity.class);
                startActivity(send);
                break;
            case R.id.fromVideo:
                getFilePath(REQUEST_CODE_GET_FILE_PATH,"video/*");
                break;
            case R.id.receiveBlack:
                Intent receive = new Intent(this, ReceiveActivity.class);
                receive.putExtra("iscolor",R.id.BW);
                startActivity(receive);
                break;
            case R.id.receiveColor:
                Intent receiveColor = new Intent(this, ReceiveActivity.class);
                receiveColor.putExtra("iscolor",R.id.HSV);
                startActivity(receiveColor);
                //Intent receive = new Intent(this, ReceiveActivity.class);
                //startActivity(receive);
                break;
            case R.id.receiveRGB:
                Intent receiveRGB = new Intent(this, ReceiveActivity.class);
                receiveRGB.putExtra("iscolor",R.id.RGB);
                startActivity(receiveRGB);
                //Intent receive = new Intent(this, ReceiveActivity.class);
                //startActivity(receive);
                break;
            case R.id.FileSelection:
                getFilePath(REQUEST_CODE_GET_FILE_PATH,"*/*");
               // Intent receive = new Intent(this, ReceiveActivity.class);
                //startActivity(receive);
                break;

        }
        setButtonsEnable();
    }

    /**
     * 设置监听器()
     * @param v
     * @param listener
     */
    protected void setOnclickListener(View v,View.OnClickListener listener){
        v.setOnClickListener(listener);
    }

    protected void setButtonsNotEnable(){
        for(int i = 0;i<this.list.size();i++){
            list.get(i).setEnabled(false);
        }
    }
    protected void setButtonsEnable(){
        for(int i = 0;i<this.list.size();i++){
            list.get(i).setEnabled(true);
        }
    }
    private void getFilePath(int requestCode,String Type) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(Type);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(Intent.createChooser(intent, "Select a File"), requestCode);
//            Intent intent2 = new Intent(Intent.ACTION_VIEW);
//            Uri uri = Uri.fromFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
//            //intent2.addCategory(Intent.CATEGORY_DEFAULT);
//            intent2.setData(uri);
//            startActivity(intent);
        } else {
            new AlertDialog.Builder(this).setTitle("未找到文件管理器")
                    .setMessage("请安装文件管理器以选择文件")
                    .setPositiveButton("确定", null)
                    .show();
        }
    }
}
