package com.littlewhite;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

import java.util.ArrayList;

public class ActionActivity extends ActivityOnlistener {
    private Button mBreceive,mBreceiveColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action);
        mBreceive = findViewById(R.id.receiveBlack);
        mBreceiveColor = findViewById(R.id.receiveColor);
        list.add(mBreceive);
        list.add(mBreceiveColor);
        setOnclickListener(mBreceive,this);
        setOnclickListener(mBreceiveColor,this);
    }
    @Override
    protected void onResume() {
        super.onResume();
    }
}
