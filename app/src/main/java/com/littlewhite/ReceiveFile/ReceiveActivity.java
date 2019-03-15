package com.littlewhite.ReceiveFile;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.littlewhite.Camera.CameraManager;
import com.littlewhite.Camera.ViewfinderView;
import com.littlewhite.R;
import com.littlewhite.TransmissionCompleteActivity;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class ReceiveActivity extends AppCompatActivity implements SurfaceHolder.Callback{
    private ViewfinderView viewfinderView;//扫描框
        private CameraManager cameraManager;//相机管理
    private ReceiveHandler receiveHandler;//处理消息
    private MergeFileThread mergeFile;
    private MultiDecoder multiDecoder;
    private int TotalQRnum;
    private SurfaceView surfaceView;
    private TextView progress;
    private boolean hasSurface = false;
    //private final CountDownLatch handlerInitLatch = new CountDownLatch(1);
   // private final CountDownLatch handlerInitLatch = new CountDownLatch(1);;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_receive);

    }
    @Override
    protected void onResume() {
        super.onResume();
    surfaceView = findViewById(R.id.surfaceView);
    progress = findViewById(R.id.progress);
    cameraManager = new CameraManager(getApplication());//getApplication()获取Application对象实例
       // handlerInitLatch = new CountDownLatch(1);
        viewfinderView = findViewById(R.id.viewfinder_view);//二维码识别框
        viewfinderView.setCameraManager(cameraManager);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        if (hasSurface) {
            // The activity was paused but not stopped, so the surface still exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(surfaceHolder);
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
    public ReceiveHandler getReceiveHandler() {

        return receiveHandler;
    }

    public void setReceiveHandler(ReceiveHandler receiveHandler) {
        this.receiveHandler = receiveHandler;
    }
    public CameraManager getCameraManager() {
        return cameraManager;
    }

    public void setCameraManager(CameraManager cameraManager) {
        this.cameraManager = cameraManager;
    }
    public void TransmissionComplete(){
        Intent intent = new Intent(this, TransmissionCompleteActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("sum",this.TotalQRnum);
        intent.putExtras(bundle);
        startActivity(intent);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        initCamera(holder);

    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

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
            if(receiveHandler==null) {
                this.receiveHandler = new ReceiveHandler(this, this.cameraManager);
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
}
