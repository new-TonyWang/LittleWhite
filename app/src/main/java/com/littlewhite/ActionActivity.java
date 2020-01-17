package com.littlewhite;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

import java.util.ArrayList;

public class ActionActivity extends ActivityOnlistener {
    private Button mBreceive,mBreceiveColor,mBreceiveRGB,readfilebutton,pureQRCOdeDecode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action);
        mBreceive = findViewById(R.id.receiveBlack);
        mBreceiveColor = findViewById(R.id.receiveColor);
        mBreceiveRGB = findViewById(R.id.receiveRGB);
        readfilebutton = findViewById(R.id.decodeQRfromPic);
        pureQRCOdeDecode = findViewById(R.id.pureQRCOdeDecode);
        list.add(mBreceive);
        list.add(mBreceiveColor);
        list.add(mBreceiveRGB);
        list.add(readfilebutton);
        list.add(pureQRCOdeDecode);
        setOnclickListener(mBreceive,this);
        setOnclickListener(mBreceiveColor,this);
        setOnclickListener(mBreceiveRGB,this);
        setOnclickListener(readfilebutton,this);
        setOnclickListener(pureQRCOdeDecode,this);
    }
    @Override
    protected void onResume() {
        super.onResume();
    }
}
