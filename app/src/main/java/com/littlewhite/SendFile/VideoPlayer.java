package com.littlewhite.SendFile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

import com.littlewhite.R;

import java.io.File;

public class VideoPlayer extends Activity {

    VideoView videoView;
    MediaController mController;
    BrightUtil brightUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        setContentView(R.layout.activity_video_play);
        BrightUtil brightUtil = new BrightUtil(this);
        brightUtil.setScreenBrightness(255);
        // 获取界面上VideoView组件
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        WindowManager manager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point theScreenResolution = new Point();
        display.getSize(theScreenResolution);
        videoView = (VideoView) findViewById(R.id.video);
        ViewGroup.LayoutParams layoutParams = videoView.getLayoutParams();
        layoutParams.height = theScreenResolution.y;
        layoutParams.width = layoutParams.height ;
        videoView.setLayoutParams(layoutParams);
        //videoView.setH
        // 创建MediaController对象
        mController = new MediaController(this);
        //Enviornment.g
        Intent intent = getIntent();
        String a = intent.getStringExtra("videoPath");
       // Log.i("路径",intent.getStringExtra("videoPath"));
        File video = new File(a);
        // Log.i("路径", Environment.getExternalStorageDirectory()+"/夏洛克/神探夏洛克 第四季 03英语_1080p.mp4");
        if (video.exists()) {
            videoView.setVideoPath(video.getAbsolutePath());
            // 设置videoView和mController建立关联
            videoView.setMediaController(mController);
            // 设置mController和videoView建立关联
            mController.setMediaPlayer(videoView);

            // videoView.setL
            // 让VideoView获取焦点
            videoView.requestFocus();
            videoView.start();
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                        @Override
                        public boolean onInfo(MediaPlayer mp, int what, int extra) {
                            mp.start();
                            mp.setLooping(true);
                            if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                                //videoView.setBackgroundColor(Color.TRANSPARENT);
                            }
                            return true;
                        }
                    });
                }

            });
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        videoView.requestFocus();
        videoView.start();
    }
}












