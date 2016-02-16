package com.sina.sinavideo.coreplayer.splayer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Region;
import android.media.AudioManager;
import android.media.TimedText;
import android.net.Uri;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;

import com.sina.sinavideo.coreplayer.ISinaMediaController;
import com.sina.sinavideo.coreplayer.ISinaVideoView;
import com.sina.sinavideo.coreplayer.R;
import com.sina.sinavideo.coreplayer.util.AndroidUtil;
import com.sina.sinavideo.coreplayer.util.LogS;

import java.io.IOException;
import java.util.List;
import java.util.Map;

// import android.opengl.GLSurfaceView;

public class VideoViewHard extends SurfaceView implements ISinaVideoView {

	private static String TAG = "SPlayerVideoViewHard";
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
	private long mSeekWhenPrepared = 0; // recording the seek position while
	// preparing
	private Context mContext;
	private Map<String, String> mHeaders;

	private boolean mChangeParentView = false; // /unsupport
	private boolean mNeedShowMediaController = false;

	// /private Bitmap mLastBitmap;
	// //////////////////////////////////////

	private static final boolean DEBUG = false;
	// True if onSurfaceCreated has been called.
	// private boolean surfaceCreated = false;
	// private boolean openGLCreated = false;
	// True if NativeFunctionsRegistered has been called.
	// private boolean nativeFunctionsRegisted = false;
	// private ReentrantLock nativeFunctionLock = new ReentrantLock();
	// Address of Native object that will do the drawing.
	// private long nativeObject = 0;
	// private int viewWidth = 0;
	// private int viewHeight = 0;

	// private Handler mHandler = null; //for //touch event

	// /private boolean surfaceChanged = false;
	private static boolean sPlayerIsInitialized = false;

	private void log(String str) {
		if (DEBUG) {
			LogS.d(TAG, "VideoViewHard: " + str);
		}
	}

	public VideoViewHard(Context context) {
		super(context);

		LogS.d(TAG, "VideoViewHard(Context context) into");

		// mHandler = hand;
		initVideoViewHard(context);
	}

	// /////////////////////////////////////
	private void initVideoViewHard(Context context) {
		log("initVideoViewHard() into");

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

	@Override
	protected void onAttachedToWindow() {
		// LogS.i("videoView", "onAttachedToWindow " + getWindowToken() +
		// " , mChangeParentView = " + mChangeParentView);
		if (mChangeParentView) {

		} else {
			super.onAttachedToWindow();
		}
	};

	@Override
	protected void onDetachedFromWindow() {
		// LogS.i("videoView", "onDetachedFromWindow " + getWindowToken() +
		// " , mChangeParentView = " + mChangeParentView);
		if (mChangeParentView) {

		} else {
			super.onDetachedFromWindow();
		}
	};

	@Override
	protected void onWindowVisibilityChanged(int visibility) {
		// LogS.i("videoView", "onWindowVisibilityChanged " + getWindowToken() +
		// " , mChangeParentView = "
		// + mChangeParentView);
		if (mChangeParentView) {

		} else {
			super.onWindowVisibilityChanged(visibility);
		}
	};

	SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			log("surfaceCreated() into, mCurrentState " + mCurrentState
					+ " mTargetState " + mTargetState);
			log("surfaceCreated()  holder=[" + holder
					+ "],holder.getSurface()=[" + holder.getSurface() + "]");
			if (mSurfaceHolder != null) { // /create first time
				mSurfaceHolder = holder;
				log("surfaceCreated() SurfaceHolder already create, retun! ");
				return;
			} else {
				mSurfaceHolder = holder;
			}
			if (mChangeParentView) {
				setVideoLayout(mVideoLayout, mAspectRatio);
				endChangeParentView();
			} else {
				if (mMediaPlayer != null && mCurrentState == STATE_SUSPEND
						&& mTargetState == STATE_RESUME) {
					log("surfaceCreated +++++++++ resume");
					resume();
				} else {
					log("surfaceCreated +++++++++ openVideo");
					openVideo();
				}
			}

			log("surfaceCreated() out");
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int w,
				int h) {
			log("surfaceChanged() into，w=" + w + ",h=" + h);
			mSurfaceWidth = w;
			mSurfaceHeight = h;

			boolean isValidState = (mTargetState == STATE_PLAYING);
			boolean hasValidSize = (mVideoWidth == w && mVideoHeight == h);
			if (mMediaPlayer != null && isValidState && hasValidSize) {
				if (mSeekWhenPrepared != 0)
					seekTo((int) mSeekWhenPrepared);
				// start();
			}

		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			log("surfaceDestroyed() into,mCurrentState=" + mCurrentState
					+ ",mTargetState=" + mTargetState);

			mSurfaceHolder = null;

			if (isInPlaybackState()) {
				log("surfaceDestroyed() stop playback, surface unsuport SUSPEND");
				stopPlayback();
			}

			log("surfaceDestroyed() out");
		}
	};

	private void openVideo() {
		log("openVideo " + mUri);

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

		Intent i = new Intent("com.android.music.musicservicecommand");
		i.putExtra("command", "pause");
		mContext.sendBroadcast(i);

		release(false); // if mMediaPlayer not NULL should release!

		try {
			if (mOnVideoOpenedListener != null) {
				mOnVideoOpenedListener.onVideoOpened(mMediaPlayer);
			}
			mDuration = -1;
			mCurrentBufferPercentage = 0;
			mMediaPlayer = new MediaPlayer(mContext, true);

			// mMediaPlayer.config("debug", MediaPlayer.SPLAYER_TRACE_ERROR);
			// //ERROR 16 24 DEBUG 48

			mMediaPlayer.setOnPreparedListener(mPreparedListener);
			mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
			mMediaPlayer.setOnCompletionListener(mCompletionListener);
			mMediaPlayer.setOnErrorListener(mErrorListener);
			mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
			mMediaPlayer.setOnInfoListener(mInfoListener);
			mMediaPlayer.setOnSeekCompleteListener(mSeekCompleteListener);
			mMediaPlayer.setOnTimedTextListener(mTimedTextListener);
			mMediaPlayer.setDataSource(mUri.toString(), null, null);
			log("openVideo() mSurfaceHolder=[" + mSurfaceHolder
					+ "],mSurfaceHolder.getSurface()=["
					+ mSurfaceHolder.getSurface() + "]");
			mMediaPlayer.setSurface(mSurfaceHolder.getSurface()); // mMediaPlayer.setSurfaceView(this);
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
			Log.d(TAG, "release() ,release MediaPlayer");
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
			requestLayout();
			invalidate();
		}
	}

	@Override
	public void stopPlayback() {
		log("stopPlayback() into");
		if (mMediaPlayer != null) {
			// mMediaPlayer.stop();
			// 在这加上的原因是surfaceview消耗时候记住当前播放进度
			// 当surfaceview再创建，再播放同一个视频时能跳到上次销毁处的播放进度
			int pos = mMediaPlayer.getCurrentPosition();
			if (pos > 0)
				mSeekWhenPrepared = pos;
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
			try {
				mMediaPlayer.start();
				mCurrentState = STATE_PLAYING;
			} catch (IllegalStateException ex) {
				log("start() IllegalStateException");
			}
		}
		mTargetState = STATE_PLAYING;
	}

	@Override
	public void pause() {
		log("pause() into");
		if (isInPlaybackState()) {
			if (mMediaPlayer.isPlaying()) {
				log("pause media player");
				try {
					mMediaPlayer.pause();
					mCurrentState = STATE_PAUSED;
				} catch (IllegalStateException ex) {
					log("pause() IllegalStateException");
				}
			}
		}
		mTargetState = STATE_PAUSED;
	}

	@Override
	public void suspend() {
		log("suspend() into");
		if (isInPlaybackState() && mCurrentState != STATE_PAUSED
				&& mTargetState != STATE_PAUSED) {
			log("suspend() releasing");
			release(false);
			mCurrentState = STATE_SUSPEND_UNSUPPORTED;
			LogS.e(TAG, "Unable to suspend video. Release MediaPlayer.");
		} else if (isInPlaybackState()) {
			log("suspend() mCurrentState->STATE_SUSPEND");
			mCurrentState = STATE_SUSPEND;
		} else {
			log("suspend() do nothing");
		}
	}

	@Override
	public void resume() {
		log("resume() into");
		if (mSurfaceHolder == null && mCurrentState == STATE_SUSPEND) {
			log("resume() mTargetState->STATE_RESUME");
			mTargetState = STATE_RESUME;
		} else if (mCurrentState == STATE_SUSPEND_UNSUPPORTED) {
			log("resume() call openVideo()");
			openVideo();
		} else {
			log("resume() do nothing");
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
				LogS.e(TAG, "getDuration() IllegalStateException state!");
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
			try {
				mMediaPlayer.seekTo((int) msec);
				mSeekWhenPrepared = 0;
			} catch (IllegalStateException ex) {
				log("seekTo() IllegalStateException");
			}
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
			// /fix x86 bug 原lai是这里!
			if (mOnPreparedListener != null) {
				mOnPreparedListener.onPrepared(mMediaPlayer);
			}
			if (mMediaController != null)
				mMediaController.setEnabled(true);
			mVideoWidth = mp.getVideoWidth();
			mVideoHeight = mp.getVideoHeight();
			mVideoAspectRatio = mp.getVideoAspectRatio();

			long seekToPosition = mSeekWhenPrepared;
			// //崩溃，状态机
			if (seekToPosition != 0) {
				log("onPrepared() seekToPosition=" + seekToPosition);
				seekTo((int) seekToPosition);
			}

			if (mVideoWidth != 0 && mVideoHeight != 0) {
				setVideoLayout(mVideoLayout, mAspectRatio);
			}
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
			if (mTmpTargetState == STATE_IDLE) {
				start();
			} else if (mTmpTargetState == STATE_PAUSED) {
				pause();
			} else {
				// mTargetState = STATE_PREPARED;
				log("not start on prepared");
			}

			invalidate();
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
			mVideoAspectRatio = mp.getVideoAspectRatio();
			if (mVideoWidth > 0 && mVideoHeight > 0) {
				setVideoLayout(mVideoLayout, mAspectRatio);
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
		log("onMeasure() into" + "widthMeasureSpec  "
				+ MeasureSpec.getSize(widthMeasureSpec)
				+ " , heightMeasureSpec = "
				+ MeasureSpec.getSize(heightMeasureSpec));
		// setMeasuredDimension(getDefaultSize(mVideoWidth, widthMeasureSpec),
		// getDefaultSize(mVideoHeight, heightMeasureSpec));
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

	public void setVideoLayout(int layout, float aspectRatio) {
		log("setVideoLayout() into");
		LayoutParams lp = getLayoutParams();
		DisplayMetrics disp = mContext.getResources().getDisplayMetrics();
		int windowWidth = disp.widthPixels, windowHeight = disp.heightPixels;
		View p = (View) getParent();
		if (p == null) {
			return;
		}

		// LogS.d(TAG,
		// "disp.widthPixels="+disp.widthPixels+",disp.heightPixels="+disp.heightPixels+
		// ",p.getWidth()="+ p.getWidth() + "p.getHeight()="+ p.getHeight());
		LayoutParams p_lp = p.getLayoutParams();
		if (p_lp != null) {

			if (p_lp.width == -1) {
				windowWidth = disp.widthPixels;// /windowWidth =
												// p.getMeasuredWidth();
												// LogS.d(TAG,
												// "p_lp.width==-1,p.getMeasuredWidth()="
												// + windowWidth);
			} else {
				windowWidth = p_lp.width;
			}

			if (p_lp.height == -1) {
				windowHeight = disp.heightPixels;// /windowHeight =
													// p.getMeasuredHeight();
													// LogS.d(TAG,
													// "p_lp.height==-1,p.getMeasuredHeight()="
													// + windowHeight);
			} else {
				windowHeight = p_lp.height;
			}
		} else {
			// LogS.d(TAG, "p_lp NULL,windowWidth=" + disp.widthPixels +
			// ",windowHeight=" + disp.heightPixels);
		}
		// LogS.d(TAG, "setVideoLayout mSurfaceView : width = " + lp.width +
		// " , height = " + lp.height);
		// LogS.d(TAG, "setVideoLayout parent width = " + windowWidth +
		// " , height = " + windowHeight);
		float windowRatio = windowWidth / (float) windowHeight;
		float videoRatio = aspectRatio <= 0.01f ? mVideoAspectRatio
				: aspectRatio;
		if (videoRatio < 0.01f) {
			LogS.d(TAG, "video Ratio error " + videoRatio);
			return;
		}
		mSurfaceHeight = mVideoHeight;
		mSurfaceWidth = mVideoWidth;
		if (VIDEO_SCALE_ORIGIN == layout && mSurfaceWidth < windowWidth
				&& mSurfaceHeight < windowHeight) {
			lp.width = (int) (mSurfaceHeight * videoRatio);
			lp.height = mSurfaceHeight;
			setLayoutParams(lp);
			// /getHolder().setFixedSize(mSurfaceWidth, mSurfaceHeight);
		} else if (layout == VIDEO_SCALE_CENTER_CROP) {
			lp.width = windowRatio > videoRatio ? windowWidth
					: (int) (videoRatio * windowHeight);
			lp.height = windowRatio < videoRatio ? windowHeight
					: (int) (windowWidth / videoRatio);
			setLayoutParams(lp);
			// /getHolder().setFixedSize(mSurfaceWidth, mSurfaceHeight);
		} else {
			boolean full = layout == VIDEO_SCALE_FIT_STRENTH;
			lp.width = (full || windowRatio < videoRatio) ? windowWidth
					: videoRatio == 0 ? windowWidth
							: (int) (videoRatio * windowHeight);
			lp.height = (full || windowRatio > videoRatio) ? windowHeight
					: videoRatio == 0 ? windowHeight
							: (int) (windowWidth / videoRatio);
			setLayoutParams(lp);
			// /getHolder().setFixedSize(mSurfaceWidth, mSurfaceHeight);
		}
		// LogS.d(TAG, "setVideoLayout : windowRatio = " + windowRatio +
		// " , videoRatio = " + videoRatio + " , aspectRatio = " + aspectRatio);
		// LogS.d(TAG, "setVideoLayout : lp.width = " + lp.width +
		// " , lp.height = " + lp.height + " , mSurfaceWidth = " + mSurfaceWidth
		// + " , mSurfaceHeight = " + mSurfaceHeight);
		// getHolder().setFixedSize(mSurfaceWidth, mSurfaceHeight);
		// LogS.d(TAG,
		// "VIDEO: %dx%dx%f, Surface: %dx%d, LP: %dx%d, Window: %dx%dx%f",
		// mVideoWidth, mVideoHeight, mVideoAspectRatio, mSurfaceWidth,
		// mSurfaceHeight, lp.width, lp.height, windowWidth, windowHeight,
		// windowRatio);
		mVideoLayout = layout;
		mAspectRatio = aspectRatio;
		log("setVideoLayout() out");
	}

	@Override
	public void requestVideoLayout() {
		log("requestVideoLayout into");
		if (mCurrentState == STATE_ERROR || mCurrentState == STATE_IDLE
				|| mCurrentState == STATE_PREPARING) {
			return;
		}

		setVideoLayout(mVideoLayout, mAspectRatio);
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
