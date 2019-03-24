package net.fec.openrq;

import net.fec.openrq.parameters.FECParameters;
import sun.plugin2.message.Message;

import java.beans.FeatureDescriptor;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

public class receivetest implements Runnable{
    private ArrayDataDecoder Decoder;
    private FECParameters FECParameters;
    private Boolean isrunning;
    private LinkedBlockingQueue<byte[]> linkedBlockingQueue;
    private FileOutputStream outputStream  ;
    private int num;
    private int sum;
   // private byte[] data;
    public receivetest(LinkedBlockingQueue<byte[]> linkedBlockingQueue,boolean isrunning){
        this.linkedBlockingQueue = linkedBlockingQueue;

       synchronized (this.linkedBlockingQueue) {
           this.isrunning = true;
         /*  try {
               this.linkedBlockingQueue.wait();
           } catch (InterruptedException e) {
               e.printStackTrace();
           }*/
         byte[] FEC = new byte[12];
         System.arraycopy(this.linkedBlockingQueue.poll(),0,FEC,0,12);
           this.FECParameters = net.fec.openrq.parameters.FECParameters.parse(FEC).value();//閸掆晝鏁ata閻㈢喐鍨氶崣鍌涙殶

       } this.Decoder = OpenRQ.newDecoderWithTwoOverhead(FECParameters);
     int capality = (FECParameters.symbolSize());//濮ｅ繋閲滈崸妤冩畱鐎瑰綊鍣�
        System.out.println("numberOfSourceBlocks()"+FECParameters.numberOfSourceBlocks());//
      this.sum =    (FECParameters.totalSymbols());//缂佸嫭鍨氶弬鍥︽閻ㄥ嫬娼￠弫甯磼闁插秷顩﹂敍锟�
        String randomFileName = UUID.randomUUID().toString();
        File output  = new File("D:\\workspace\\idea\\OpenRQ\\src\\"+randomFileName+".zip");
        try {
            output.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            this.outputStream = new FileOutputStream(output);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void run() {
       while(true) {
           synchronized (this.linkedBlockingQueue) {
                   EncodingPacket encodingPacket = null;
                   /*try {
                      // this.linkedBlockingQueue.wait();
                   } catch (InterruptedException e) {
                       e.printStackTrace();
                   }*/
                   byte[] symbol = this.linkedBlockingQueue.poll();
                   encodingPacket = this.Decoder.parsePacket(symbol, 12,(symbol.length-12),true).value();
                   System.out.println("encodingPacket.encodingSymbolID():"+encodingPacket.encodingSymbolID());//娑擄拷閻╂潙褰夐崠鏍电礉瀹歌尙绮＄憴锝嗙�介惃鍕嚋閺侊拷
                 //  System.out.println(encodingPacket.sourceBlockNumber());//
                   Decoder.sourceBlock(encodingPacket.sourceBlockNumber()).putEncodingPacket(encodingPacket);
              // Decoder.sourceBlock(encodingPacket.sourceBlockNumber());
                  this.num++;
                if (this.num==this.sum+2){
                    byte[] data = this.Decoder.dataArray();
                    try {
                        outputStream.write(data);
                        outputStream.close();
                        isrunning = false;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }
               //閸掆晝鏁ata閻㈢喐鍨氶弬鍥︽
           }
       }
    }
}
