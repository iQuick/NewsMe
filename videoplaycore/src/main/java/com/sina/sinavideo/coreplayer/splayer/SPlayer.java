package com.sina.sinavideo.coreplayer.splayer;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.sina.sinavideo.coreplayer.R;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SPlayer {

	private static final String TAG = "SPlayer";
	private static final String COMPRESS_LIBS_NAME = "libsplayer.7z";
	// private static final String[] LIBS_ARM_CODECS = {"libsplayer.so"};
	// private static final String[] LIBS_X86_CODECS = {"libsplayer.so"};
	// private static final String[] LIBS_MIPS_CODECS = {"libsplayer.so"};
	private static final String[] LIBS_PLAYER = { "libsplayer.so",
			"libsinadlna_jni.so" };
	private static final String LIBS_LOCK = ".lock";
	private static final int VITAMIO_NOT_SUPPORTED = -1;
	private static final int VITAMIO_MIPS = 90;
	private static final int VITAMIO_X86 = 80;
	private static final int VITAMIO_ARMV6 = 60;
	private static final int VITAMIO_ARMV6_VFP = 61;
	private static final int VITAMIO_ARMV7_VFPV3 = 70;
	private static final int VITAMIO_ARMV7_NEON = 71;
	private static final int vitamioType;

	static {
		int cpu = CPU.getFeature();
		if ((cpu & CPU.FEATURE_ARM_NEON) > 0)
			vitamioType = VITAMIO_ARMV7_NEON;
		else if ((cpu & CPU.FEATURE_ARM_VFPV3) > 0
				&& (cpu & CPU.FEATURE_ARM_V7A) > 0)
			vitamioType = VITAMIO_ARMV7_VFPV3;
		else if ((cpu & CPU.FEATURE_ARM_VFP) > 0
				&& (cpu & CPU.FEATURE_ARM_V6) > 0)
			vitamioType = VITAMIO_ARMV6_VFP;
		else if ((cpu & CPU.FEATURE_ARM_V6) > 0)
			vitamioType = VITAMIO_ARMV6;
		else if ((cpu & CPU.FEATURE_X86) > 0)
			vitamioType = VITAMIO_X86;
		else if ((cpu & CPU.FEATURE_MIPS) > 0)
			vitamioType = VITAMIO_MIPS;
		else
			vitamioType = VITAMIO_NOT_SUPPORTED;

	}

	private static String vitamioPackage;
	private static String vitamioLibraryPath;
	private static String splayerLogPath = null;

	private static boolean isClearOldLibrary = false;
	private static final String[] oldLibrarys = { "libffmpeg.so",
			"libOMX.11.so", "libOMX.14.so", "libOMX.18.so", "libOMX.9.so",
			// "libstlport_shared.so",
			"libvao.0.so", "libvplayer.so", "libvscanner.so", "libvvo.0.so",
			"libvvo.7.so", "libvvo.8.so", "libvvo.9.so", "libvvo.j.so" };

	/**
	 * Call this method before using any other SPlayer specific classes.
	 * <p/>
	 * This method will use {@link #isInitialized(Context)} to check if SPlayer
	 * is initialized at this device, and initialize it if not initialized.
	 * 
	 * @param ctx
	 *            Android Context
	 * @return true if the SPlayer initialized successfully.
	 */
	public static boolean initialize(Context ctx) {
		return isInitialized(ctx) || extractLibs(ctx, R.raw.libsplayer);
	}

	/**
	 * Same as {@link #initialize(Context)}
	 * 
	 * @param ctx
	 *            Android Context
	 * @param rawId
	 *            R.raw.libsplayer
	 * @return true if the SPlayer initialized successfully.
	 */
	public static boolean initialize(Context ctx, int rawId) {
		return isInitialized(ctx) || extractLibs(ctx, rawId);
	}

	public static void clearOldLibrary() {
		if (isClearOldLibrary == false) {
			File dir = new File(getLibraryPath());
			if (dir.exists() && dir.isDirectory()) {
				for (String oldFileName : oldLibrarys) {
					File oldFile = new File(getLibraryPath() + oldFileName);
					if (oldFile.exists() && oldFile.isFile()) {
						Log.e(TAG, "delete old file:" + oldFileName);
						oldFile.delete();
					}
				}
			}
			isClearOldLibrary = true;
		}
	}

	/**
	 * Check if SPlayer is initialized at this device
	 * 
	 * @param ctx
	 *            Android Context
	 * @return true if the SPlayer has been initialized.
	 */
	public static boolean isInitialized(Context ctx) {
		vitamioPackage = ctx.getPackageName();
		vitamioLibraryPath = ContextUtils.getDataDir(ctx) + "libs/";

		if (splayerLogPath == null) {
			String state = Environment.getExternalStorageState();
			if (Environment.MEDIA_MOUNTED.equals(state)) {
				Log.d(TAG, "sdcard  mount");
				File filepath = ctx.getExternalFilesDir(null);
				if (filepath != null) {
					if (!filepath.exists()) {
						if (filepath.mkdirs()) {
							Log.d(TAG, "filepath.mkdirs() success");
						}
					}
				} else {
					Log.e(TAG, "getExternalFilesDir(null) fail");
				}
				File logpath = ctx.getExternalFilesDir("splayer");
				if (logpath != null) {
					if (!logpath.exists()) {
						if (logpath.mkdirs()) {
							splayerLogPath = logpath.getAbsolutePath();
							Log.d(TAG,
									"mkdirs() getAbsolutePath splayerLogPath="
											+ splayerLogPath);
						}
					} else {
						splayerLogPath = logpath.getAbsolutePath();
						Log.d(TAG, "getAbsolutePath splayerLogPath="
								+ splayerLogPath);
					}
				} else {
					Log.e(TAG, "getExternalFilesDir(splayer) fail");
				}
			}
			if (null == splayerLogPath) {
				splayerLogPath = ContextUtils.getDataDir(ctx) + "splayer/"; // /data/data/com.sina.sinavideo/splayer/
			}
			Log.d(TAG, "splayerLogPath=" + splayerLogPath);
		}

		clearOldLibrary(); // clear vitamio library

		File dir = new File(getLibraryPath());
		if (dir.exists() && dir.isDirectory()) {
			String[] libs = dir.list();
			if (libs != null) {
				Arrays.sort(libs);
				for (String L : getRequiredLibs()) {
					if (Arrays.binarySearch(libs, L) < 0) {
						Log.e(TAG, "Native libs " + L + " not exists!");
						return false; // /发现异常后应该重新解压一遍
					}
				}
				File lock = new File(getLibraryPath() + LIBS_LOCK);
				BufferedReader buffer = null;
				try {
					buffer = new BufferedReader(new FileReader(lock));
					int appVersion = ContextUtils.getVersionCode(ctx);
					int libVersion = Integer.valueOf(buffer.readLine());
					Log.i(TAG, "isNativeLibsInited, APP VERSION: " + appVersion
							+ ", SPLayer Library version: " + libVersion);
					if (libVersion == appVersion)
						return true;
				} catch (IOException e) {
					Log.e(TAG, "isNativeLibsInited error," + e.toString());
				} catch (NumberFormatException e) {
					Log.e(TAG, "isNativeLibsInited error, " + e.toString());
				} finally {
					IOUtils.closeSilently(buffer);
				}
			}
		}
		return false;
	}

	public static String getVitamioPackage() {
		return vitamioPackage;
	}

	public static int getVitamioType() {
		return vitamioType;
	}

	public static final String getLibraryPath() {
		return vitamioLibraryPath;
	}

	// 播放器日志目录
	public static final String getLogPath() {
		if (splayerLogPath == null) {
			// @INFO 这儿要改下，不能直接取
			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				splayerLogPath = Environment.getExternalStorageDirectory()
						.getAbsolutePath()+"/splayer/";
			}else{
				
			}
			// splayerLogPath = "/sdcard/splayer/";
		}
		try {
			File f = new File(splayerLogPath);
			if (!f.exists()) {
				f.mkdir();
			} else {
				if (!f.isDirectory() && f.isFile()) {
					Log.e("SPlayer", "log exit but is file");
					f.delete();
					f.mkdir();
				}
			}
		} catch (Exception ex) {
			Log.e("SPlayer", "create log dir fail");
			ex.printStackTrace();
		}
		return splayerLogPath;
	}

	private static final List<String> getRequiredLibs() {
		List<String> libs = new ArrayList<String>();
		String[][] vitamioLibs = null;
		switch (vitamioType) {
		case VITAMIO_ARMV6:
		case VITAMIO_ARMV6_VFP:
		case VITAMIO_ARMV7_VFPV3:
		case VITAMIO_ARMV7_NEON:
			vitamioLibs = new String[][] { LIBS_PLAYER }; // vitamioLibs = new
															// String[][]{LIBS_ARM_CODECS,
															// LIBS_PLAYER};
			break;
		case VITAMIO_X86:
			vitamioLibs = new String[][] { LIBS_PLAYER }; // vitamioLibs = new
															// String[][]{LIBS_X86_CODECS,
															// LIBS_PLAYER};
			break;
		case VITAMIO_MIPS:
			vitamioLibs = new String[][] { LIBS_PLAYER }; // vitamioLibs = new
															// String[][]{LIBS_MIPS_CODECS,
															// LIBS_PLAYER};
			break;
		default:
			break;
		}
		if (vitamioLibs == null)
			return libs;
		for (String[] libArray : vitamioLibs) {
			for (String lib : libArray)
				libs.add(lib);
		}
		libs.add(LIBS_LOCK);
		return libs;
	}

	private static boolean extractLibs(Context ctx, int rawID) {
		long begin = System.currentTimeMillis();
		final int version = ContextUtils.getVersionCode(ctx);
		Log.d(TAG, "loadLibs start " + version);
		File lock = new File(getLibraryPath() + LIBS_LOCK);
		if (lock.exists()) {
			if (false == lock.delete()) {
				Log.e(TAG, "extractLibs: delete lock file fail");
			}
		}
		// /加上一个逻辑，解压前应将原有的so删掉，防止解压出错
		for (String L : getRequiredLibs()) {
			File tmpFile = new File(getLibraryPath() + L);
			if (tmpFile != null && tmpFile.exists() && tmpFile.isFile()) {
				tmpFile.delete();
				Log.d(TAG, "extractLibs: delete file:" + getLibraryPath() + L);
			}
		}
		// /
		String libPath = copyCompressedLib(ctx, rawID, COMPRESS_LIBS_NAME);
		if (libPath == null) {
			return false;
		}

		Log.d(TAG, "copyCompressedLib time: "
				+ (System.currentTimeMillis() - begin) / 1000.0);
		boolean inited = initializeLibs(libPath, getLibraryPath(),
				String.valueOf(SPlayer.getVitamioType()));
		File oldZip = new File(libPath);
		if (false == oldZip.delete()) {
			Log.e(TAG, "extractLibs: cannot delete zip file:" + libPath);
		} else {
			// Log.d(TAG,"extractLibs: delete zip file:" +libPath);
		}
		FileWriter fw = null;
		try {
			if (false == lock.createNewFile()) {
				Log.e(TAG, "extractLibs: createNewFile return false");
			} else {
				// Log.e(TAG,"extractLibs: createNewFile return true");
			}
			fw = new FileWriter(lock);
			fw.write(String.valueOf(version));
			return true;
		} catch (IOException e) {
			Log.e(TAG, "Error creating lock file, " + e.toString());
		} finally {
			Log.d(TAG, "initializeNativeLibs: " + inited + ", libsType:"
					+ SPlayer.getVitamioType());
			Log.d(TAG, "loadLibs time: " + (System.currentTimeMillis() - begin)
					/ 1000.0);
			IOUtils.closeSilently(fw);
		}
		return false;
	}

	private static String copyCompressedLib(Context ctx, int rawID,
			String destName) {
		byte[] buffer = new byte[1024];
		InputStream is = null;
		BufferedInputStream bis = null;
		FileOutputStream fos = null;
		String destPath = null;

		try {
			try {
				String destDir = getLibraryPath();
				destPath = destDir + destName;
				File f = new File(destDir);
				if (f.exists() && !f.isDirectory())
					f.delete();
				if (!f.exists())
					f.mkdirs();
				f = new File(destPath);
				if (f.exists() && !f.isFile())
					f.delete();
				if (!f.exists())
					f.createNewFile();
			} catch (Exception fe) {
				Log.e(TAG, "loadLib error, " + fe.toString());
			}

			is = ctx.getResources().openRawResource(rawID);
			bis = new BufferedInputStream(is);
			fos = new FileOutputStream(destPath);
			while (bis.read(buffer) != -1) {
				fos.write(buffer);
			}
		} catch (Exception e) {
			Log.e(TAG, "loadLib error, " + e.toString());
			return null;
		} finally {
			IOUtils.closeSilently(fos);
			IOUtils.closeSilently(bis);
			IOUtils.closeSilently(is);
		}

		return destPath;
	}

	static {
		System.loadLibrary("sinit");
	}

	private native static boolean initializeLibs(String libPath,
			String destDir, String prefix);
}
