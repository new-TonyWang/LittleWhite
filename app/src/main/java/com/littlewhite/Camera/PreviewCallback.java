/*
 * Copyright (C) 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.littlewhite.Camera;

import android.graphics.Point;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

@SuppressWarnings("deprecation") // camera APIs
final class PreviewCallback implements Camera.PreviewCallback {

  private static final String TAG = PreviewCallback.class.getSimpleName();

  private final CameraConfigurationManager configManager;
  private Handler previewHandler;
  private int previewMessage;
  private int x ;
  private int y;
  int i = 0;
  PreviewCallback(CameraConfigurationManager configManager) {
    this.configManager = configManager;
  }

  void setHandler(Handler previewHandler, int previewMessage) {
    this.previewHandler = previewHandler;
    this.previewMessage = previewMessage;
  }
  void setResolution(){
    Point cameraResolution = configManager.getCameraResolution();
    this.x = cameraResolution.x;
    this.y = cameraResolution.y;
  }
  @Override
  public void onPreviewFrame(byte[] data, Camera camera) {
   // Log.i(TAG,"第"+i+++"个");
    //Handler thePreviewHandler = previewHandler;
    if ( previewHandler != null) {
     // Log.i(TAG, "预览分辨率x:"+this.x+"预览分辨率y:"+this.y);
      //分辨率是1920*1080
      Message message = previewHandler.obtainMessage(previewMessage, this.x,
          this.y, data);
      message.sendToTarget();
      //Log.i(TAG,"onPreviewFrame启动");

    } else {
      Log.d(TAG, "Got preview callback, but no handler");
    }
    camera.addCallbackBuffer(data);
   // camera.addCallbackBuffer(data);
  }
}
