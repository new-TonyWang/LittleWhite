package com.littlewhite;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends ActivityOnlistener {
    private Button mBaction,mBhistory,mBsetting;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBaction = findViewById(R.id.action);
        mBhistory = findViewById(R.id.history);
        mBsetting = findViewById(R.id.settings);
        setOnclickListener(mBaction,this);
        setOnclickListener(mBhistory,this);
        setOnclickListener(mBsetting,this);
    }


}
