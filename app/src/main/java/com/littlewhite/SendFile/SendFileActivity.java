package com.littlewhite.SendFile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.littlewhite.ActivityOnlistener;
import com.littlewhite.MainActivity;
import com.littlewhite.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class SendFileActivity extends ActivityOnlistener {


    private ProgressBar pb;
    private Button bt;
    private Context context;
    private String path = null;//mnt/sdcard/Android/data/< package name >/files/
    private Button mBFileSelection;
    private TextView FilePathTV;
    private int width;
    private int height;
    private int FPS;
    private EditText widthEdit;
    private EditText heightEdit;
    private EditText FPSEdit;
    private EditText QRCodeCapacityEdit;
    private SendFileHandler sendFileHandler;
    private int QRCodeType;
    private int QRCodeCapacity;
    private Spinner ErrorCorrectionLevelSelection;
    private Spinner QRCodeTypeSelection;
    private String ErrorCorrectionLevel;
    private Button VideoGeneration;
    private ArrayAdapter<CharSequence> adapter;
    private boolean haschange = false;
    // private List<CharSequence> List = new ArrayList<CharSequence>();

    public SendFileHandler getSendFileHandler() {
        return sendFileHandler;
    }

    //  private spin
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_file);
        //pb = (ProgressBar) findViewById(R.id.pb);
        //bt = findViewById(R.id.bt);

        this.sendFileHandler = new SendFileHandler(this);


            // String apppath = this.getExternalFilesDir(null).getPath();
        // path = apppath;
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

    @Override
    protected void onResume() {
        super.onResume();
        this.mBFileSelection = findViewById(R.id.FileSelection);
        this.FilePathTV = findViewById(R.id.Filepath);
        this.widthEdit = findViewById(R.id.widthEdit);
        this.width = Integer.valueOf(widthEdit.getText().toString());
        this.heightEdit = findViewById(R.id.heightEdit);
        this.height = Integer.valueOf(heightEdit.getText().toString());
        this.FPSEdit = findViewById(R.id.FPS);
        this.FPS = Integer.valueOf(FPSEdit.getText().toString());
        this.VideoGeneration = findViewById(R.id.VideoGeneration);
        this.QRCodeCapacityEdit = findViewById(R.id.QRCodeCapacity);
        this.QRCodeCapacity = Integer.valueOf(QRCodeCapacityEdit.getText().toString());
        ErrorCorrectionLevelSelection = findViewById(R.id.ErrorCorrectionLevelSelection);
        QRCodeTypeSelection = findViewById(R.id.QRCodeType);
        ErrorCorrectionLevel = "L";
        QRCodeType = 0;
        adapter = ArrayAdapter.createFromResource(this, R.array.Error_Correction_Level, android.R.layout.simple_spinner_item);//创建spinner的适配器
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ErrorCorrectionLevelSelection.setAdapter(adapter);
        ArrayAdapter<CharSequence> adapterQRCodeType = ArrayAdapter.createFromResource(this, R.array.QRCodeType, android.R.layout.simple_spinner_item);
        adapterQRCodeType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        QRCodeTypeSelection.setAdapter(adapterQRCodeType);
        initSpanner(ErrorCorrectionLevelSelection);
        initSpanner(QRCodeTypeSelection);
        setOnclickListener(mBFileSelection, this);
        setOnclickListener(VideoGeneration, this);
        setEditTextListener(widthEdit);
        setEditTextListener(heightEdit);
        setEditTextListener(FPSEdit);
        setEditTextListener(QRCodeCapacityEdit);
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null /*&& "video/mp4".equals(type)*/)
        {
            Uri uri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
            //如果是媒体类型需要从数据库获取路径
            File chosenFile =new File(getRealPathFromURI(uri));
            if (chosenFile.length() < 1048576) {//文件必须小于1MB，不然手会累死的
                this.path = chosenFile.getAbsolutePath();
                FilePathTV.setText(path + "\n 文件大小:" + chosenFile.length() + "B");
                FilePathTV.setTextColor(0xFF868585);
            } else {
                path = null;
                FilePathTV.setText("文件必须小于1MiB");
                FilePathTV.setTextColor(Color.rgb(255, 0, 0));
                Toast.makeText(this, "文件必须小于1MiB", Toast.LENGTH_SHORT).show();
            }
            //pathTextView.setText("文件路径:"+filePath);
        }
    }

    private void initSpanner(final Spinner spinner) {//设置监听

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            //当AdapterView中的item被选中的时候执行的方法。
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {//id返回的是所选的位置
                switch (parent.getId()) {
                    case R.id.ErrorCorrectionLevelSelection:
                        ErrorCorrectionLevel = (String) adapter.getItem(position);
                        break;
                    case R.id.QRCodeType:
                        QRCodeType = (int) id;//0为黑白，1为彩色
                        break;
                }
                checkConfig();
                // tv.setText(adapterXML.getItem(position));
                // Log.i("QRCodeType:",adapterQRCodeType.getItem(position)+"");//可以成功显示
                // Log.i("id",id+"");//可以成功显示
            }

            @Override    //未选中时的时候执行的方法
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }


    private void setEditTextListener(final EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                //text  输入框中改变后的字符串信息
                //start 输入框中改变后的字符串的起始位置
                //before 输入框中改变前的字符串的位置 默认为0
                //count 输入框中改变后的一共输入字符串的数量

            }

            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {
                //text  输入框中改变前的字符串信息
                //start 输入框中改变前的字符串的起始位置
                //count 输入框中改变前后的字符串改变数量一般为0
                //after 输入框中改变后的字符串与起始位置的偏移量

            }

            @Override
            public void afterTextChanged(Editable edit) {
                //edit  输入结束呈现在输入框中的信息
                editText.removeTextChangedListener(this);
                checkConfig();
                editText.addTextChangedListener(this);

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.FileSelection:
                super.onClick(v);
                break;
            case R.id.VideoGeneration:
                this.FPS = Integer.valueOf(FPSEdit.getText().toString());
                this.width = Integer.valueOf(widthEdit.getText().toString());
                this.height = Integer.valueOf(heightEdit.getText().toString());
                this.QRCodeCapacity = Integer.valueOf(QRCodeCapacityEdit.getText().toString());
                SendConfigs Configs = new SendConfigs(path,FPS,width,height,QRCodeType,ErrorCorrectionLevel,QRCodeCapacity);
                this.mBFileSelection.setClickable(false);
                this.VideoGeneration.setClickable(false);
                FrameLayout frameLayout = findViewById(R.id.grayCover);
                ProgressBar progressBar = findViewById(R.id.progressBar);
                frameLayout.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                setCanNotEditNoClick(this.FPSEdit);
                setCanNotEditNoClick(this.widthEdit);
                setCanNotEditNoClick(this.heightEdit);
                setCanNotEditNoClick(this.QRCodeCapacityEdit);
                stopSpanner(this.ErrorCorrectionLevelSelection);
                stopSpanner(this.QRCodeTypeSelection);
                //StartRaptorEncode();
                this.sendFileHandler.StartProgress(Configs);

        }
    }

    private void OnFocusChangeListener(final EditText editText) {
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focus) {
                //Watcher watcher = new Watcher();
                if (focus) {
                    //editText.addTextChangedListener(watcher);
                } else {
                    //editText.removeTextChangedListener(watcher);
                }
            }

        });
    }
    public void setCanNotEditNoClick(View v) {
        v.setFocusable(false);
        v.setFocusableInTouchMode(false);
        // 如果之前没设置过点击事件，该处可省略
        v.setOnClickListener(null);
    }
    public void stopSpanner(AdapterView v) {
        v.setFocusable(false);
        v.setFocusableInTouchMode(false);
       // v.removeAllViewsInLayout();
        v.setAdapter(null);

        // 如果之前没设置过点击事件，该处可省略
        //v.setOnItemClickListener(null);
    }

    /*
    private class Watcher implements TextWatcher{

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            checkConfig();
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    }*/
    private void getFilePath(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(Intent.createChooser(intent, "Select a File"), requestCode);
        } else {
            new AlertDialog.Builder(this).setTitle("未找到文件管理器")
                    .setMessage("请安装文件管理器以选择文件")
                    .setPositiveButton("确定", null)
                    .show();
        }
    }
    public void playVideo(String videoPath){
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        TextView phase = findViewById(R.id.phase);
        phase.setText("开始视频播放");
        Intent intent  = new Intent(this,VideoPlayer.class);
        intent.putExtra("videoPath",videoPath);
        startActivity(intent);

    }
    public void StartRaptorEncode(){
            TextView phase = findViewById(R.id.phase);
        phase.setVisibility(View.VISIBLE);
            phase.setText("正在生成二维码序列......");
       // adapter.clear();

    }
    public void StartFFmpeg(){
        TextView phase = findViewById(R.id.phase);
        phase.setText("正在生成视频......");

    }
    //String path;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            if ("file".equalsIgnoreCase(uri.getScheme())) {//使用第三方应用打开
                path = uri.getPath();
                File chosenFile = new File(path);
                if (chosenFile.length() < 1048576) {//文件必须小于1MB，不然手会累死的
                    FilePathTV.setText(path + "\n 文件大小:" + chosenFile.length() + "B");
                    FilePathTV.setTextColor(0xFF868585);
                } else {
                    path = null;
                    FilePathTV.setText("文件必须小于1MiB");
                    FilePathTV.setTextColor(Color.rgb(255, 0, 0));
                    Toast.makeText(this, "文件必须小于1MiB", Toast.LENGTH_SHORT).show();
                }
                // Toast.makeText(this,path+"11111",Toast.LENGTH_SHORT).show();
                return;
            }
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {//4.4以后
                path = getPath(this, uri);
                //FilePathTV.setText(path);
                //Toast.makeText(this,path,Toast.LENGTH_SHORT).show();
            } else {//4.4以下下系统调用方法
                path = getRealPathFromURI(uri);
                //FilePathTV.setText(path);
                //Toast.makeText(this, path+"222222", Toast.LENGTH_SHORT).show();
            }
            File chosenFile = new File(path);
            if (chosenFile.length() < 1048576) {//文件必须小于1MB，不然手会累死的
                FilePathTV.setText(path + "\n 文件大小:" + chosenFile.length() + "B");
                FilePathTV.setTextColor(0xFF868585);
            } else {
                path = null;
                FilePathTV.setText("文件必须小于1MiB");
                FilePathTV.setTextColor(Color.rgb(255, 0, 0));
                Toast.makeText(this, "文件必须小于1MiB", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public String getRealPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (null != cursor && cursor.moveToFirst()) {
            ;
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
            cursor.close();
        }
        return res;
    }

    /**
     * 专为Android4.4设计的从Uri获取文件绝对路径，以前的方法已不好使
     */
    @SuppressLint("NewApi")
    public String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public String getDataColumn(Context context, Uri uri, String selection,
                                String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private boolean checkConfig() {
        if (this.path != null) {
            this.FPS = Integer.valueOf(FPSEdit.getText().toString().equals("") ? "0" : FPSEdit.getText().toString());
            this.width = Integer.valueOf(widthEdit.getText().toString().equals("") ? "0" : widthEdit.getText().toString());
            this.height = Integer.valueOf(heightEdit.getText().toString().equals("") ? "0" : heightEdit.getText().toString());
            this.QRCodeCapacity = Integer.valueOf(QRCodeCapacityEdit.getText().toString().equals("") ? "0" : QRCodeCapacityEdit.getText().toString());
            if (this.FPS > 25 || this.FPS == 0) {
                this.VideoGeneration.setClickable(false);
                //this.FPSEdit.setText(R.string.FPS_Default);
                Toast.makeText(this, "视频帧率必须在1-25之间", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (this.width < 100
                    || this.height < 100) {
                this.VideoGeneration.setClickable(false);
                //widthEdit.setText(R.string.height_width_default);
                //heightEdit.setText(R.string.height_width_default);
                Toast.makeText(this, "视频分辨率必须大于100", Toast.LENGTH_SHORT).show();
                return false;
            }
            switch (this.QRCodeType) {

                case 0://黑白
                    switch (ErrorCorrectionLevel) {
                        case "L":
                            if (this.QRCodeCapacity > 1935
                                    || this.QRCodeCapacity < 21) {

                                this.VideoGeneration.setClickable(false);
                                //  QRCodeCapacityEdit.setText(R.string.black_white_QRCode_capacity_default);
                                Toast.makeText(this, "L级别纠错的时候黑白二维码容量必须在21-1935之间", Toast.LENGTH_SHORT).show();
                                return false;
                            }
                            break;
                        case "M":
                            if (this.QRCodeCapacity > 1705
                                    || this.QRCodeCapacity < 21) {

                                this.VideoGeneration.setClickable(false);
                                // QRCodeCapacityEdit.setText(Integer.toString(1750));
                                Toast.makeText(this, "M级别纠错的时候黑白二维码容量必须在21-1705之间", Toast.LENGTH_SHORT).show();
                                return false;
                            }
                            break;
                        case "Q":
                            if (this.QRCodeCapacity > 1211
                                    || this.QRCodeCapacity < 21) {

                                this.VideoGeneration.setClickable(false);
                                // QRCodeCapacityEdit.setText(Integer.toString(1211));
                                Toast.makeText(this, "Q级别纠错的时候黑白二维码容量必须在21-1211之间", Toast.LENGTH_SHORT).show();
                                return false;
                            }
                            break;
                        case "H":
                            if (this.QRCodeCapacity > 941
                                    || this.QRCodeCapacity < 21) {

                                this.VideoGeneration.setClickable(false);
                                //QRCodeCapacityEdit.setText(Integer.toString(941));
                                Toast.makeText(this, "H级别纠错的时候黑白二维码容量必须在21-941之间", Toast.LENGTH_SHORT).show();
                                return false;
                            }
                            break;
                    }
                    break;
                //this.VideoGeneration.setClickable(true);
                //return  true;

                case 1://彩色
                    switch (ErrorCorrectionLevel) {
                        case "L":
                            if (this.QRCodeCapacity > 1747
                                    || this.QRCodeCapacity < 21) {

                                this.VideoGeneration.setClickable(false);
                                // QRCodeCapacityEdit.setText(R.string.ColorCode_capacity_default);
                                Toast.makeText(this, "L级别纠错的时候彩色二维码容量必须在21-1747之间", Toast.LENGTH_SHORT).show();
                                return false;
                            }
                            break;
                        case "M":
                            if (this.QRCodeCapacity > 1339
                                    || this.QRCodeCapacity < 21) {

                                this.VideoGeneration.setClickable(false);
                                // QRCodeCapacityEdit.setText(Integer.toString(1339));
                                Toast.makeText(this, "M级别纠错的时候彩色二维码容量必须在21-1339之间", Toast.LENGTH_SHORT).show();
                                return false;
                            }

                            break;
                        case "Q":
                            if (this.QRCodeCapacity > 955
                                    || this.QRCodeCapacity < 21) {

                                this.VideoGeneration.setClickable(false);
                                // QRCodeCapacityEdit.setText(Integer.toString(955));
                                Toast.makeText(this, "Q级别纠错的时候彩色二维码容量必须在21-955之间", Toast.LENGTH_SHORT).show();
                                return false;
                            }
                            break;
                        case "H":
                            if (this.QRCodeCapacity > 739
                                    || this.QRCodeCapacity < 21) {

                                this.VideoGeneration.setClickable(false);
                                //QRCodeCapacityEdit.setText(Integer.toString(739));
                                Toast.makeText(this, "H级别纠错的时候彩色二维码容量必须在21-739之间", Toast.LENGTH_SHORT).show();
                                return false;
                            }
                            break;
                    }
                    break;

            }


            this.VideoGeneration.setClickable(true);
            return true;

        }
        this.VideoGeneration.setClickable(false);
        return false;
    }


}


