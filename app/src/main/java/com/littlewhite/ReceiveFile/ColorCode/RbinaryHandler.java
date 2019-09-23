package com.littlewhite.ReceiveFile.ColorCode;

import android.os.Handler;
import android.os.Message;

import com.littlewhite.R;
import com.littlewhite.ReceiveFile.ReceiveActivity;

public class RbinaryHandler extends RGBbinaryHandler {

    public RbinaryHandler(Handler handler, ReceiveActivity receiveActivity) {
        super(handler, receiveActivity);
    }

    @Override
    public void handleMessage(Message message) {
            switch(message.what){
                case R.id.Binarize:
                    RGBChannel Channel= (RGBChannel) message.obj;
                   // RGBmatrixChannel = getRGBData(Channel.getData(),Channel.getChannel(),message.arg1,message.arg2,Channel.getWidth(),Channel.getHeight(),Channel.getTimemillis());
                    RGBmatrixChannel rgBmatrixChannel = getRGBData(Channel.getData(),Channel.getChannel(),
                            message.arg1,message.arg2,Channel.getWidth(),Channel.getHeight(),Channel.getTimemillis());
                    send_to_QRDecoder(rgBmatrixChannel);
                    break;
                case R.id.finish:
                    send_to_finish();
                    break;
            }
    }
}
