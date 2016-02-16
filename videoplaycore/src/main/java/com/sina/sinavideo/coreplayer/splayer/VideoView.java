package com.sina.sinavideo.coreplayer.splayer;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.PixelFormat;
import android.graphics.Region;
import android.media.AudioManager;
import android.media.TimedText;
import android.net.Uri;
import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Toast;

import com.sina.sinavideo.coreplayer.ISinaMediaController;
import com.sina.sinavideo.coreplayer.ISinaVideoView;
import com.sina.sinavideo.coreplayer.R;
import com.sina.sinavideo.coreplayer.util.AndroidUtil;
import com.sina.sinavideo.coreplayer.util.LogS;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;

public class VideoView extends GLSurfaceView implements GLSurfaceView.Renderer,
		ISinaVideoView {

	private static String TAG = "SPlayerVideoView";
	// //////////////////////////////////////
	private static final int STATE_ERROR = -1;
	private static final int STATE_IDLE = 0;
	private static final int STATE_PREPARING = 1;
	private static final int STATE_PREPARED = 2;
	private static final int STATE_PLAYING = 3;
	private static final int STATE_PAUSED = 4;
	private static final int STATE_PLAYBACK_COMPLETED = 5;
	private static final int STATE_SUSPEND = 6;
	private static final int STATE_RESUME = 7;
	private static final int STATE_SUSPEND_UNSUPPORTED = 8;

	private Uri mUri;
	private int mDuration;
	private int mCurrentState = STATE_IDLE;
	private int mTargetState = STATE_IDLE;
	private float mAspectRatio = 0;
	private int mVideoLayout = VIDEO_SCALE_CENTER_INSIDE;
	private SurfaceHolder mSurfaceHolder = null;
	private MediaPlayer mMediaPlayer = null;
	private int mVideoWidth;
	private int mVideoHeight;
	private float mVideoAspectRatio;
	// private int mVideoChroma = MediaPlayer.VIDEOCHROMA_RGBA;
	private int mSurfaceWidth;
	private int mSurfaceHeight;
	private ISinaMediaController mMediaController;
	// private View mMediaBufferingIndicator;
	private MediaPlayer.OnCompletionListener mOnCompletionListener;
	private MediaPlayer.OnPreparedListener mOnPreparedListener;
	private MediaPlayer.OnErrorListener mOnErrorListener;
	private MediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener;
	private MediaPlayer.OnSeekCompleteListener mOnSeekCompleteListener;
	private MediaPlayer.OnInfoListener mOnInfoListener;
	private MediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener;
	private MediaPlayer.OnTimedTextListener mOnTimedTextListener;
	private MediaPlayer.OnVideoOpenedListener mOnVideoOpenedListener;
	private int mCurrentBufferPercentage;
	private long mSeekWhenPrepared; // recording the seek position while
									// preparing
	private Context mContext;
	private Map<String, String> mHeaders;

	private boolean mChangeParentView = false;
	private boolean mNeedShowMediaController = false;

	// /private Bitmap mLastBitmap;
	// //////////////////////////////////////

	private static final boolean DEBUG = false;
	// True if onSurfaceCreated has been called.
	private boolean surfaceCreated = false;
	private boolean openGLCreated = false;
	// True if NativeFunctionsRegistered has been called.
	private boolean nativeFunctionsRegisted = false;
	private ReentrantLock nativeFunctionLock = new ReentrantLock();
	// Address of Native object that will do the drawing.
	private long nativeObject = 0;
	private int viewWidth = 0;
	private int viewHeight = 0;

	// private Handler mHandler = null; //for //touch event

	// /private boolean surfaceChanged = false;
	private static boolean surfaceViewSupported = false;
	private static boolean sPlayerIsInitialized = false;

	private void log(String str) {
		if (DEBUG) {
			LogS.d(TAG, "VideoView: " + str);
		}
	}

	public static boolean UseOpenGL2(Object renderWindow) {
		return VideoView.class.isInstance(renderWindow);
	}

	public VideoView(Context context) {
		super(context);

		LogS.d(TAG, "VideoView(Context context) into");

		if (surfaceViewSupported == false) {
			surfaceViewSupported = IsSupported(context);
			if (surfaceViewSupported == false)
				return;
		}

		init(false, 0, 0);

		// mHandler = hand;
		initVideoView(context);
	}

	public VideoView(Context context, boolean translucent, int depth,
			int stencil) {
		super(context);

		LogS.d(TAG,
				"VideoView(Context context, boolean translucent,int depth, int stencil) into");

		if (surfaceViewSupported == false) {
			surfaceViewSupported = IsSupported(context);
			if (surfaceViewSupported == false)
				return;
		}

		init(translucent, depth, stencil);

		initVideoView(context);
	}

	private void init(boolean translucent, int depth, int stencil) {

		log("init() into");
		// By default, GLSurfaceView() creates a RGB_565 opaque surface.
		// If we want a translucent one, we should change the surface's
		// format here, using PixelFormat.TRANSLUCENT for GL Surfaces
		// is interpreted as any 32-bit surface with alpha by SurfaceFlinger.

		if (translucent) {
			this.getHolder().setFormat(PixelFormat.TRANSLUCENT);
		}

		// Setup the context factory for 2.0 rendering.
		// See ContextFactory class definition below
		setEGLContextFactory(new ContextFactory());

		// We need to choose an EGLConfig that matches the format of
		// our surface exactly. This is going to be done in our
		// custom config chooser. See ConfigChooser class definition
		// below.
		setEGLConfigChooser(translucent ? new ConfigChooser(8, 8, 8, 8, depth,
				stencil) : new ConfigChooser(5, 6, 5, 0, depth, stencil));

		// Set the renderer responsible for frame rendering
		this.setRenderer(this);
		this.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
	}

	// protected void onAttachedToWindow() {
	// LogS.i("videoView", "onAttachedToWindow " + getWindowToken() +
	// " , mChangeParentView = " + mChangeParentView);
	// if(mChangeParentView){
	//
	// } else {
	// super.onAttachedToWindow();
	// }
	// };
	//
	// protected void onDetachedFromWindow() {
	// LogS.i("videoView", "onDetachedFromWindow " + getWindowToken() +
	// " , mChangeParentView = " + mChangeParentView);
	// if(mChangeParentView){
	//
	// } else {
	// super.onDetachedFromWindow();
	// }
	// };
	//
	// protected void onWindowVisibilityChanged(int visibility) {
	// LogS.i("videoView", "onWindowVisibilityChanged " + getWindowToken() +
	// " , mChangeParentView = " + mChangeParentView);
	// if(mChangeParentView){
	//
	// } else {
	// super.onWindowVisibilityChanged(visibility);
	// }
	// };

	private static class ContextFactory implements
			GLSurfaceView.EGLContextFactory {

		private static int EGL_CONTEXT_CLIENT_VERSION = 0x3098;

		@Override
		public EGLContext createContext(EGL10 egl, EGLDisplay display,
				EGLConfig eglConfig) {
			LogS.d(TAG, "creating OpenGL ES 2.0 context");
			checkEglError("Before eglCreateContext", egl);
			int[] attrib_list = { EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE };
			EGLContext context = egl.eglCreateContext(display, eglConfig,
					EGL10.EGL_NO_CONTEXT, attrib_list);
			checkEglError("After eglCreateContext", egl);
			return context;
		}

		@Override
		public void destroyContext(EGL10 egl, EGLDisplay display,
				EGLContext context) {
			egl.eglDestroyContext(display, context);
		}
	}

	private static void checkEglError(String prompt, EGL10 egl) {
		int error;
		while ((error = egl.eglGetError()) != EGL10.EGL_SUCCESS) {
			LogS.d(TAG, String.format("%s: EGL error: 0x%x", prompt, error));
		}
	}

	private static class ConfigChooser implements
			GLSurfaceView.EGLConfigChooser {

		public ConfigChooser(int r, int g, int b, int a, int depth, int stencil) {
			mRedSize = r;
			mGreenSize = g;
			mBlueSize = b;
			mAlphaSize = a;
			mDepthSize = depth;
			mStencilSize = stencil;
		}

		// This EGL config specification is used to specify 2.0 rendering.
		// We use a minimum size of 4 bits for red/green/blue, but will
		// perform actual matching in chooseConfig() below.
		private static int EGL_OPENGL_ES2_BIT = 4;
		private static int[] s_configAttribs2 = { EGL10.EGL_RED_SIZE, 4,
				EGL10.EGL_GREEN_SIZE, 4, EGL10.EGL_BLUE_SIZE, 4,
				EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT, EGL10.EGL_NONE };

		@Override
		public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {

			// Get the number of minimally matching EGL configurations
			int[] num_config = new int[1];
			egl.eglChooseConfig(display, s_configAttribs2, null, 0, num_config);

			int numConfigs = num_config[0];

			if (numConfigs <= 0) {
				throw new IllegalArgumentException(
						"No configs match configSpec");
			}

			// Allocate then read the array of minimally matching EGL configs
			EGLConfig[] configs = new EGLConfig[numConfigs];
			egl.eglChooseConfig(display, s_configAttribs2, configs, numConfigs,
					num_config);

			if (DEBUG) {
				// printConfigs(egl, display, configs);
			}
			// Now return the "best" one
			return chooseConfig(egl, display, configs);
		}

		public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display,
				EGLConfig[] configs) {
			for (EGLConfig config : configs) {
				int d = findConfigAttrib(egl, display, config,
						EGL10.EGL_DEPTH_SIZE, 0);
				int s = findConfigAttrib(egl, display, config,
						EGL10.EGL_STENCIL_SIZE, 0);

				// We need at least mDepthSize and mStencilSize bits
				if (d < mDepthSize || s < mStencilSize)
					continue;

				// We want an *exact* match for red/green/blue/alpha
				int r = findConfigAttrib(egl, display, config,
						EGL10.EGL_RED_SIZE, 0);
				int g = findConfigAttrib(egl, display, config,
						EGL10.EGL_GREEN_SIZE, 0);
				int b = findConfigAttrib(egl, display, config,
						EGL10.EGL_BLUE_SIZE, 0);
				int a = findConfigAttrib(egl, display, config,
						EGL10.EGL_ALPHA_SIZE, 0);

				if (r == mRedSize && g == mGreenSize && b == mBlueSize
						&& a == mAlphaSize)
					return config;
			}
			return null;
		}

		private int findConfigAttrib(EGL10 egl, EGLDisplay display,
				EGLConfig config, int attribute, int defaultValue) {

			if (egl.eglGetConfigAttrib(display, config, attribute, mValue)) {
				return mValue[0];
			}
			return defaultValue;
		}

		private void printConfigs(EGL10 egl, EGLDisplay display,
				EGLConfig[] configs) {
			int numConfigs = configs.length;
			LogS.d(TAG, String.format("%d configurations", numConfigs));
			for (int i = 0; i < numConfigs; i++) {
				Log.w(TAG, String.format("Configuration %d:\n", i));
				printConfig(egl, display, configs[i]);
			}
		}

		private void printConfig(EGL10 egl, EGLDisplay display, EGLConfig config) {
			int[] attributes = { EGL10.EGL_BUFFER_SIZE, EGL10.EGL_ALPHA_SIZE,
					EGL10.EGL_BLUE_SIZE,
					EGL10.EGL_GREEN_SIZE,
					EGL10.EGL_RED_SIZE,
					EGL10.EGL_DEPTH_SIZE,
					EGL10.EGL_STENCIL_SIZE,
					EGL10.EGL_CONFIG_CAVEAT,
					EGL10.EGL_CONFIG_ID,
					EGL10.EGL_LEVEL,
					EGL10.EGL_MAX_PBUFFER_HEIGHT,
					EGL10.EGL_MAX_PBUFFER_PIXELS,
					EGL10.EGL_MAX_PBUFFER_WIDTH,
					EGL10.EGL_NATIVE_RENDERABLE,
					EGL10.EGL_NATIVE_VISUAL_ID,
					EGL10.EGL_NATIVE_VISUAL_TYPE,
					0x3030, // EGL10.EGL_PRESERVED_RESOURCES,
					EGL10.EGL_SAMPLES,
					EGL10.EGL_SAMPLE_BUFFERS,
					EGL10.EGL_SURFACE_TYPE,
					EGL10.EGL_TRANSPARENT_TYPE,
					EGL10.EGL_TRANSPARENT_RED_VALUE,
					EGL10.EGL_TRANSPARENT_GREEN_VALUE,
					EGL10.EGL_TRANSPARENT_BLUE_VALUE,
					0x3039, // EGL10.EGL_BIND_TO_TEXTURE_RGB,
					0x303A, // EGL10.EGL_BIND_TO_TEXTURE_RGBA,
					0x303B, // EGL10.EGL_MIN_SWAP_INTERVAL,
					0x303C, // EGL10.EGL_MAX_SWAP_INTERVAL,
					EGL10.EGL_LUMINANCE_SIZE, EGL10.EGL_ALPHA_MASK_SIZE,
					EGL10.EGL_COLOR_BUFFER_TYPE, EGL10.EGL_RENDERABLE_TYPE,
					0x3042 // EGL10.EGL_CONFORMANT
			};
			String[] names = { "EGL_BUFFER_SIZE", "EGL_ALPHA_SIZE",
					"EGL_BLUE_SIZE", "EGL_GREEN_SIZE", "EGL_RED_SIZE",
					"EGL_DEPTH_SIZE", "EGL_STENCIL_SIZE", "EGL_CONFIG_CAVEAT",
					"EGL_CONFIG_ID", "EGL_LEVEL", "EGL_MAX_PBUFFER_HEIGHT",
					"EGL_MAX_PBUFFER_PIXELS", "EGL_MAX_PBUFFER_WIDTH",
					"EGL_NATIVE_RENDERABLE", "EGL_NATIVE_VISUAL_ID",
					"EGL_NATIVE_VISUAL_TYPE", "EGL_PRESERVED_RESOURCES",
					"EGL_SAMPLES", "EGL_SAMPLE_BUFFERS", "EGL_SURFACE_TYPE",
					"EGL_TRANSPARENT_TYPE", "EGL_TRANSPARENT_RED_VALUE",
					"EGL_TRANSPARENT_GREEN_VALUE",
					"EGL_TRANSPARENT_BLUE_VALUE", "EGL_BIND_TO_TEXTURE_RGB",
					"EGL_BIND_TO_TEXTURE_RGBA", "EGL_MIN_SWAP_INTERVAL",
					"EGL_MAX_SWAP_INTERVAL", "EGL_LUMINANCE_SIZE",
					"EGL_ALPHA_MASK_SIZE", "EGL_COLOR_BUFFER_TYPE",
					"EGL_RENDERABLE_TYPE", "EGL_CONFORMANT" };
			int[] value = new int[1];
			for (int i = 0; i < attributes.length; i++) {
				int attribute = attributes[i];
				String name = names[i];
				if (egl.eglGetConfigAttrib(display, config, attribute, value)) {
					LogS.d(TAG, String.format("  %s: %d\n", name, value[0]));
				} else {
					// Log.w(TAG, String.format("  %s: failed\n", name));
					while (egl.eglGetError() != EGL10.EGL_SUCCESS)
						;
				}
			}
		}

		// Subclasses can adjust these values:
		protected int mRedSize;
		protected int mGreenSize;
		protected int mBlueSize;
		protected int mAlphaSize;
		protected int mDepthSize;
		protected int mStencilSize;
		private int[] mValue = new int[1];
	}

	// IsSupported
	// Return true if this device support Open GL ES 2.0 rendering.
	public static boolean IsSupported(Context context) {
		LogS.d(TAG, " IsSupported()");

		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		ConfigurationInfo info = am.getDeviceConfigurationInfo();
		if (info.reqGlEsVersion >= 0x20000) {
			// Open GL ES 2.0 is supported.
			return true;
		}
		return false;
	}

	// /implements GLSurfaceView.Renderer
	@Override
	public void onDrawFrame(GL10 gl) {
		// /log("onDrawFrame() into");//Log.d(TAG,"onDrawFrame() into");

		nativeFunctionLock.lock();
		// if(!nativeFunctionsRegisted || !surfaceCreated) {
		// LogS.d(TAG,
		// "onDrawFrame() donothing, !nativeFunctionsRegisted || !surfaceCreated");
		// nativeFunctionLock.unlock();
		// return;
		// }
		if (!nativeFunctionsRegisted) {
			// LogS.e(TAG, "onDrawFrame() donothing, !nativeFunctionsRegisted");
			nativeFunctionLock.unlock();
			return;
		} else if (!surfaceCreated) {
			// LogS.e(TAG, "onDrawFrame() donothing, !surfaceCreated");
			nativeFunctionLock.unlock();
			return;
		}

		if (!openGLCreated) {
			// /LogS.d(TAG, "onDrawFrame() CreateOpenGLNative into, viewWidth="
			// + viewWidth + ",viewHeight=" + viewHeight);
			if (0 != CreateOpenGLNative(nativeObject, viewWidth, viewHeight)) {
				LogS.e(TAG, " CreateOpenGLNative fail");
				nativeFunctionLock.unlock();
				return; // Failed to create OpenGL
			}
			openGLCreated = true; // Created OpenGL successfully
		}
		// log("DrawNative() begin");
		DrawNative(nativeObject); // Draw the new frame
		// log("DrawNative() end");
		nativeFunctionLock.unlock();
		// log("onDrawFrame() out");
	}

	// /implements GLSurfaceView.Renderer
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) { // /implements
																// GLSurfaceView.Renderer
		log("onSurfaceCreated() into");

		// mSurfaceHolder = this.getHolder();
		//
		// if (mMediaPlayer != null && mCurrentState == STATE_SUSPEND &&
		// mTargetState == STATE_RESUME) {
		// LogS.d(TAG, "onSurfaceCreated() resume");
		// resume();
		// } else {
		// LogS.d(TAG, "onSurfaceCreated() openVideo");
		// openVideo();
		// }

		// surfaceCreated = true;
		// openVideo();

		log("onSurfaceCreated() out");
	}

	// /implements GLSurfaceView.Renderer
	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		log("onSurfaceChanged() into,width=" + width + ",height=" + height);

		surfaceCreated = true;
		viewWidth = width;
		viewHeight = height;

		nativeFunctionLock.lock();
		if (nativeFunctionsRegisted) {
			// /LogS.d(TAG, "onSurfaceChanged() CreateOpenGLNative into, width="
			// + width + ",height=" + height);
			if (CreateOpenGLNative(nativeObject, width, height) == 0) {
				openGLCreated = true;
			}
		} else {
			LogS.w(TAG, "onSurfaceChanged() native object not register yet!");
		}
		nativeFunctionLock.unlock();

		log("onSurfaceChanged() out");
	}

	// /register from native
	public void RegisterNativeObject(long nativeObject) {
		// /Log.w(TAG,"RegisterNativeObject() into");///log("RegisterNativeObject() into");

		nativeFunctionLock.lock();
		this.nativeObject = nativeObject;
		nativeFunctionsRegisted = true;
		nativeFunctionLock.unlock();

		// /Log.w(TAG,"RegisterNativeObject() out");
		// ///log("RegisterNativeObject() out");
	}

	// /unregister from native
	public void DeRegisterNativeObject(long nativeObject) {
		// /Log.w(TAG,"DeRegisterNativeObject() into");
		// ///log("DeRegisterNativeObject() into");

		nativeFunctionLock.lock();

		if (nativeObject != this.nativeObject) {
			Log.e(TAG,
					"********************** DeRegisterNativeObject() nativeObject!= this.nativeObject ************************");
		} else {
			// //BUG: different native opengl channel obj shared the same
			// VideoView obj
			nativeFunctionsRegisted = false;
			this.nativeObject = 0;
		}
		// /nativeFunctionsRegisted = false;
		// /this.nativeObject = 0;
		openGLCreated = false;

		nativeFunctionLock.unlock();

		// /Log.w(TAG,"DeRegisterNativeObject() out");
		// ///log("DeRegisterNativeObject() out");
	}

	// /call from native
	public void ReDraw() {
		// log("ReDraw() into" );
		if (surfaceCreated) {
			// Request the renderer to redraw using the render thread context.
			this.requestRender();
		} else {
			LogS.w(TAG, "ReDraw() suface not created yet");
		}
		// log("ReDraw() out" );
	}

	// /call from native
	public int GetWidth() {
		return viewWidth;
	}

	// /call from native
	public int GetHeight() {
		return viewHeight;
	}

	private native int CreateOpenGLNative(long nativeObject, int width,
			int height);

	private native void DrawNative(long nativeObject);

	// /////////////////////////////////////
	private void initVideoView(Context context) {
		log("initVideoView() into");

		mContext = context;
		mVideoWidth = 0;
		mVideoHeight = 0;
		mCurrentState = STATE_IDLE;
		mTargetState = STATE_IDLE;
		if (context instanceof Activity) {
			((Activity) context)
					.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		}

		// /getHolder().setFormat(PixelFormat.RGBX_8888);
		getHolder().addCallback(mSHCallback);

		// TV上需要把这3行注释掉，以让上层view获取焦点处理按键事件，等远欢加上方法后再还原
		// setFocusable(true);
		// setFocusableInTouchMode(true);
		// requestFocus();
	}

	SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			log("surfaceCreated() into mCurrentState " + mCurrentState
					+ " mTargetState " + mTargetState);
			if (mSurfaceHolder != null) { // /create first time
				mSurfaceHolder = holder;
				log("surfaceCreated() SurfaceHolder already create, retun! ");
				return;
			} else {
				mSurfaceHolder = holder;
			}

			// if (mChangeParentView) {
			// setVideoLayout(mVideoLayout, mAspectRatio);
			// LogS.d(TAG, "surfaceCreated +++++++++ ChangeParentView");
			// setDisplayWithLastFrame();
			// endChangeParentView();
			// } else {
			if (mMediaPlayer != null && mCurrentState == STATE_SUSPEND
					&& mTargetState == STATE_RESUME) {
				log("surfaceCreated +++++++++ resume");
				// setDisplayWithLastFrame();
				resume();
			} else {
				log("surfaceCreated +++++++++ openVideo");
				openVideo();
			}
			// }
			log("surfaceCreated() out");
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int w,
				int h) {
			log("surfaceChanged() into，w=" + w + ",h=" + h);
			// mSurfaceWidth = w;
			// mSurfaceHeight = h;
			//
			// boolean isValidState = (mTargetState == STATE_PLAYING);
			// boolean hasValidSize = (mVideoWidth == w && mVideoHeight == h);
			// if (mMediaPlayer != null && isValidState && hasValidSize) {
			// //崩溃状态机
			// if (mSeekWhenPrepared != 0){
			// LogS.d(TAG,
			// "surfaceChanged() seekto?mSeekWhenPrepared="+mSeekWhenPrepared);
			// seekTo((int) mSeekWhenPrepared);
			// }
			// // start();
			// }

			// ///BUG 2.3切换holder时不会
			// surfaceCreated = true;
			// surfaceChanged = true;
			// viewWidth = w;
			// viewHeight = h;

			//
			// nativeFunctionLock.lock();
			// if(nativeFunctionsRegisted) {
			// if(CreateOpenGLNative(nativeObject,viewWidth,viewHeight) == 0){
			// openGLCreated = true;
			// }
			// }else{
			// LogS.d(TAG,"native object not register yet!");
			// }
			// nativeFunctionLock.unlock();

			log("surfaceChanged() out");
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			log("surfaceDestroyed() into,mCurrentState=" + mCurrentState);

			// /mSurfaceHolder = null;

			// don't release during change ParentView
			// if (mChangeParentView) {
			// if (mMediaPlayer != null && mCurrentState != STATE_PLAYING) {
			// try {
			// mLastBitmap = mMediaPlayer.getCurrentFrame();
			// } catch (Exception e) {
			// e.printStackTrace();
			// }
			// } else {
			// mLastBitmap = null;
			// }
			// } else {
			// if (mCurrentState != STATE_SUSPEND) {
			// release(true);
			// } else {
			// if (mMediaPlayer != null && mCurrentState != STATE_PLAYING) {
			// try {
			// mLastBitmap = mMediaPlayer.getCurrentFrame();
			// } catch (Exception e) {
			// e.printStackTrace();
			// }
			// } else {
			// mLastBitmap = null;
			// }
			// }
			// }

			log("surfaceDestroyed() out");
		}
	};

	private void openVideo() {
		log("openVideo " + mUri);

		if (surfaceViewSupported == false) {
			Toast.makeText(this.getContext(), "播放器仅支持android2.3及以上系统",
					Toast.LENGTH_SHORT).show();
			Log.e(TAG, "player only support android2.3 or above system");
			return;
		}
		if (false == sPlayerIsInitialized) {
			Log.e(TAG, "check SPlayer.isInitialized(mContext)");
			sPlayerIsInitialized = SPlayer.isInitialized(mContext);
		}
		if (false == sPlayerIsInitialized) {
			long leftSize = AndroidUtil.getAvailableInternalRomSize();
			if (leftSize >= AndroidUtil.MIN_PLAYSDK_SOTROAGRE_SIZE) {
				sPlayerIsInitialized = SPlayer.initialize(mContext,
						R.raw.libsplayer);
			} else {
				Toast.makeText(this.getContext(),
						"您的设备存储空间不足,将无法正常播放视频,请清理后重试", Toast.LENGTH_SHORT)
						.show();
				Log.e(TAG, "no enough storage left");
				return;
			}
		}
		if (false == sPlayerIsInitialized) {
			Log.e(TAG, "SPlayer not initialized yet");
			return;
		}
		if (mUri == null || mSurfaceHolder == null) {
			log("openVideo() donothing for NULL mUri=" + mUri
					+ ",mSurfaceHolder=" + mSurfaceHolder);
			return;
		}

		// Intent i = new Intent("com.android.music.musicservicecommand");
		// i.putExtra("command", "pause");
		// mContext.sendBroadcast(i);

		release(false); // if mMediaPlayer not NULL should release!

		try {
			if (mOnVideoOpenedListener != null) {
				mOnVideoOpenedListener.onVideoOpened(mMediaPlayer);
			}
			mDuration = -1;
			mCurrentBufferPercentage = 0;
			mMediaPlayer = new MediaPlayer(mContext);

			// mMediaPlayer.config("debug", 16); //16 24 48
			// mMediaPlayer.config("filter", 4);
			// mMediaPlayer.config("idct", 4);
			// mMediaPlayer.config("skip", 1);

			mMediaPlayer.setOnPreparedListener(mPreparedListener);
			mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
			mMediaPlayer.setOnCompletionListener(mCompletionListener);
			mMediaPlayer.setOnErrorListener(mErrorListener);
			mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
			mMediaPlayer.setOnInfoListener(mInfoListener);
			mMediaPlayer.setOnSeekCompleteListener(mSeekCompleteListener);
			mMediaPlayer.setOnTimedTextListener(mTimedTextListener);
			mMediaPlayer.setDataSource(mUri.toString(), null, null);
			mMediaPlayer.setDisplay(this); // mMediaPlayer.setDisplay(mSurfaceHolder);
			// mMediaPlayer
			// .setVideoChroma(mVideoChroma == MediaPlayer.VIDEOCHROMA_RGB565 ?
			// MediaPlayer.VIDEOCHROMA_RGB565
			// : MediaPlayer.VIDEOCHROMA_RGBA);
			// mMediaPlayer.setScreenOnWhilePlaying(true);
			mMediaPlayer.setWakeMode(mContext,
					PowerManager.SCREEN_BRIGHT_WAKE_LOCK);
			mMediaPlayer.prepareAsync();
			mCurrentState = STATE_PREPARING;

			attachMediaController();

		} catch (IOException ex) {
			LogS.e(TAG, "Unable to open content: " + mUri, ex);
			ex.printStackTrace();
			mCurrentState = STATE_ERROR;
			mTargetState = STATE_ERROR;
			mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_IO, 0);
			return;
		} catch (IllegalArgumentException ex) {
			LogS.e(TAG, "Unable to open content: " + mUri, ex);
			ex.printStackTrace();
			mCurrentState = STATE_ERROR;
			mTargetState = STATE_ERROR;
			mErrorListener.onError(mMediaPlayer,
					MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
			return;
		} catch (IllegalStateException ex) {
			LogS.e(TAG, "Unable to open content: " + mUri, ex);
			ex.printStackTrace();
			mCurrentState = STATE_ERROR;
			mTargetState = STATE_ERROR;
			mErrorListener.onError(mMediaPlayer,
					MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
			return;
		} catch (Exception ex) {
			LogS.e(TAG,
					"catch other exception,Unable to open content: " + mUri, ex);
			ex.printStackTrace();
			mCurrentState = STATE_ERROR;
			mTargetState = STATE_ERROR;
			mErrorListener.onError(mMediaPlayer,
					MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
		}
		log("openVideo out");
	}

	private void release(boolean cleartargetstate) {
		log("release() into, cleartargetstate=" + cleartargetstate);
		if (mMediaPlayer != null) {
			// mMediaPlayer.reset();
			// /Log.d(TAG,"release() ,release MediaPlayer");
			mMediaPlayer.release();
			mMediaPlayer = null;
			mCurrentState = STATE_IDLE;
			if (cleartargetstate)
				mTargetState = STATE_IDLE;
		}
	}

	// /非常暴力地播放指定URI，sufaceview不变，如果mediaplayer已存在则要release
	@Override
	public void setVideoPath(String path) {
		log("setVideoPath " + path);
		if (path == null) {
			mUri = null;
			return;
		}
		setVisibility(View.GONE);
		setVideoURI(Uri.parse(path));
		setVisibility(View.VISIBLE);
	}

	@Override
	public void setVideoURI(Uri uri) {
		log("setVideoURI() into, uri=" + uri);
		mUri = uri;
		if (mUri != null) {
			mSeekWhenPrepared = 0;
			openVideo();
			// requestLayout();
			// invalidate();
		}
	}

	@Override
	public void stopPlayback() {
		log("stopPlayback() into");
		if (mMediaPlayer != null) {
			// mMediaPlayer.stop();
			// /Log.w(TAG,"stopPlayback() ,release MediaPlayer");
			mMediaPlayer.release();
			mMediaPlayer = null;
			mDuration = -1;
			mCurrentState = STATE_IDLE;
			mTargetState = STATE_IDLE;
		}
	}

	@Override
	public void setMediaController(ISinaMediaController controller) {
		log("setMediaController() into");
		if (mMediaController != null)
			mMediaController.hide();
		mMediaController = controller;
		attachMediaController();
	}

	private void attachMediaController() {
		if (mMediaPlayer != null && mMediaController != null) {
			// /LogS.d(TAG, "attachMediaController");
			mMediaController.setMediaPlayer(this);
			View anchorView = getParent() instanceof View ? (View) getParent()
					: this;
			mMediaController.setAnchorView(anchorView);
			mMediaController.setEnabled(isInPlaybackState());
			if (mUri != null) {
				List<String> paths = mUri.getPathSegments();
				String name = paths == null || paths.isEmpty() ? "null" : paths
						.get(paths.size() - 1);
				mMediaController.setFileName(name);
			}
		}
	}

	@Override
	public boolean onTrackballEvent(MotionEvent ev) {
		log("onTrackballEvent() into");
		if (isInPlaybackState() && mMediaController != null)
			toggleMediaControlsVisiblity();
		return false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		log("onKeyDown() into");
		boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK
				&& keyCode != KeyEvent.KEYCODE_VOLUME_UP
				&& keyCode != KeyEvent.KEYCODE_VOLUME_DOWN
				&& keyCode != KeyEvent.KEYCODE_MENU
				&& keyCode != KeyEvent.KEYCODE_CALL
				&& keyCode != KeyEvent.KEYCODE_ENDCALL;
		if (isInPlaybackState() && isKeyCodeSupported
				&& mMediaController != null) {
			if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK
					|| keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
					|| keyCode == KeyEvent.KEYCODE_SPACE) {
				if (mMediaPlayer.isPlaying()) {
					pause();
					mMediaController.show();
				} else {
					start();
					mMediaController.hide();
				}
				return true;
			} else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
				if (!mMediaPlayer.isPlaying()) {
					start();
					mMediaController.hide();
				}
				return true;
			} else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
					|| keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
				if (mMediaPlayer.isPlaying()) {
					pause();
					mMediaController.show();
				}
				return true;
			} else {
				toggleMediaControlsVisiblity();
			}
		}

		return super.onKeyDown(keyCode, event);
	}

	private void toggleMediaControlsVisiblity() {
		if (mMediaController.isShowing()) {
			mMediaController.hide();
		} else {
			mMediaController.show();
		}
	}

	protected boolean isInPlaybackState() {
		return (mMediaPlayer != null && mCurrentState != STATE_ERROR
				&& mCurrentState != STATE_IDLE && mCurrentState != STATE_PREPARING);
	}

	@Override
	public void start() {
		log("start() into");
		if (isInPlaybackState()) {
			// Log.d(TAG, "VideoView start media player");
			mMediaPlayer.start();
			mCurrentState = STATE_PLAYING;
		}
		mTargetState = STATE_PLAYING;
	}

	@Override
	public void pause() {
		log("pause() into");
		if (isInPlaybackState()) {
			if (mMediaPlayer.isPlaying()) {
				Log.d(TAG, "VideoView pause media player");
				mMediaPlayer.pause();
				mCurrentState = STATE_PAUSED;
			}
		}
		mTargetState = STATE_PAUSED;
	}

	@Override
	public void suspend() {
		log("suspend() into");
		if (isInPlaybackState() && mCurrentState != STATE_PAUSED
				&& mTargetState != STATE_PAUSED) {
			release(false);
			mCurrentState = STATE_SUSPEND_UNSUPPORTED;
			LogS.e(TAG, "Unable to suspend video. Release MediaPlayer.");
		} else if (isInPlaybackState()) {
			mCurrentState = STATE_SUSPEND;
		}
	}

	@Override
	public void resume() {
		log("resume() into");
		if (mSurfaceHolder == null && mCurrentState == STATE_SUSPEND) {
			mTargetState = STATE_RESUME;
		} else if (mCurrentState == STATE_SUSPEND_UNSUPPORTED) {
			openVideo();
		}
	}

	@Override
	public long getDuration() {
		log("getDuration() into");
		if (isInPlaybackState()) {
			if (mDuration > 0)
				return mDuration;
			try {
				mDuration = mMediaPlayer.getDuration();
			} catch (IllegalStateException ex) {
				LogS.e(TAG, "getCurrentPosition() getDuration state!");
			}
			return mDuration;
		}
		mDuration = -1;
		return mDuration;
	}

	@Override
	public long getCurrentPosition() {
		log("getCurrentPosition() into");
		if (isInPlaybackState() && mMediaPlayer != null) {
			int pos = 0;
			try {
				pos = mMediaPlayer.getCurrentPosition();
			} catch (IllegalStateException ex) {
				LogS.e(TAG, "getCurrentPosition() IllegalStateException state!");
			}
			return pos;
		}
		return 0;
	}

	@Override
	public void seekTo(long msec) {
		log("seekTo() into,msec=" + msec);
		if (isInPlaybackState()) {
			// Log.d(TAG, "VideoView seekTo msec=" + msec);
			mMediaPlayer.seekTo((int) msec);
			mSeekWhenPrepared = 0;
		} else {
			mSeekWhenPrepared = msec;
		}
	}

	@Override
	public boolean isPlaying() {
		log("isPlaying() into");
		return isInPlaybackState() && mMediaPlayer.isPlaying();
	}

	@Override
	public int getBufferPercentage() {
		log("getBufferPercentage() into");
		if (mMediaPlayer != null)
			return mCurrentBufferPercentage;
		return 0;
	}

	@Override
	public void setVolume(float leftVolume, float rightVolume) {
		log("setVolume() into");
		if (mMediaPlayer != null) {
			mMediaPlayer.setVolume(leftVolume, rightVolume);
		}
	}

	public float getVideoAspectRatio() {
		log("getVideoAspectRatio() into");
		return mVideoAspectRatio;
	}

	@Override
	public boolean canPause() {
		return true;
	}

	@Override
	public boolean canSeekBackward() {
		return true;
	}

	@Override
	public boolean canSeekForward() {
		return true;
	}

	@Override
	public void setBufferSize(int bufSize) {
		log("setBufferSize() into,bufSize=" + bufSize);
		if (mMediaPlayer != null)
			mMediaPlayer.setBufferSize(bufSize);
	}

	@Override
	public boolean isBuffering() {
		log("isBuffering() into");
		if (mMediaPlayer != null)
			return mMediaPlayer.isBuffering();
		return false;
	}

	@Override
	public void setOnPreparedListener(MediaPlayer.OnPreparedListener listener) {
		mOnPreparedListener = listener;
	}

	MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {

		@Override
		public void onPrepared(MediaPlayer mp) {
			log("onPrepared() into，");
			mCurrentState = STATE_PREPARED;
			int mTmpTargetState = mTargetState;// mTargetState = STATE_PLAYING;

			// Get the capabilities of the player for this stream
			if (mOnPreparedListener != null) {
				mOnPreparedListener.onPrepared(mMediaPlayer);
			}
			if (mMediaController != null)
				mMediaController.setEnabled(true);
			mVideoWidth = mp.getVideoWidth();
			mVideoHeight = mp.getVideoHeight();
			// mVideoAspectRatio = mp.getVideoAspectRatio();

			long seekToPosition = mSeekWhenPrepared;

			// //崩溃，状态机
			if (seekToPosition != 0)
				seekTo((int) seekToPosition);

			// if (mVideoWidth != 0 && mVideoHeight != 0) {
			// setVideoLayout(mVideoLayout, mAspectRatio);
			// if (mSurfaceWidth == mVideoWidth
			// && mSurfaceHeight == mVideoHeight) {
			// if (mTargetState == STATE_PLAYING) {
			// start();
			// mNeedShowMediaController = true;
			// // if (mMediaController != null) {
			// // mMediaController.show();
			// // }
			// } else if (!isPlaying()
			// && (seekToPosition != 0 || getCurrentPosition() > 0)) {
			// if (mMediaController != null) {
			// mMediaController.show(0);
			// }
			// }
			// }
			// }
			// else
			// if (mTargetState == STATE_PLAYING) {
			// log("onPrepared() to start");
			// start();
			// } else {
			// log("not start on prepared");
			// }
			if (mTmpTargetState == STATE_IDLE) {
				start();
			} else if (mTmpTargetState == STATE_PAUSED) {
				pause();
			} else {
				// mTargetState = STATE_PREPARED;
				log("not start on prepared");
			}

			// invalidate();
		}
	};

	@Override
	public void setOnVideoSizeChangedListener(
			MediaPlayer.OnVideoSizeChangedListener listener) {
		mOnVideoSizeChangedListener = listener;
	}

	private MediaPlayer.OnVideoSizeChangedListener mSizeChangedListener = new MediaPlayer.OnVideoSizeChangedListener() {

		@Override
		public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
			log("onVideoSizeChanged() into: " + width + " " + height);
			mVideoWidth = mp.getVideoWidth();
			mVideoHeight = mp.getVideoHeight();
			// / mVideoAspectRatio = mp.getVideoAspectRatio();
			if (mVideoWidth > 0 && mVideoHeight > 0) {
				// /setVideoLayout(mVideoLayout, mAspectRatio);
				if (mOnVideoSizeChangedListener != null) {
					mOnVideoSizeChangedListener.onVideoSizeChanged(
							mMediaPlayer, mVideoWidth, mVideoHeight);
				}
			}
		}
	};

	@Override
	public void setOnCompletionListener(
			MediaPlayer.OnCompletionListener listener) {
		mOnCompletionListener = listener;
	}

	private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {

		@Override
		public void onCompletion(MediaPlayer mp) {
			log("onCompletion() into");
			mCurrentState = STATE_PLAYBACK_COMPLETED;
			mTargetState = STATE_PLAYBACK_COMPLETED;
			if (mOnCompletionListener != null)
				mOnCompletionListener.onCompletion(mMediaPlayer);
		}
	};

	@Override
	public void setOnErrorListener(MediaPlayer.OnErrorListener listener) {
		mOnErrorListener = listener;
	}

	private MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener() {

		@Override
		public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
			log("onError() into:" + framework_err + " " + impl_err);
			mCurrentState = STATE_ERROR;
			mTargetState = STATE_ERROR;
			if (mOnErrorListener != null) {
				if (mOnErrorListener.onError(mMediaPlayer, framework_err,
						impl_err))
					return true;
			}
			return true;
		}
	};

	@Override
	public void setOnBufferingUpdateListener(
			MediaPlayer.OnBufferingUpdateListener listener) {
		mOnBufferingUpdateListener = listener;
	}

	private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {

		@Override
		public void onBufferingUpdate(MediaPlayer mp, int percent) {
			log("onBufferingUpdate(): " + " percent=" + percent);
			mCurrentBufferPercentage = percent;
			if (mOnBufferingUpdateListener != null)
				mOnBufferingUpdateListener.onBufferingUpdate(mMediaPlayer,
						percent);
		}
	};

	@Override
	public void setOnInfoListener(MediaPlayer.OnInfoListener listener) {
		mOnInfoListener = listener;
	}

	private MediaPlayer.OnInfoListener mInfoListener = new MediaPlayer.OnInfoListener() {

		@Override
		public boolean onInfo(MediaPlayer mp, int what, int extra) {
			log("onInfo: " + what + " " + extra);
			if (mOnInfoListener != null) {
				mOnInfoListener.onInfo(mMediaPlayer, what, extra);
			}
			if (mMediaPlayer != null) {
				if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
					// /mMediaPlayer.pause(); ///seekto导致不同错乱，注释
				} else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
					if (isInPlaybackState() && mCurrentState != STATE_SUSPEND) {
						// /mMediaPlayer.start();
						if (mNeedShowMediaController) {
							mNeedShowMediaController = false;
							if (mMediaController != null) {
								mMediaController.show();
							}
						}
					}
				}
			}
			return true;
		}
	};

	@Override
	public void setOnSeekCompleteListener(
			MediaPlayer.OnSeekCompleteListener listener) {
		mOnSeekCompleteListener = listener;
	}

	private MediaPlayer.OnSeekCompleteListener mSeekCompleteListener = new MediaPlayer.OnSeekCompleteListener() {

		@Override
		public void onSeekComplete(MediaPlayer mp) {
			log("onSeekComplete() into");
			if (mOnSeekCompleteListener != null)
				mOnSeekCompleteListener.onSeekComplete(mMediaPlayer);
		}
	};

	@Override
	public void setOnTimedTextListener(MediaPlayer.OnTimedTextListener listener) {
		mOnTimedTextListener = listener;
	}

	private MediaPlayer.OnTimedTextListener mTimedTextListener = new MediaPlayer.OnTimedTextListener() {

		@Override
		public void onTimedText(MediaPlayer mp, TimedText text) {
			log("onSubtitleUpdate: do nothing");
			// if (mOnTimedTextListener != null)
			// mOnTimedTextListener.onTimedText(text);
		}
	};

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// log( "onMeasure() into");
		// int widthMeasureSpecSize = MeasureSpec.getSize(widthMeasureSpec);
		// int heightMeasureSpecSize = MeasureSpec.getSize(heightMeasureSpec);
		// int widthMesureMode = MeasureSpec.getMode(widthMeasureSpec);
		// int heightMesureMode = MeasureSpec.getMode(heightMeasureSpec);
		// // MeasureSpec.AT_MOST -2147483648 ; //MeasureSpec.EXACTLY
		// 1073741824;//MeasureSpec.UNSPECIFIED 0;
		// log("onMeasure() into widthMeasureSpec  " + widthMeasureSpec +
		// " , heightMeasureSpec = "+ heightMeasureSpec +
		// ", widthMeasureSpecSize=" + widthMeasureSpecSize +
		// ", heightMeasureSpecSize=" + heightMeasureSpecSize +
		// ", widthMesureMode=" + widthMesureMode +
		// ", heightMesureMode=" + heightMesureMode);
		// if(widthMeasureSpecSize==800){
		// log("onMeasure() set to 800x480");
		// setMeasuredDimension(800,480);
		// }else{
		// setMeasuredDimension(getDefaultSize(mVideoWidth,
		// widthMeasureSpec),getDefaultSize(mVideoHeight, heightMeasureSpec));
		// }
		//

		setMeasuredDimension(
				getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
				getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec));
	}

	@Override
	public void beginChangeParentView() {
		log("beginChangeParentView do nothing");

		mChangeParentView = true;
	}

	@Override
	public void endChangeParentView() {
		log("endChangeParentView do nothing");

		// mLastBitmap = null;
		mChangeParentView = false;
	}

	@Override
	public void requestVideoLayout() {
		if (mCurrentState == STATE_ERROR || mCurrentState == STATE_IDLE
				|| mCurrentState == STATE_PREPARING) {
			return;
		}
		log("requestVideoLayout into");

		// setVideoLayout(mVideoLayout, mAspectRatio);
	}

	@Override
	public void configScaleType(int type) {
		mVideoLayout = type;
	}

	@Override
	public boolean isPlayEnd() {
		log("isPlayEnd() into ");
		if (mMediaPlayer != null && mCurrentState == STATE_PLAYBACK_COMPLETED) {
			return true;
		}
		return false;
	}

	// public interface OnVideoOpenedListener{
	// void onVideoOpened();
	// }
	// private OnVideoOpenedListener mOnVideoOpenedListener;
	@Override
	public void setOnVideoOpenedListener(MediaPlayer.OnVideoOpenedListener l) {
		mOnVideoOpenedListener = l;
	}

	// /////////////////////////////////////

	@Override
	public boolean gatherTransparentRegion(Region region) {
		return true;
	}

}
