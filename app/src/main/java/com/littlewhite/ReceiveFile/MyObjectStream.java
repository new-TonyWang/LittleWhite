package com.littlewhite.ReceiveFile;

import java.io.*;
import java.util.concurrent.LinkedBlockingQueue;

/**

 * 使用自定义的方式,来实现追加写入object和读取

 */

public class MyObjectStream extends ObjectOutputStream {



    public MyObjectStream(OutputStream out) throws IOException {

        super(out);// 会调用writeStreamHeader()

    }



    /**

     * 覆盖父类的方法,使其在已有对象信息并追加时,不写header信息

     * 查看源码会发现:writeStreamHeader方法会写入以下两行内容:

     *

     *  bout.writeShort(STREAM_MAGIC);

     *  bout.writeShort(STREAM_VERSION);

     *

     *  这两行对应的值:

     *  final static short STREAM_MAGIC = (short)0xaced;

     *  final static short STREAM_VERSION = 5;

     *

     *  在文件中头部就会写入:AC ED 00 05

     *  一个文件对象只有在文件头出应该出现此信息,文件内容中不能出现此信息,否则会导致读取错误

     *  所以在追加时,就需要覆盖父类的writeStreamHeader方法,执行reset()方法

     *

     *  reset()方法写入的是这个:final static byte TC_RESET =        (byte)0x79;

     * @throws IOException

     */

    @Override

    protected void writeStreamHeader() throws IOException {

        super.reset();

    }



    public static ObjectOutputStream newInstance(File file)  {



        //long length = file.length();

        ObjectOutputStream oos = null;
        try {
        if(file.length() == 0) {


                oos = new ObjectOutputStream(new FileOutputStream(file,true));


        } else {

            oos = new MyObjectStream(new FileOutputStream(file,true));

        }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return oos;

    }


    public <T>void writeObject (T t, File file)  {

        ObjectOutputStream oos = null;

        try {

            oos = MyObjectStream.newInstance(file);

            oos.writeObject(t);

            oos.flush();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }
    public void CloseStream(MyObjectStream myOutPutStream){
        try {
            myOutPutStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

