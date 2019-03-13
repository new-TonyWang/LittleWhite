package com.littlewhite;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

public class ActionActivity extends ActivityOnlistener {
    private Button mBsend,mBreceive;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action);
        mBsend = findViewById(R.id.send);
        mBreceive = findViewById(R.id.receive);
        setOnclickListener(mBsend,this);
        setOnclickListener(mBreceive,this);
    }
    @Override
    protected void onResume() {
        super.onResume();
    }
}
