package com.littlewhite.SendFile;

import android.media.MediaScannerConnection;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.littlewhite.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FFMPEGHandler extends Handler {
    static {
        System.loadLibrary("jxffmpegrun");
        System.loadLibrary("avcodec");
        System.loadLibrary("avformat");
        System.loadLibrary("avutil");
        System.loadLibrary("swscale");
        System.loadLibrary("fdk-aac");
    }

    public FFMPEGHandler(SendFileActivity activity) {

        this.sendFileActivity = activity;
        //this.mediaScannerConnection  =  new MediaScannerConnection(this.sendFileActivity, null);

    }

    private SendFileActivity sendFileActivity;
    private int width;
    private int height;
    private int frameRate ;
    private StringBuilder mp4Name;
    private String pureMp4Name;

    //调用connect


    //private SendConfigs sendConfigs;
   // private final String TAG = "文件目录";
    //private String

    @Override
    public void handleMessage(Message message) {
        switch (message.what) {
            case R.id.Init:
                initFFmpeg((SendConfigs) message.obj);
                break;
            case R.id.GenerateVideo:
                this.sendFileActivity.getSendFileHandler().sendEmptyMessage(R.id.FFmpegPhase);
                runffmepg( (FFmpegInOutPath) message.obj);
                break;
            case R.id.finish:
                Looper.myLooper().quit();
                break;
            case R.id.failed:
                Looper.myLooper().quit();
                break;

        }
    }
    private void initFFmpeg(SendConfigs sendConfigs){
        this.width = sendConfigs.getWidth();
        this.height = sendConfigs.getHeight();
        this.frameRate = sendConfigs.getFPS();
        StringBuilder FileName = new StringBuilder(sendConfigs.getPath().substring(0, sendConfigs.getPath().lastIndexOf(".")));
        this.pureMp4Name  = FileName.substring(FileName.lastIndexOf("/"),FileName.length())+".mp4";


        //合成视频名称，和待发送文件在同一个文件夹。
    };
    private int jxFFmpegCMDRun(String cmd) {
        //String regulation = "[ \\t]+";
        final String[] split = cmd.split(" ");
        int a = split.length;
        for (String aSplit : split) {
            Log.i("", aSplit);
        }
        return ffmpegRun(split);
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

    public void runffmepg(FFmpegInOutPath dirPath){
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
        this.mp4Name = new StringBuilder(dirPath.getOutPath()+"/"+pureMp4Name);
        StringBuilder cmd = new StringBuilder("ffmpeg -framerate ");
        cmd.append(this.frameRate);//设置帧率
        cmd.append(" -i ");
       // cmd.append("'");
        cmd.append(dirPath.getInPath());
        cmd.append("/%0d.jpg");
        cmd.append(" -vcodec libx264 -s ");
        cmd.append(this.width);
        cmd.append("*");
        cmd.append(this.height);
        cmd.append(" ");
        cmd.append("-y ");
        //cmd.append(" ");
        //cmd.append("'");
        cmd.append(this.mp4Name);
        //cmd.append("'");
        //cmd.append("/out.mp4");
       // cmd.append("/sdcard/Android/data/com.example.littlewhite/files/send/Downloadblu1557504523669/out.mp4");
                //String basePath = Environment.getExternalStorageDirectory().getPath();
                //String cmd_transcoding = "ffmpeg -i "+ basePath + "/girl.mp4 -r 30 -q:v 2 "+ basePath + "/output/image%0d.jpg";
                //String cmd_transcoding = "ffmpeg -i "+basePath+"/girl.mp4 -r 60 "+ basePath+"/output/image%0d.jpg";
                //int i = jxFFmpegCMDRun(cmd_transcoding);//ffmpeg -i girl.mp4 -r 30 -q:v 2 ./output/image%0d.jpg
               // String cmd_transcoding2 = "ffmpeg -f concat -r 20 -i "+dirpath+"/input.txt -vcodec libx264 -s 450*450 "+dirpath+"/output.mp4";
                //String  = "ffmpeg -i "+ dirpath+"/image";
                //StringBuffer cmd_transcoding2 = new StringBuffer("ffmpeg -r 20");
                    /*for(int i = 1;i<=finalLoop;i++){
                    cmd_transcoding2.append(" -i "+dirpath+"/image"+i+".jpg -i "+path+"/image0000.jpg");
                    }
                    cmd_transcoding2.append(" -vcodec libx264 "+dirpath+"/output3.mp4");
                    String cmd = cmd_transcoding2.toString();
                     */
               // Log.i(TAG,cmd.toString());
                int i2 = jxFFmpegCMDRun(cmd.toString());
        AlbumNotifier albumNotifier = new AlbumNotifier();
        albumNotifier.insertVideoToMediaStore(this.sendFileActivity,this.mp4Name.toString(),System.currentTimeMillis(),width,height,0);
                Message message = obtainMessage(R.id.finish,this.mp4Name.toString());

               this.sendFileActivity.getSendFileHandler().sendMessage(message);
        //Log.i(TAG,"成功");
    }
    public native int ffmpegRun(String[] cmd);
}
