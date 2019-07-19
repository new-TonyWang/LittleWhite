package com.littlewhite.ReceiveFile;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.littlewhite.Camera.CameraManager;
import com.littlewhite.Camera.ViewfinderView;
import com.littlewhite.Camera.newCameraManager;
import com.littlewhite.FileManager.FileManager;
import com.littlewhite.History.HistoryActivity;
import com.littlewhite.R;
import com.littlewhite.ReceiveFile.QRcodeDecoder.MultiDecoder;
import com.littlewhite.ReceiveFile.SqllitUtil.FileInfo;
import com.littlewhite.ReceiveFile.SqllitUtil.SqllitData;
import com.littlewhite.SendReceive;
import com.littlewhite.TransmissionCompleteActivity;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class ReceiveActivity extends SendReceive<ReceiveHandler> implements SurfaceHolder.Callback{
    private ViewfinderView viewfinderView;//扫描框
        private newCameraManager cameraManager;//相机管理
   // private ReceiveHandler receiveHandler;//处理消息
   // private MergeFileThread mergeFile;
   // private MultiDecoder multiDecoder;
    private int TotalQRnum;
    private SurfaceView surfaceView;
    private TextView progress;
    private boolean hasSurface = false;
    private boolean isautofocus = true;
    //private FileInfo fileHistory;
    //private final CountDownLatch handlerInitLatch = new CountDownLatch(1);
   // private final CountDownLatch handlerInitLatch = new CountDownLatch(1);;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_receive);

    }
    @Override
    protected void onResume() {
        super.onResume();
    surfaceView = findViewById(R.id.surfaceView);
    progress = findViewById(R.id.progress);
    //Intent intent = getIntent();
    cameraManager = new newCameraManager(getApplication());//getApplication()获取Application对象实例
       // handlerInitLatch = new CountDownLatch(1);
        viewfinderView = findViewById(R.id.viewfinder_view);//二维码识别框
        viewfinderView.setCameraManager(cameraManager);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        //surfaceHolder.addCallback(this);

        if (hasSurface) {
            // The activity was paused but not stopped, so the surface still exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(surfaceHolder);//重新打开app的时候用
        } else {
            // Install the callback and wait for surfaceCreated() to init the camera.
            surfaceHolder.addCallback(this);
        }
    }

    /**
     * 更新进度的时候用
     *
     * @param num
     */
    public void UpgradeProgress(int num){
    //progress.setText(getResources().getText(R.string.Progress));
    progress.setText(num+"/"+this.TotalQRnum);
    }

    public int getTotalQRnum() {
        return TotalQRnum;
    }

    public void setTotalQRnum(int totalQRnum) {
        TotalQRnum = totalQRnum;
    }
    @Override
   public ReceiveHandler getHandler(){

        return handler;
    }

    public void setReceiveHandler(ReceiveHandler receiveHandler) {
        this.handler = receiveHandler;
    }
    public newCameraManager getCameraManager() {
        return cameraManager;
    }

    public void setCameraManager(newCameraManager cameraManager) {
        this.cameraManager = cameraManager;
    }
    public void TransmissionComplete(Bundle fileBundle){
      //  receiveHandler.quitSynchronously();
        handler = null;
        Intent intent = new Intent(this, FileManager.class);
        intent.putExtra("dir",getExternalFilesDir("receive").getAbsolutePath());
        startActivity(intent);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {//首次初始化相机
            hasSurface = true;
            initCamera(holder);
        }

    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {hasSurface = false;

    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:

                return super.onKeyDown(keyCode,event);
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                switchAutoFocus();
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                switchAutoFocus();
                return true;
        }
        return false;
    }

    private void switchAutoFocus(){
        if(isautofocus){
            isautofocus = false;
            cameraManager.setAutoFocus();
            Toast inf = Toast.makeText(this,"锁定焦距",Toast.LENGTH_SHORT);
            showMyToast(inf,1000);
        }else{
            isautofocus = true;
            cameraManager.setAutoFocus();
            Toast inf = Toast.makeText(this,"自动对焦",Toast.LENGTH_SHORT);
            showMyToast(inf,1000);
        }
    }
    private void showMyToast(final Toast toast, final int cnt) {
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                toast.show();
            }
        }, 0, 1000);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                toast.cancel();
                timer.cancel();
            }
        }, cnt );
    }
    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            // Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        try {
            cameraManager.openDriver(surfaceHolder);
            // Creating the handler starts the preview, which can also throw a RuntimeException.
            if(handler==null) {
                Intent intent = getIntent();
                switch(intent.getIntExtra("iscolor",R.id.BW)) {
                    case R.id.BW:
                    this.handler = new ReceiveHandler(this, this.cameraManager,R.id.BW);//解码黑白
                        break;
                    case R.id.HSV:
                        this.handler = new ReceiveHandler(this, this.cameraManager,R.id.HSV);//解码HSV
                        break;
                    case R.id.RGB:
                        this.handler = new ReceiveHandler(this, this.cameraManager,R.id.RGB);//解码RGB
                        break;
                }

            }
           // decodeOrStoreSavedBitmap(null, null);
        } catch (IOException ioe) {
          //  Log.w(TAG, ioe);
            displayFrameworkBugMessageAndExit();
        } catch (RuntimeException e) {
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
           // Log.w(TAG, "Unexpected error initializing camera", e);
            displayFrameworkBugMessageAndExit();
        }
    }
    private void displayFrameworkBugMessageAndExit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.app_name));
       // builder.setMessage(getString(R.string.msg_camera_framework_bug));
       // builder.setPositiveButton(R.string.button_ok, new FinishListener(this));
       // builder.setOnCancelListener(new FinishListener(this));
        builder.show();
    }
    @Override
    protected void onPause() {
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
            //receiveHandler = null;//程序未退出的时候就不删除handler
        }

        cameraManager.closeDriver();
        //historyManager = null; // Keep for onActivityResult
        if (!hasSurface) {
           // SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.removeCallback(this);

        }
        super.onPause();
    }

    /**
     *
     * raptor解析所有数据包的时候调用，加上progressbar和文字提示。
     */
    public void RaptorCalculationStart(){
        this.progress.setVisibility(View.GONE);
        View view = findViewById(R.id.result_view);
        view.setVisibility(View.VISIBLE);
        ProgressBar progressBar = findViewById(R.id.progressBarReceive);
        progressBar.setVisibility(View.VISIBLE);

        TextView  textview = findViewById(R.id.ProgressTextView);
        textview.setText("正在解析文件......");

    }
}
