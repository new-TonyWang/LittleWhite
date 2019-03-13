package com.google.zxing;
/**
 * 非文件传输码异常
 * @author 我的电脑
 *
 */
public class NotDataException extends Exception {

	  private static final NotDataException INSTANCE = new NotDataException();
	  static {
	   // INSTANCE.setStackTrace(NO_TRACE); // since it's meaningless
	  }

	  private NotDataException() {
	    // do nothing
	  }

	  public static NotDataException getNotDataInstance() {
	    return INSTANCE;
	  }

}
