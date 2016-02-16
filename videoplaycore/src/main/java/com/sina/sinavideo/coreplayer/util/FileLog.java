package com.sina.sinavideo.coreplayer.util;

public class FileLog {

	public static final String TAG = "FileLog";
	public static LogWriteListener mListener;

	public interface LogWriteListener {

		public void writeLog(String type, String tag, String content);
	}

	public static void setLogListener(LogWriteListener listener) {
		mListener = listener;
	}

	public static void write(String type, String tag, String content) {
		if (mListener == null) {
			return;
		}
		mListener.writeLog(type, tag, content);
	}
}