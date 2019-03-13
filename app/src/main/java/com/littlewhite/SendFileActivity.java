package com.littlewhite;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class SendFileActivity extends AppCompatActivity {
    private Button mBFileSelection;
    private TextView FilePathTV;
    private EditText widthEdit;
    private EditText heightEdit;
    private Spinner FPS;
    private Spinner ErrorCorrectionLevel;

  //  private spin
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_file);

    }
}
