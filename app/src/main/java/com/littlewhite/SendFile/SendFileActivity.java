package com.littlewhite.SendFile;

import android.content.Context;
import android.nfc.Tag;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.littlewhite.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Date;

public class SendFileActivity extends AppCompatActivity {




        private ProgressBar pb;
        private Button bt;

        private Context context;
       private String path ;//mnt/sdcard/Android/data/< package name >/files/

        private Button mBFileSelection;
    private TextView FilePathTV;
    private EditText widthEdit;
    private EditText heightEdit;
    private Spinner FPS;
    private Spinner ErrorCorrectionLevel;
    private SendFileHandler sendFileHandler;

    //  private spin
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_file);

            //pb = (ProgressBar) findViewById(R.id.pb);
            //bt = findViewById(R.id.bt);
        this.sendFileHandler = new SendFileHandler();
            String apppath = this.getExternalFilesDir(null).getPath();
            path=apppath;
          // String dirpath = makedir(path);//获取文件夹路径
            //通过zxing生成无数张二维码图片并存放在dirpath并且返回生成文件的数量
           // int number = zxingcreation(dirpath);
            //写入配置文件文件
           // writetxt(dirpath,number);
            //利用ffmpeg生成视频
           // runffmepg(dirpath,number);
        }
        /*
        public void onClick(View v) {
            pb.setVisibility(View.VISIBLE);
            bt.setVisibility(View.INVISIBLE);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String basePath = Environment.getExternalStorageDirectory().getPath();
                    //String cmd_transcoding = "ffmpeg -i "+ basePath + "/girl.mp4 -r 30 -q:v 2 "+ basePath + "/output/image%0d.jpg";
                    //String cmd_transcoding = "ffmpeg -i "+basePath+"/girl.mp4 -r 60 "+ basePath+"/output/image%0d.jpg";
                    //int i = jxFFmpegCMDRun(cmd_transcoding);//ffmpeg -i girl.mp4 -r 30 -q:v 2 ./output/image%0d.jpg
                    String cmd_transcoding2 = "ffmpeg -r 60 -i "+basePath+"/output/image%0d.jpg -vcodec libx264 "+ basePath+"/output.mp4";
                    int i2 = jxFFmpegCMDRun(cmd_transcoding2);
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            pb.setVisibility(View.GONE);
                            Toast.makeText(MainActivity.this, "ok了", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).start();
        }
*/

    }

