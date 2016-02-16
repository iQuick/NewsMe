package com.sina.sinavideo.coreplayer.splayer;

import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

@SuppressLint("NewApi")
public class SMedia {

	private static String TAG = "SMedia";
	// private static SMedia gMediaCodecWrap = null; //for support multi player
	private static String gCname = null;
	private boolean debug = false;
	private String mime = "video/avc";
	private MediaFormat format = null;
	private MediaCodec decoder = null;
	public ByteBuffer[] inputBuffers = null;
	public ByteBuffer[] outputBuffers = null;
	private int width = -1, height = -1;
	private long encodeFramePTS = 0; // last frame PTS
	private BufferInfo info = null;
	private int mNativeContext = 0; // accessed by native methods
	private ReentrantLock nativeFunctionLock = null;
	private Surface mSurface = null;
	// /visit by native
	public ByteBuffer csd0Array = null;
	public ByteBuffer csd1Array = null;
	public ByteBuffer encodeFrame;
	public ByteBuffer decodeFrame;

	// public String mCodecName = "";
	// /end

	public static SMedia create() {
		// Log.d(TAG,"create into");
		// if (gMediaCodecWrap == null) {
		// gMediaCodecWrap = new SMedia();
		// }
		// return gMediaCodecWrap;
		return new SMedia();
	}

	public SMedia() {
		nativeFunctionLock = new ReentrantLock();
	}

	public void registerContext(int contxt) {
		log("registerContext() new contxt=" + contxt + ",old mNativeContext="
				+ mNativeContext);
		nativeFunctionLock.lock();
		mNativeContext = contxt;
		nativeFunctionLock.unlock();
	}

	public void alloc(int contxt, int csd, int size) {
		log("alloc() contxt=" + contxt + ", csd=" + csd + ",size=" + size);
		nativeFunctionLock.lock();
		if (contxt != mNativeContext) {
			nativeFunctionLock.unlock();
			Log.e(TAG, "alloc() mNativeContext change!");
			return;
		}
		if (csd == 1) {
			csd0Array = ByteBuffer.allocateDirect(size);
		} else {
			csd1Array = ByteBuffer.allocateDirect(size);
		}
		nativeFunctionLock.unlock();
	}

	public native Surface getSurface(int contxt);

	public int start(int contxt, int width_, int height_) {
		log("start() into, contxt=" + contxt + ",width_=" + width_
				+ ",height_=" + height_);
		nativeFunctionLock.lock();
		if (contxt != mNativeContext) {
			nativeFunctionLock.unlock();
			Log.e(TAG, "start() mNativeContext change!");
			return -1;
		}
		if (null != decoder) {
			try {
				decoder.stop();
				decoder.release();
				decoder = null;
			} catch (Exception ex) {
				Log.e(TAG, "start() decoder release fail");
				ex.printStackTrace();
			}
		}
		int ret = -1;
		if (width_ <= 0 || height_ <= 0) {
			Log.e(TAG, "width height wrong!");
			nativeFunctionLock.unlock();
			return -1;
		}
		if (null == csd0Array || null == csd1Array) {
			Log.e(TAG, "pps sps wrong!");
			nativeFunctionLock.unlock();
			return -1;
		}
		width = width_;
		height = height_;

		try {
			info = new BufferInfo();
			format = MediaFormat.createVideoFormat(mime, width, height);
			if (null == format) {
				Log.e(TAG, "cannot create video format!");
				nativeFunctionLock.unlock();
				return -1;
			}
			format.setByteBuffer("csd-0", csd0Array);
			format.setByteBuffer("csd-1", csd1Array);

			if (gCname == null) {
				gCname = haveSEC();
			}
			if (gCname.equals("")) {
				decoder = MediaCodec.createDecoderByType(mime);
			} else {
				decoder = MediaCodec.createByCodecName(gCname);
			}

			if (decoder == null) {
				Log.e(TAG, "createDecoderByType fail");
				nativeFunctionLock.unlock();
				return -1;
			}
			// String codecName = findCodecName();
			// if(codecName!=null){
			// mCodecName = codecName;
			// }
			// Log.d(TAG,"start() format=[" + format + "]");
			mSurface = getSurface(mNativeContext);
			if (null == mSurface) {
				Log.e(TAG, "mSurface null");
				nativeFunctionLock.unlock();
				return -1;
			} else {
				// Log.d(TAG,"mSurface not null");
			}
			// Log.d(TAG,"decoder.configure into,mSurface=[" + mSurface + "]");
			decoder.configure(format, mSurface, null, 0);
			// Log.d(TAG,"decoder.configure out");
			decoder.start();

			inputBuffers = decoder.getInputBuffers();
			outputBuffers = decoder.getOutputBuffers();

			ret = 0;
		} catch (Exception ex) {
			Log.e(TAG, "start fail!");
			ex.printStackTrace();
			ret = -1;
		}
		nativeFunctionLock.unlock();
		log("start out, ret=" + ret);
		return ret;
	}

	public native int readEncodeFrame(int contxt);

	// call from native in decode()
	public void pts(long t) {
		// log("pts="+t);
		encodeFramePTS = t;
	}

	public native int writeDecodeFrame(int contxt, long pts);

	public int decode(int contxt) {
		log("decode() into," + "contxt=" + contxt);
		nativeFunctionLock.lock();
		int ret = 0;
		if (contxt != mNativeContext) {
			nativeFunctionLock.unlock();
			Log.e(TAG, "decode() mNativeContext change!");
			return -4;
		}
		if (decoder == null) {
			Log.e(TAG, "decode() decoder null");
			nativeFunctionLock.unlock();
			return -1; // try again
		}

		try {
			int inIndex = decoder.dequeueInputBuffer(10000);
			if (inIndex >= 0) {
				encodeFrame = inputBuffers[inIndex];
				int sampleSize = readEncodeFrame(mNativeContext);
				if (sampleSize < 0) {
					// log("sampleSize < 0, set EOS 1");
					// decoder.queueInputBuffer(inIndex, 0, 0, 0,
					// android.media.MediaCodec.BUFFER_FLAG_END_OF_STREAM);
					// isEOS = 1;
					decoder.queueInputBuffer(inIndex, 0, 0, 0, 0);
				} else {
					decoder.queueInputBuffer(inIndex, 0, sampleSize,
							encodeFramePTS, 0);
				}
			} else {
				log(" decoder.dequeueInputBuffer timeout");
			}
		} catch (Exception ex) {
			Log.e(TAG, "decode() handle input buffer fail!");
			ex.printStackTrace();
			nativeFunctionLock.unlock();
			return -4; // try again
		}

		try {
			int outIndex = decoder.dequeueOutputBuffer(info, 10000);
			switch (outIndex) {
			case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
				log("INFO_OUTPUT_BUFFERS_CHANGED");
				outputBuffers = decoder.getOutputBuffers();
				ret = outIndex;
				break;
			case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
				log("New format " + decoder.getOutputFormat());
				ret = outIndex;
				break;
			case MediaCodec.INFO_TRY_AGAIN_LATER:
				log("dequeueOutputBuffer timed out!");
				ret = outIndex;
				break;
			default:
				log("decoder.dequeueOutputBuffer OK");
				// decodeFrame = outputBuffers[outIndex];
				writeDecodeFrame(mNativeContext, info.presentationTimeUs);
				decoder.releaseOutputBuffer(outIndex, true); // decoder.releaseOutputBuffer(outIndex,
																// false);
				ret = 0; // ret = 0;
				break;
			}
		} catch (Exception ex) {
			Log.e(TAG, "decode() handle output buffer fail!");
			ex.printStackTrace();
			nativeFunctionLock.unlock();
			return -4; // try again
		}
		// All decoded frames have been rendered, we can stop playing now
		// if ((info.flags & android.media.MediaCodec.BUFFER_FLAG_END_OF_STREAM)
		// != 0) {
		// log("OutputBuffer BUFFER_FLAG_END_OF_STREAM");
		// ret = -4; //return -4;
		// }
		nativeFunctionLock.unlock();
		log("decode() out, ret=" + ret);
		return ret;
	}

	public void stop(int contxt) {
		log("stop into");
		nativeFunctionLock.lock();
		if (contxt != mNativeContext) {
			nativeFunctionLock.unlock();
			Log.e(TAG, "stop() mNativeContext change!");
			return;
		}
		if (decoder != null) {
			try {
				log("stop() resleasing decoder");
				decoder.stop();
				decoder.release();
				decoder = null;
				log("decoder release mSurface=[" + mSurface + "]");
				mSurface = null;
			} catch (Exception ex) {
				Log.e(TAG, "stop() reslease decoder fail");
				ex.printStackTrace();
			}
		}
		nativeFunctionLock.unlock();
		log("stop out");
	}

	/*
	 * private String findCodecName() { String name = null; try{ int numCodecs =
	 * MediaCodecList.getCodecCount(); for (int i = 0; i < numCodecs; i++) {
	 * MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i); if
	 * (codecInfo.isEncoder()) { continue; } String[] types =
	 * codecInfo.getSupportedTypes(); for (int j = 0; j < types.length; j++) {
	 * if (types[j].equalsIgnoreCase(mime)) { name = codecInfo.getName();
	 * //Log.d(TAG,"codec name=" + name); if(!name.contains("google") ||
	 * !name.contains("GOOGLE") ){ return name; } } } } }catch(Exception ex){ }
	 * return name; }
	 */

	/**
	 * check c name
	 */
	private String haveSEC() {
		try {
			int numCodecs = MediaCodecList.getCodecCount();
			for (int i = 0; i < numCodecs; i++) {
				MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
				if (codecInfo.isEncoder()) {
					continue;
				}
				String[] types = codecInfo.getSupportedTypes();
				if (null == types) {
					break;
				}
				for (int j = 0; j < types.length; j++) {
					if (types[j] != null
							&& types[j].equalsIgnoreCase(mime)
							&& codecInfo.getName().equalsIgnoreCase(
									"OMX.SEC.AVC.Decoder")) {
						// Log.d(TAG, "find SEC," +codecInfo.getName()
						// +"->support type["+j+"]="+types[j]);
						return codecInfo.getName();
					}
				}
			}
		} catch (Exception ex) {
			Log.e(TAG, "find SEC error!");
		}
		// Log.d(TAG, "not find SEC!");
		return "";
	}

	private void log(String l) {
		if (debug) {
			Log.d(TAG, "MediaCodecWrap: " + l);
		}
	}

}
