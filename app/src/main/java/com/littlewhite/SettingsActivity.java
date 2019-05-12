package com.littlewhite;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;

public class SettingsActivity extends AppCompatActivity implements  View.OnClickListener {

    private Button button;
    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        //Toolbar toolbar = findViewById(R.id.toolbar);
        button = findViewById(R.id.button);
        setOnclickListener(button,this);
       // setSupportActionBar(toolbar);
    }

    protected void onResume() {
        super.onResume();

    }
    @Override
    public void onClick(View v) {
            v.setEnabled(false);
            switch (v.getId()){
                case R.id.button:
                    DeleteFiles();
                    break;
//                case R.id.fab:
//                    Snackbar.make(v, "Replace with your own action", Snackbar.LENGTH_LONG)
//                            .setAction("Action", null).show();
//                    break;
            }
        v.setEnabled(true);
    }
 private void DeleteFiles(){
        File path = new File(this.getExternalFilesDir("send").getAbsolutePath());
     deleteFile(path);
        path.mkdir();
 }
    /**
     * 先根遍历序递归删除文件夹
     *
     * @param dirFile 要被删除的文件或者目录
     * @return 删除成功返回true, 否则返回false
     */
    public  boolean deleteFile(File dirFile) {
        // 如果dir对应的文件不存在，则退出
        if (!dirFile.exists()) {
            return false;
        }

        if (dirFile.isFile()) {
            return dirFile.delete();
        } else {

            for (File file : dirFile.listFiles()) {
                deleteFile(file);
            }
        }

        return dirFile.delete();
    }

    /**
     * 设置监听器()
     * @param v
     * @param listener
     */
    private void setOnclickListener(View v,View.OnClickListener listener){
        v.setOnClickListener(listener);
    }

}
