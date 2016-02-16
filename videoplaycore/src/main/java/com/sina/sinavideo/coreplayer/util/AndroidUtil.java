package com.sina.sinavideo.coreplayer.util;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.WindowManager;

public class AndroidUtil {

	private static final String TAG = "AndroidUtil";
	/** 剩余空间 **/
	private static final long SURPLUS_AVAIL_SIZE = 100 * 1024 * 1024;// 100M
	public static final long MIN_PLAYSDK_SOTROAGRE_SIZE = 30 * 1000 * 1000;

	/**
	 * 获取本地Ip地址
	 * 
	 * @param context
	 * @return
	 */
	public static String getLocalIpAddress(Context context) {
		WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		if (wifiInfo != null && wifiInfo.getIpAddress() != 0) {
			return android.text.format.Formatter.formatIpAddress(wifiInfo
					.getIpAddress());
		} else {
			try {
				Enumeration<NetworkInterface> en = NetworkInterface
						.getNetworkInterfaces();
				while (en.hasMoreElements()) {
					NetworkInterface intf = en.nextElement();
					Enumeration<InetAddress> enumIpAddr = intf
							.getInetAddresses();
					while (enumIpAddr.hasMoreElements()) {
						InetAddress inetAddress = enumIpAddr.nextElement();
						if (!inetAddress.isLoopbackAddress()
								&& inetAddress.getHostAddress().indexOf(":") == -1) {
							String ipAddress = inetAddress.getHostAddress();
							if (!TextUtils.isEmpty(ipAddress)
									&& !ipAddress.contains(":")) {
								return ipAddress;
							}
						}
					}
				}
			} catch (SocketException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * Cmwap网络是否已连接
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNetworkConnectedByCmwap(Context context) {
		NetworkInfo networkInfo = ((ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE))
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		return networkInfo != null && networkInfo.isConnected()
				&& networkInfo.getExtraInfo() != null
				&& networkInfo.getExtraInfo().toLowerCase().contains("cmwap");
	}

	/**
	 * 连接的是否是2G网络
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNetworkConnectedBy2G(Context context) {
		NetworkInfo networkInfo = ((ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE))
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (networkInfo != null && networkInfo.isConnected()) {
			int subtype = networkInfo.getSubtype();
			if (subtype == TelephonyManager.NETWORK_TYPE_GPRS
					|| subtype == TelephonyManager.NETWORK_TYPE_EDGE
					|| subtype == TelephonyManager.NETWORK_TYPE_CDMA) {// 移动和联通2G
				return true;
			}
		}
		return false;
	}

	/**
	 * 连接的是否是3G网络
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNetworkConnectedBy3G(Context context) {
		NetworkInfo networkInfo = ((ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE))
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (networkInfo != null && networkInfo.isConnected()) {
			int subtype = networkInfo.getSubtype();
			if (subtype == TelephonyManager.NETWORK_TYPE_EVDO_A
					|| subtype == TelephonyManager.NETWORK_TYPE_EVDO_0
					|| subtype == TelephonyManager.NETWORK_TYPE_UMTS
					|| subtype == TelephonyManager.NETWORK_TYPE_HSPA) {// 电信或联通3G
				return true;
			}
		}
		return false;
	}

	/**
	 * Wifi网络是否已连接
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNetworkConnectedByWifi(Context context) {
		NetworkInfo networkInfo = ((ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE))
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return networkInfo != null && networkInfo.isConnected();
	}

	/**
	 * 网络是否已连接
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNetworkConnected(Context context) {
		try {
			NetworkInfo networkInfo = ((ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE))
					.getActiveNetworkInfo();
			return networkInfo != null && networkInfo.isConnected();
		} catch (NullPointerException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 是否开启飞行模式
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isAirplaneModeOn(Context context) {
		return Settings.System.getInt(context.getContentResolver(),
				Settings.System.AIRPLANE_MODE_ON, 0) != 0;
	}

	/**
	 * 获取当前屏幕亮度，范围0-255
	 * 
	 * @param context
	 * @return 屏幕当前亮度值
	 */
	public static int getScreenBrightness(Context context) {
		int rightnessValue = 0;
		try {
			rightnessValue = Settings.System.getInt(
					context.getContentResolver(),
					Settings.System.SCREEN_BRIGHTNESS);
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}
		return rightnessValue;
	}

	/**
	 * 设置屏幕亮度（0-255）
	 * 
	 * @param activity
	 * @param screenBrightness
	 */
	public static void setScreenBrightness(Activity activity,
			float screenBrightness) {
		WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
		lp.screenBrightness = screenBrightness / 255f;
		activity.getWindow().setAttributes(lp);
	}

	/**
	 * 判断是否开启了自动亮度调节
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isAutomicBrightness(Context context) {
		boolean automicBrightness = false;
		try {
			automicBrightness = Settings.System.getInt(
					context.getContentResolver(),
					Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}
		return automicBrightness;
	}

	/**
	 * 开启亮度自动调节
	 * 
	 * @param context
	 */
	public static void startAutoBrightness(Context context) {
		Settings.System.putInt(context.getContentResolver(),
				Settings.System.SCREEN_BRIGHTNESS_MODE,
				Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
	}

	/**
	 * 停止自动亮度调节
	 * 
	 * @param context
	 */
	public static void stopAutoBrightness(Context context) {
		Settings.System.putInt(context.getContentResolver(),
				Settings.System.SCREEN_BRIGHTNESS_MODE,
				Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
	}

	/**
	 * 跳转系统网络设置界面
	 * 
	 * @param context
	 */
	public static boolean startActivitySettingWireless(Context context) {
		Intent intent = new Intent();
		if (Build.VERSION.SDK_INT < 14) {
			intent.setAction(Settings.ACTION_WIRELESS_SETTINGS);
		} else {
			intent.setAction(Settings.ACTION_SETTINGS);
		}
		try {
			context.startActivity(intent);
			return true;
		} catch (ActivityNotFoundException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 调整程序声音类型为媒体播放声音，并且与媒体播放声音大小一致
	 * 
	 * @param context
	 */
	public static void adjustVoiceToSystemSame(Context context) {
		AudioManager audioManager = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
				AudioManager.ADJUST_SAME, 0);
	}

	/**
	 * sdcard是否可读写
	 * 
	 * @return
	 */
	public static boolean isSdcardReady() {
		try {
			return Environment.MEDIA_MOUNTED.equals(Environment
					.getExternalStorageState());
		} catch (Exception e) {
			LogS.e(TAG, "isSdcardReady had exception!", e);
			return false;
		}
	}

	public static boolean isSdcardAvailable(int fileSize) {
		if (isSdcardReady()) {
			File sdcardDir = Environment.getExternalStorageDirectory();
			StatFs sf = new StatFs(sdcardDir.getPath());
			long availCount = sf.getAvailableBlocks();
			long blockSize = sf.getBlockSize();
			long availSize = availCount * blockSize;
			if (availSize - SURPLUS_AVAIL_SIZE >= fileSize) {
				return true;
			} else {
				return false;
			}
		}

		return false;
	}

	/**
	 * 获取文件系统的剩余空间，单位：KB
	 * 
	 * @return
	 */
	public static long getFileSystemAvailableSize(File dirName) {
		long availableSize = -1;
		if (dirName != null && dirName.exists()) {
			StatFs sf = new StatFs(dirName.getPath());
			long blockSize = sf.getBlockSize();
			long blockCount = sf.getBlockCount();
			long availableBlocks = sf.getAvailableBlocks();
			availableSize = availableBlocks * blockSize / 1024;
			LogS.d(TAG, "blockSize = " + blockSize + ", blockCount = "
					+ blockCount + ", totalSize = " + blockSize * blockCount
					/ 1024 + " KB" + "\navailableBlocks = " + availableBlocks
					+ ", availableSize = " + availableSize + " KB");
		}
		return availableSize;
	}

	/**
	 * 获取Context所在进程的名称
	 * 
	 * @param context
	 * @return
	 */
	public static String getProcessName(Context context) {
		int pid = android.os.Process.myPid();
		ActivityManager mActivityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningAppProcessInfo appProcess : mActivityManager
				.getRunningAppProcesses()) {
			if (appProcess.pid == pid) {
				return appProcess.processName;
			}
		}
		return null;
	}

	/**
	 * 应用是否已经安装
	 * 
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static boolean isAppInstalled(Context context, String packageName) {
		try {
			// mContext.getPackageInfo(String packageName, int
			// flags)第二个参数flags为0：因为不需要该程序的其他信息，只需返回程序的基本信息。
			return context.getPackageManager().getPackageInfo(packageName, 0) != null;
		} catch (NameNotFoundException e) {
		}
		return false;
	}

	/**
	 * 网络是否手机网络连接
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isOnlyMobileType(Context context) {
		State wifiState = null;
		State mobileState = null;
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = null;
		try {
			networkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (networkInfo != null) {
			wifiState = networkInfo.getState();
		}
		try {
			networkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (networkInfo != null) {
			mobileState = networkInfo.getState();
		}
		LogS.d("zhang", "onReceive -- wifiState = " + wifiState
				+ " -- mobileState = " + mobileState);
		if (wifiState != null && mobileState != null
				&& State.CONNECTED != wifiState
				&& State.CONNECTED == mobileState) {
			// 手机网络连接成功
			LogS.d("zhang", "onReceive -- 手机网络连接成功");
			return true;
		}
		return false;
	}

	/**
	 * 程序是否在前台运行
	 * 
	 * @return
	 */
	public static boolean isAppOnForeground(Context context) {
		try {
			ActivityManager activityManager = (ActivityManager) context
					.getApplicationContext().getSystemService(
							Context.ACTIVITY_SERVICE);
			String packageName = context.getPackageName();
			List<RunningAppProcessInfo> appProcesses = activityManager
					.getRunningAppProcesses();
			if (appProcesses == null) {
				return false;
			}
			for (RunningAppProcessInfo appProcess : appProcesses) {
				// The name of the process that this object is associated with.
				if (appProcess.processName.equals(packageName)
						&& appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			LogS.e(TAG, "isAppOnForeground exception!", e);
		}
		return false;
	}

	public static long getAvailableInternalRomSize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		long availBlocks = stat.getAvailableBlocks();
		return availBlocks * blockSize;
	}

	private static String mTempString = null;

	public static String numToChina(int num) {
		int size = (num + "").length();
		if (size == 1) {
			mTempString = arabNumtoChinese(num);
		} else if (size == 2) {
			if (num / 10 == 1) {
				mTempString = "十";
			} else {
				mTempString = arabNumtoChinese(num / 10) + "十";
			}
			if (num % 10 != 0) {
				mTempString += arabNumtoChinese(num % 10);
			}
		} else if (size == 3) {
			mTempString = arabNumtoChinese(num / 100) + "百";
			if (num % 100 != 0) {
				int num1 = num % 100;
				int num2 = num1 / 10;
				mTempString += arabNumtoChinese(num1 / 10);
				if (num2 != 0) {
					mTempString += "十";
				}
				if (num % 10 != 0) {
					mTempString += arabNumtoChinese(num1 % 10);
				}
			}
		}
		return mTempString;
	}

	private static String arabNumtoChinese(int a) {
		StringBuffer sb = new StringBuffer();
		String[] str = new String[] { "零", "一", "二", "三", "四", "五", "六", "七",
				"八", "九" };
		return sb.append(str[a]).toString();
	}

	/**
	 * 判断是否是本地url
	 * 
	 * @return
	 */
	public static boolean isLocalUrl(String url) {
		File file = Environment.getExternalStorageDirectory();
		if (AndroidUtil.isSdcardReady() && file != null
				&& url.startsWith(file.getAbsolutePath())) {
			return true;
		}
		if (url.startsWith("http"))
			return false;
		return false;
	}
}