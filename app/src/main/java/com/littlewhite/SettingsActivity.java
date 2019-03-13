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

public class SettingsActivity extends AppCompatActivity implements  View.OnClickListener {
    private TextView textView;
    private Button button;
    private EditText editText;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        button = findViewById(R.id.button);
        textView = findViewById(R.id.textView);
        editText = findViewById(R.id.editText);
        setSupportActionBar(toolbar);



    }

    protected void onResume() {
        super.onResume();
        sharedPreferences = getSharedPreferences("data",MODE_PRIVATE);//设置轻量级存储
        editor = sharedPreferences.edit();
        setOnclickListener(button,this);
        FloatingActionButton fab = findViewById(R.id.fab);
        setOnclickListener(fab,this);
        textView.setText(sharedPreferences.getString("name","null"));
    }
    @Override
    public void onClick(View v) {
            v.setEnabled(false);
            switch (v.getId()){
                case R.id.button:
                    String text = editText.getText().toString();
                    editor.putString("name",text);
                    editor.apply();
                    String output = sharedPreferences.getString("name","null");
                    textView.setText(output);
                    break;
                case R.id.fab:
                    Snackbar.make(v, "Replace with your own action", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    break;
            }
        v.setEnabled(true);
    }

    /**
     * 设置监听器(传说中的代码复用??)
     * @param v
     * @param listener
     */
    private void setOnclickListener(View v,View.OnClickListener listener){
        v.setOnClickListener(listener);
    }

}
