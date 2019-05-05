package com.littlewhite.SendFile;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class SendFileHandler extends Handler {
    static {
        System.loadLibrary("jxffmpegrun");
        System.loadLibrary("avcodec");
        System.loadLibrary("avformat");
        System.loadLibrary("avutil");
        System.loadLibrary("swscale");
        System.loadLibrary("fdk-aac");
    }
    private SendFileActivity activity;
    private final String TAG = "文件目录";
    //private String


    public int jxFFmpegCMDRun(String cmd) {
        //String regulation = "[ \\t]+";
        final String[] split = cmd.split(" ");
        int a = split.length;
        for(int i=0;i<a;i++){
            Log.i("",split[i]);}
        return ffmpegRun(split);
    }
    public String makedir(String path){//生成存放二维码的目录，同时返回目录路径
        // String  dirpath = path+"/"+new Date().getTime();
        String dirpath = path+"/1";
        File file = null;
        try {
            file = new File(dirpath);
            if (!file.exists()) {
                file.mkdir();
            }else{

                //提示文件夹已存在，是否覆写
            }
        } catch (Exception e) {
            Log.i("error:", e+"");
        }

        Log.i(TAG,path);
        return dirpath;
    }
    public void writetxt(String dirpath,int i){//创建fffmpeg配置文件,传入路径和文件数量
        File targetfile = new File(dirpath+"/input.txt");
        // InputStream is=getResources().openRawResource(R.mipmap.qrcode2);
        try {
            if(targetfile.exists()){
                targetfile.delete();
                File targetfilenew = new File(dirpath+"/input.txt");
                targetfile=targetfilenew;
            };
            //指定文件追加内容
            RandomAccessFile raf = new RandomAccessFile(targetfile,"rw");
            for(int j=1;j<i+1;j++) {
                raf.seek(targetfile.length());
                raf.write(("file 'image" + j + ".jpg'\r\nduration 20\r\nfile '../image0000.jpg'\r\nduration 1\r\n").getBytes());
            }
            raf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int zxingcreation(String dirpath){//利用zxing生成二维码并且返回二维码数量
        //判断需要生成几张二维码
        //生成二维码并且命名image+i.jpg
        return 5;
    }

    public void runffmepg(final String dirpath, int loop){
        //public void onClick(View v) {
       // pb.setVisibility(View.VISIBLE);
       // bt.setVisibility(View.INVISIBLE);
            /*if(loop<1){return;}
            if(loop<10){//zxing生成的图片如果小于10则多循环几次播放
                int a = 10/loop;
                if(a>=2&&loop!=5){a=3;}
                loop = a;
            }*/
       // final int finalLoop = loop;

        new Thread(new Runnable() {
            @Override
            public void run() {
                //String basePath = Environment.getExternalStorageDirectory().getPath();
                //String cmd_transcoding = "ffmpeg -i "+ basePath + "/girl.mp4 -r 30 -q:v 2 "+ basePath + "/output/image%0d.jpg";
                //String cmd_transcoding = "ffmpeg -i "+basePath+"/girl.mp4 -r 60 "+ basePath+"/output/image%0d.jpg";
                //int i = jxFFmpegCMDRun(cmd_transcoding);//ffmpeg -i girl.mp4 -r 30 -q:v 2 ./output/image%0d.jpg
                String cmd_transcoding2 = "ffmpeg -f concat -r 20 -i "+dirpath+"/input.txt -vcodec libx264 -s 450*450 "+dirpath+"/output.mp4";
                //String  = "ffmpeg -i "+ dirpath+"/image";
                //StringBuffer cmd_transcoding2 = new StringBuffer("ffmpeg -r 20");
                    /*for(int i = 1;i<=finalLoop;i++){
                    cmd_transcoding2.append(" -i "+dirpath+"/image"+i+".jpg -i "+path+"/image0000.jpg");
                    }
                    cmd_transcoding2.append(" -vcodec libx264 "+dirpath+"/output3.mp4");
                    String cmd = cmd_transcoding2.toString();
                     */
                Log.i(TAG,cmd_transcoding2);
                int i2 = jxFFmpegCMDRun(cmd_transcoding2);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                       // pb.setVisibility(View.GONE);
                       // Toast.makeText(SendFileActivity.this, "ok了", Toast.LENGTH_SHORT).show();
                        Log.i(TAG,"成功");
                    }
                });
            }
        }).start();
    }
    public native int ffmpegRun(String[] cmd);

}
