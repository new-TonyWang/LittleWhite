package com.littlewhite.Camera;


import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;

import com.google.zxing.ColorYUV;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.littlewhite.Camera.open.OpenCamera;
import com.littlewhite.Camera.open.OpenCameraInterface;

import java.io.IOException;


public class newCameraManager {

  /**
   * 让获取的解码框的宽高为偶数，方便截取UV通道
   *
   */
  @SuppressWarnings("deprecation") // camera APIs


    private static final String TAG = newCameraManager.class.getSimpleName();

    private static final int MIN_FRAME_WIDTH = 240;
    private static final int MIN_FRAME_HEIGHT = 240;
    private static final int MAX_FRAME_WIDTH = 1200; // = 5/8 * 1920
    private static final int MAX_FRAME_HEIGHT = 674; // = 5/8 * 1080-1

  private final Context context;
  private final CameraConfigurationManager configManager;
  private OpenCamera camera;
  private AutoFocusManager autoFocusManager;
  private Rect framingRect;
  private Rect framingRectInPreview;
  private boolean initialized;
  private boolean previewing;
  private int requestedCameraId = OpenCameraInterface.NO_REQUESTED_CAMERA;
  private int requestedFramingRectWidth;
  private int requestedFramingRectHeight;
    /**
     * Preview frames are delivered here, which we pass on to the registered handler. Make sure to
     * clear the handler so it will only receive one message.
     */
    private final PreviewCallback previewCallback;

    public newCameraManager(Context context) {
      this.context = context;
      this.configManager = new CameraConfigurationManager(context);
      previewCallback = new PreviewCallback(configManager);
    }

    /**
     * Opens the camera driver and initializes the hardware parameters.
     *
     * @param holder The surface object which the camera will draw preview frames into.
     * @throws IOException Indicates the camera driver failed to open.
     */
    public synchronized void openDriver(SurfaceHolder holder) throws IOException {
      OpenCamera theCamera = camera;
      if (theCamera == null) {
        theCamera = OpenCameraInterface.open(requestedCameraId);
        if (theCamera == null) {
          throw new IOException("Camera.open() failed to return object from driver");
        }
        camera = theCamera;
      }

      if (!initialized) {
        initialized = true;
        configManager.initFromCameraParameters(theCamera);
        if (requestedFramingRectWidth > 0 && requestedFramingRectHeight > 0) {
          setManualFramingRect(requestedFramingRectWidth, requestedFramingRectHeight);
          requestedFramingRectWidth = 0;
          requestedFramingRectHeight = 0;
        }
      }

      Camera cameraObject = theCamera.getCamera();
      Camera.Parameters parameters = cameraObject.getParameters();
      String parametersFlattened = parameters == null ? null : parameters.flatten(); // Save these, temporarily
      try {
        configManager.setDesiredCameraParameters(theCamera, false);
      } catch (RuntimeException re) {
        // Driver failed
        Log.w(TAG, "Camera rejected parameters. Setting only minimal safe-mode parameters");
        Log.i(TAG, "Resetting to saved camera params: " + parametersFlattened);
        // Reset:
        if (parametersFlattened != null) {
          parameters = cameraObject.getParameters();
          parameters.unflatten(parametersFlattened);
          try {
            cameraObject.setParameters(parameters);
            configManager.setDesiredCameraParameters(theCamera, true);
          } catch (RuntimeException re2) {
            // Well, darn. Give up
            Log.w(TAG, "Camera rejected even safe-mode parameters! No configuration");
          }
        }
      }
      cameraObject.setPreviewDisplay(holder);

    }

    public synchronized boolean isOpen() {
      return camera != null;
    }

    /**
     * Closes the camera driver if still in use.
     */
    public synchronized void closeDriver() {
      if (camera != null) {
        camera.getCamera().release();
        camera = null;
        // Make sure to clear these each time we close the camera, so that any scanning rect
        // requested by intent is forgotten.
        framingRect = null;
        framingRectInPreview = null;
      }
    }

    /**
     * Asks the camera hardware to begin drawing preview frames to the screen.
     */
    public synchronized void startPreview() {
      OpenCamera theCamera = camera;
      if (theCamera != null && !previewing) {
        theCamera.getCamera().startPreview();
        previewing = true;
        autoFocusManager = new AutoFocusManager(context, theCamera.getCamera());
        Point cameraResolution = configManager.getCameraResolution();
        theCamera.getCamera().addCallbackBuffer(new byte[((cameraResolution.x * cameraResolution.y) * ImageFormat.getBitsPerPixel(ImageFormat.NV21)) / 8]);
        theCamera.getCamera().addCallbackBuffer(new byte[((cameraResolution.x * cameraResolution.y) * ImageFormat.getBitsPerPixel(ImageFormat.NV21)) / 8]);
        theCamera.getCamera().setPreviewCallbackWithBuffer(previewCallback);
        //autoFocusManager.start();
      }
    }

    /**
     * Tells the camera to stop drawing preview frames.
     */
    public synchronized void stopPreview() {
      if (autoFocusManager != null) {
        autoFocusManager.stop();
        autoFocusManager = null;
      }
      if (camera != null && previewing) {
        camera.getCamera().stopPreview();
        previewCallback.setHandler(null, 0);
        previewing = false;
      }
    }

    /**
     * Convenience method for
     *
     * @param newSetting if {@code true}, light should be turned on if currently off. And vice versa.
     */
    public synchronized void setTorch(boolean newSetting) {
      OpenCamera theCamera = camera;
      if (theCamera != null && newSetting != configManager.getTorchState(theCamera.getCamera())) {
        boolean wasAutoFocusManager = autoFocusManager != null;
        if (wasAutoFocusManager) {
          autoFocusManager.stop();
          autoFocusManager = null;
        }
        configManager.setTorch(theCamera.getCamera(), newSetting);
        if (wasAutoFocusManager) {
          autoFocusManager = new AutoFocusManager(context, theCamera.getCamera());
          autoFocusManager.start();
        }
      }
    }
  public synchronized void setAutoFocus() {
    OpenCamera theCamera = camera;
    if (theCamera != null) {
      boolean wasAutoFocusManager = autoFocusManager != null;
      if (wasAutoFocusManager) {
        autoFocusManager.stop();
        autoFocusManager = null;
      }
      else {
        autoFocusManager = new AutoFocusManager(context, theCamera.getCamera());
        autoFocusManager.start();
      }
    }
  }
    /**
     * A single preview frame will be returned to the handler supplied. The data will arrive as byte[]
     * in the message.obj field, with width and height encoded as message.arg1 and message.arg2,
     * respectively.
     *
     * @param handler The handler to send the message to.
     * @param message The what field of the message to be sent.
     */
    public synchronized void requestPreviewFrame(Handler handler, int message) {
      OpenCamera theCamera = camera;
      if (theCamera != null && previewing) {
        previewCallback.setHandler(handler, message);
        previewCallback.setResolution();
        theCamera.getCamera().setOneShotPreviewCallback(previewCallback);
      }
    }

  /**
   * 开启预览，即开启
   * @param handler
   * @param message
   */
  public synchronized void requestPreviewFrameWithBuffer(Handler handler, int message) {
    OpenCamera theCamera = camera;
    if (theCamera != null && previewing) {
      previewCallback.setHandler(handler, message);
      previewCallback.setResolution();

    }
  }
    /**
     * Calculates the framing rect which the UI should draw to show the user where to place the
     * barcode. This target helps with alignment as well as forces the user to hold the device
     * far enough away to ensure the image will be in focus.
     *
     * @return The rectangle to draw on screen in window coordinates.
     */
    public synchronized Rect getFramingRect() {
      if (framingRect == null) {
        if (camera == null) {
          return null;
        }
        Point screenResolution = configManager.getScreenResolution();
        if (screenResolution == null) {
          // Called early, before init even finished
          return null;
        }


        int height = findBiggestDimensionInRange(screenResolution.y, MIN_FRAME_HEIGHT, MAX_FRAME_HEIGHT);
        //int width = findDesiredDimensionInRange(screenResolution.x, MIN_FRAME_WIDTH, MAX_FRAME_WIDTH);
         // width = (width&1)+width;
          height = (height&1)+height;
        int width =height;
        int leftOffset = ((screenResolution.x - width) >> 1)- (((screenResolution.x - width)>>1)& 1);
        int topOffset = ((screenResolution.y - height) >> 1)-(((screenResolution.y - height)>>1) & 1);
        //强行设置为偶数
        framingRect = new Rect(leftOffset, topOffset, leftOffset + width, topOffset + height);
        Log.d(TAG, "Calculated framing rect: " + framingRect);
      }
      return framingRect;
    }

    private static int findDesiredDimensionInRange(int resolution, int hardMin, int hardMax) {
      int dim = 9 * resolution / 10; // Target 5/8 of each dimension
      if (dim < hardMin) {
        return hardMin;
      }
      if (dim > hardMax) {
        return hardMax;
      }
      return dim;
    }

  private static int findBiggestDimensionInRange(int resolution, int hardMin, int hardMax) {//不设置最大值
    int dim = 9 * resolution / 10; // Target 9/10 of each dimension
    if (dim < hardMin) {
      return hardMin;
    }
    return dim;
  }
    /**
     * Like {@link #getFramingRect} but coordinates are in terms of the preview frame,
     * not UI / screen.
     *
     * @return {@link Rect} expressing barcode scan area in terms of the preview size
     */
    public synchronized Rect getFramingRectInPreview() {
      if (framingRectInPreview == null) {
        Rect framingRect = getFramingRect();
        if (framingRect == null) {
          return null;
        }
        Rect rect = new Rect(framingRect);
        Point cameraResolution = configManager.getCameraResolution();
        Point screenResolution = configManager.getScreenResolution();
        if (cameraResolution == null || screenResolution == null) {
          // Called early, before init even finished
          return null;
        }

        //rect.left = rect.left * cameraResolution.x / screenResolution.x;
        //rect.right = rect.right * cameraResolution.x / screenResolution.x;
        //rect.top = rect.top * cameraResolution.y / screenResolution.y;
      //  rect.bottom = rect.bottom * cameraResolution.y / screenResolution.y;

        rect.left = rect.left * cameraResolution.x / screenResolution.x-((rect.left * cameraResolution.x / screenResolution.x)&1);
        rect.right = rect.right * cameraResolution.x / screenResolution.x-(( rect.right * cameraResolution.x / screenResolution.x)&1);
        rect.top = rect.top * cameraResolution.y / screenResolution.y-((rect.top * cameraResolution.y / screenResolution.y)&1);
        rect.bottom = rect.bottom * cameraResolution.y / screenResolution.y-((rect.bottom * cameraResolution.y / screenResolution.y)&1);
        //强行设置为偶数
        framingRectInPreview = rect;
      }
      return framingRectInPreview;
    }


    /**
     * Allows third party apps to specify the camera ID, rather than determine
     * it automatically based on available cameras and their orientation.
     *
     * @param cameraId camera ID of the camera to use. A negative value means "no preference".
     */
    public synchronized void setManualCameraId(int cameraId) {
      requestedCameraId = cameraId;
    }

    /**
     * Allows third party apps to specify the scanning rectangle dimensions, rather than determine
     * them automatically based on screen resolution.
     *
     * @param width The width in pixels to scan.
     * @param height The height in pixels to scan.
     */
    public synchronized void setManualFramingRect(int width, int height) {
      if (initialized) {
        Point screenResolution = configManager.getScreenResolution();
        if (width > screenResolution.x) {
          width = screenResolution.x;
        }
        if (height > screenResolution.y) {
          height = screenResolution.y;
        }
        int leftOffset = (screenResolution.x - width) / 2;
        int topOffset = (screenResolution.y - height) / 2;
        framingRect = new Rect(leftOffset, topOffset, leftOffset + width, topOffset + height);
        Log.d(TAG, "Calculated manual framing rect: " + framingRect);
        framingRectInPreview = null;
      } else {
        requestedFramingRectWidth = width;
        requestedFramingRectHeight = height;
      }
    }

    /**
     * A factory method to build the appropriate LuminanceSource object based on the format
     * of the preview buffers, as described by Camera.Parameters.
     *
     * @param data A preview frame.
     * @param width The width of the image.
     * @param height The height of the image.
     * @return A PlanarYUVLuminanceSource instance.
     */
    public PlanarYUVLuminanceSource buildPlanarYUVSource(byte[] data, int width, int height) {
      Rect rect = getFramingRectInPreview();//需要获取扫描边框位置
      if (rect == null) {
        return null;
      }
      // Go ahead and assume it's YUV rather than die.
      return new PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top,
        rect.width(), rect.height(), false);
    }

  public ColorYUV buildColorYUVSource(byte[] data, int width, int height) {
    Rect rect = getFramingRectInPreview();//需要获取扫描边框位置
    if (rect == null) {
      return null;
    }
    // Go ahead and assume it's YUV rather than die.
    return new ColorYUV(data, width, height, rect.left, rect.top,
            rect.width(), rect.height(), false);
  }
  }
