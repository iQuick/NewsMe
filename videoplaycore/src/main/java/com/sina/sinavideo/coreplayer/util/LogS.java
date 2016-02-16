package com.sina.sinavideo.coreplayer.util;

import android.util.Log;

import com.sina.sinavideo.coreplayer.Constants;

/**
 * 日志类<br>
 * NOTE: 日志部分需要更改为统一模块。。。，后期考虑修改
 * 
 * @author jiantian1
 */
public class LogS {

	/**
	 * 打印debug级别的日志
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 * @return The number of bytes written.
	 */
	public static int d(String tag, String msg) {
		if (Constants.DEBUG) {
			if (msg == null) {
				msg = "null";
			}
			return Log.d(tag, msg);
		}
		return 0;
	}

	public static int i(String tag, String msg) {
		if (Constants.DEBUG) {
			if (msg == null) {
				msg = "null";
			}
			return Log.i(tag, msg);
		}
		return 0;
	}

	public static int v(String tag, String msg) {
		if (Constants.DEBUG) {
			if (msg == null) {
				msg = "null";
			}
			return Log.v(tag, msg);
		}
		return 0;
	}

	/**
	 * 打印debug级别的日志
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 * @param tr
	 *            An exception to log
	 * @return The number of bytes written.
	 */
	public static int d(String tag, String msg, Throwable tr) {
		if (Constants.DEBUG) {
			if (msg == null) {
				msg = "null";
			}
			return Log.d(tag, msg, tr);
		}
		return 0;
	}

	public static int i(String tag, String msg, Throwable tr) {
		if (Constants.DEBUG) {
			if (msg == null) {
				msg = "null";
			}
			return Log.i(tag, msg, tr);
		}
		return 0;
	}

	public static int w(String tag, String msg) {
		if (Constants.DEBUG) {
			if (msg == null) {
				msg = "null";
			}
			return Log.w(tag, msg);
		}
		return 0;
	}

	public static int w(String tag, String msg, Throwable tr) {
		if (Constants.DEBUG) {
			if (msg == null) {
				msg = "null";
			}
			return Log.w(tag, msg, tr);
		}
		return 0;
	}

	public static int e(String tag, String msg) {
		FileLog.write("E", tag, msg);
		if (Constants.DEBUG) {
			if (msg == null) {
				msg = "null";
			}
			return Log.e(tag, msg);
		}
		return 0;
	}

	public static int e(String tag, String msg, Throwable tr) {
		FileLog.write("E", tag, msg);
		if (Constants.DEBUG) {
			if (msg == null) {
				msg = "null";
			}
			return Log.e(tag, msg, tr);
		}
		return 0;
	}

}
