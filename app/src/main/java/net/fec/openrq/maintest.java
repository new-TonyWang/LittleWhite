package net.fec.openrq;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.LinkedBlockingQueue;

public class maintest {
    public static void main(String args[]){
        LinkedBlockingQueue<byte[]> linkedBlockingQueue = new LinkedBlockingQueue<byte[]>();
        Boolean isrunning  = true;
        ZipUtil zipUtil = new ZipUtil();
        Path path = null;
        try {
         path =    zipUtil.zipDirectory("D:\\workspace\\idea\\OpenRQ\\src\\image0.png");
        } catch (IOException e) {
            e.printStackTrace();
        }
        sendtest sendtest = new sendtest(linkedBlockingQueue,isrunning,path);
        new Thread(sendtest).start();
        receivetest receivetest = new receivetest(linkedBlockingQueue,isrunning);

        new Thread(receivetest).start();
    }
}
