package com.littlewhite;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Button;

import com.littlewhite.ReceiveFile.SqllitUtil.SqllitData;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActivityOnlistener {
    private Button mBsend, mBreceive, mBsetting;
    private SqllitData sqllitData;

    //private Button FileExplorer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initPermission();
        this.sqllitData = new SqllitData(this);
        this.sqllitData.DeleteEmptyFile();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBsend = findViewById(R.id.send);
        mBreceive = findViewById(R.id.receive);
        mBsetting = findViewById(R.id.settings);
        setOnclickListener(mBsend, this);
        setOnclickListener(mBreceive, this);
        setOnclickListener(mBsetting, this);
        createDir();
    }

    private void createDir() {
        File sendPath = getExternalFilesDir("send");
        File receivePath = getExternalFilesDir("receive");
        if (!sendPath.exists()) {
            sendPath.mkdir();
        }
        if (!receivePath.exists()) {
            receivePath.mkdir();
        }

    }

    private void initPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> permissionList = new ArrayList<>();
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(Manifest.permission.CAMERA);
            }

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }

            if (!permissionList.isEmpty()) {
                ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), 1);
            } else {
            }
        }
    }

}
