package com.google.zxing;

public class ColorYUV extends LuminanceSource {
  private static final int THUMBNAIL_SCALE_FACTOR = 2;

  private final byte[] yuvData;
  private final int dataWidth;

  public int getDataWidth() {
    return dataWidth;
  }

  public int getDataHeight() {
    return dataHeight;
  }


  private final int dataHeight;
  private final int left;
  private final int top;


  public ColorYUV(byte[] yuvData,
                  int dataWidth,
                  int dataHeight,
                  int left,
                  int top,
                  int width,
                  int height,
                  boolean reverseHorizontal) {
    super(width, height);

    if (left + width > dataWidth || top + height > dataHeight) {
      throw new IllegalArgumentException("Crop rectangle does not fit within image data.");
    }

    this.yuvData = yuvData;
    this.dataWidth = dataWidth;
    this.dataHeight = dataHeight;
    this.left = left;
    this.top = top;
    if (reverseHorizontal) {
      reverseHorizontal(width, height);
    }
  }
  @Override
  public byte[] getRow(int y, byte[] row) {
    if (y < 0 || y >= getHeight()) {
      throw new IllegalArgumentException("Requested row is outside the image: " + y);
    }
    int width = getWidth();
    if (row == null || row.length < width) {
      row = new byte[width];
    }
    int offset = (y + top) * dataWidth + left;
    System.arraycopy(yuvData, offset, row, 0, width);
    return row;
  }

  public byte[] getMatrix() {
    int width = getWidth();
    int height = getHeight();
    // If the caller asks for the entire underlying image, save the copy and give them the
    // original data. The docs specifically warn that result.length must be ignored.
    int area = width * height;
    byte[] matrix = new byte[area];
    int inputOffset = top * dataWidth + left;//inputOffset是源数组起始位置，可以用于获取扫描框的位置
    // Otherwise copy one cropped row at a time.
    for (int y = 0; y < height; y++) {
      int outputOffset = y * width;
      System.arraycopy(yuvData, inputOffset, matrix, outputOffset, width);
      inputOffset += dataWidth;
    }
    return matrix;
  }

  /**
   * 获取uv数组
   * @return
   */
  public byte[] getUVMatrix() {
    int width = getWidth();
    int height = getHeight();
    // If the caller asks for the entire underlying image, save the copy and give them the
    // original data. The docs specifically warn that result.length must be ignored.
    int index = this.dataWidth * this.dataHeight;
    int area = width*height/2;
    int len = height/2;
    //int end = (int)Math.ceil(height/2);
    byte[] matrix = new byte[area];
    int inputoffset = top*this.dataWidth/2+index+left;
    int outputOffset ;
    for(int i = 0;i<len;i++){
      outputOffset = i*width;
      System.arraycopy(yuvData,inputoffset,matrix,outputOffset,width);
      inputoffset+=this.dataWidth;
    }

    return matrix;
  }
  private void reverseHorizontal(int width, int height) {
    byte[] yuvData = this.yuvData;
    for (int y = 0, rowStart = top * dataWidth + left; y < height; y++, rowStart += dataWidth) {
      int middle = rowStart + width / 2;
      for (int x1 = rowStart, x2 = rowStart + width - 1; x1 < middle; x1++, x2--) {
        byte temp = yuvData[x1];
        yuvData[x1] = yuvData[x2];
        yuvData[x2] = temp;
      }
    }
  }

}
