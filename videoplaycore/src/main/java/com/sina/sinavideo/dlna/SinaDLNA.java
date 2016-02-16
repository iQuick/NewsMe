package com.sina.sinavideo.dlna;

import java.lang.ref.WeakReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.sina.sinavideo.coreplayer.splayer.MediaPlayer;
import com.sina.sinavideo.coreplayer.splayer.SPlayer;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class SinaDLNA {

    private static final String TAG = "SinaDLNA";
    static {
        try {
        	
        	String LIB_ROOT = SPlayer.getLibraryPath();
            Log.d(TAG, "loading library");
            System.load(LIB_ROOT + "libsinadlna_jni.so");
            _init();
            Log.d(TAG, "loading library success");
        } catch (Exception ex) {
            Log.e(TAG, "load library fail");
        }
    }

    public SinaDLNA(Context contxt) {

        Looper looper;
        if ((looper = Looper.myLooper()) != null) {
            mEventHandler = new EventHandler(this, looper);
        } else if ((looper = Looper.getMainLooper()) != null) {
            mEventHandler = new EventHandler(this, looper);
        } else {
            mEventHandler = null;
        }

        mContext = contxt;
    }

    @Override
    protected void finalize() {
        _finalize();
    }

    
    /**
     * DLNA 建立
     */
    public final void setup() {
        _setup(new WeakReference<SinaDLNA>(this));
    }

    /**
     * DLNA 释放
     */
    public final void release() {
        _release();
    }

    /**
     * 操作命令：选择一个播放设备
     * 参数: uuid 设备ID 
     * 返回值：0 成功 -1失败
     */
    public native final int setMediaRender(String uuid) throws IllegalStateException, IllegalArgumentException,
            RuntimeException;

    /**
     * 操作命令：获得当前播放设备的UUID 
     * 参数: 返回值：当前设备的ID uuid
     */
    public native final String getMediaRender() throws IllegalStateException;

    /**
     * 操作命令：设置播放URL 
     * 参数: url 播放文件地址 ； didl 文件信息
     *  返回值：结果异步通知
     */
    public native final void open(String url, String didl) throws IllegalStateException, IllegalArgumentException,
            RuntimeException;

    /**
     * 操作命令：开始播放
     * 参数: 
     * 返回值：结果异步通知
     */
    public native final void play() throws IllegalStateException;

    /**
     * 操作命令：停止播放 
     * 参数: 
     * 返回值：结果异步通知
     */
    public native final void pause() throws IllegalStateException;

    /**
     * 操作命令：播放进度 
     * 参数: msec 单位毫秒 
     * 返回值：结果异步通知
     */
    public native final void seek(int msec) throws IllegalStateException;

    /**
     * 操作命令：播放停止 
     * 参数: 
     * 返回值：结果异步通知
     */
    public native final void stop() throws IllegalStateException;

    /**
     * 操作命令：设备静音控制 
     * 参数: mute true 静音； false有音 
     * 返回值：结果异步通知
     */
    public native final void setMute(boolean mute) throws IllegalStateException;

    /**
     * 操作命令：获得当前静音设置
     * 参数: 
     * 返回值：结果异步通知
     */
    public native final void getMute() throws IllegalStateException;

    /**
     * 操作命令：获得设备最小音量 
     * 参数: 
     * 返回值：最小音量 -1 失败; >=0 期望值
     */
    public native final int getVolumeMin() throws IllegalStateException;

    /**
     * 操作命令：获得设备最大音量 
     * 参数: 
     * 返回值：最大音量 -1 失败; >0 期望值
     */
    public native final int getVolumeMax() throws IllegalStateException;

    /**
     * 操作命令：设置设备音量 
     * 参数: vol音量大小 范围在最小音量和最大音量之间 
     * 返回值：
     */
    public native final void setVolume(int vol) throws IllegalStateException;

    /**
     * 操作命令：获得当前设备音量设置 
     * 参数: 
     * 返回值：结果异步通知
     */
    public native final void getVolume() throws IllegalStateException;

    /**
     * 操作命令：获得当前播放文件的总时长 
     * 参数: 
     * 返回值：结果异步通知
     */
    public native final void getDuration() throws IllegalStateException;

    /**
     * 操作命令：获得当前播放时间位置 
     * 参数: 
     * 返回值：结果异步通知
     */
    public native final void getPosition() throws IllegalStateException;

    /**
     * DLNA事件通知接口
     */
    public interface SinaDLNAListener {

        /**
         * 设备添加 
         * 参数 uuid 设备ID name 设备名
         */
        public void onMediaRenderAdded(String uuid, String name);

        /**
         * 设备移除 
         * 参数 uuid 设备ID name 设备名
         */
        public void onMediaRenderRemoved(String uuid, String name);

        /**
         * 设备状态变化 
         * 参数 name 变量名 value 设备名
         */
        public void onMediaRenderStateChanged(String name, String value);

        /**
         * 设置播放URL命令的结果事件 
         * 参数：result 0 成功 -1失败
         */
        public void onOpen(int result);

        /**
         * 播放开始命令的结果事件 
         * 参数：result 0 成功 -1失败
         */
        public void onPlay(int result);

        /**
         * 播放暂停命令的结果事件 
         * 参数：result 0 成功 -1失败
         */
        public void onPause(int result);

        /**
         * 播放停止命令的结果事件 
         * 参数：result 0 成功 -1失败
         */
        public void onStop(int result);

        /**
         * 播放进度命令的结果事件 
         * 参数：result 0 成功 -1失败
         */
        public void onSeek(int result);

        /**
         * 静音设置命令的结果事件 
         * 参数：result 0 成功 -1失败
         */
        public void onSetMute(int result);

        /**
         * 获得静音设置命令的结果事件 
         * 参数：result 0 成功 -1失败 mute true 静音 false 有音
         */
        public void onGetMute(int result, boolean mute);

        /**
         * 音量设置命令的结果事件 
         * 参数：result 0 成功 -1失败
         */
        public void onSetVolume(int result);

        /**
         * 获得音量设置命令的结果事件 
         * 参数：result 0 成功 -1失败 vol 当前音量
         */
        public void onGetVolume(int result, int vol);

        /**
         * 获得视频总时长命令的结果事件 
         * 参数：result 0 成功 -1失败 msec 总时长,毫秒
         */
        public void onGetDuration(int result, int msec);

        /**
         * 获得当前时间位置命令的结果事件 
         * 参数：result 0 成功 -1失败 msec 当前的播放时间位置,毫秒
         */
        public void onGetPosition(int result, int msec);

    }

    public void setSinaDLNAListener(SinaDLNAListener listener) {
        mSinaDLNAListener = listener;
    }

    public static String buildDIDL(String url){
        Log.d(TAG, "buildDIDL() url=[" + url +"]" + ",md5=[" + buildMD5(url) + "]");
        StringBuffer didl = new StringBuffer();
        didl.append("<DIDL-Lite xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\">");
        didl.append("<item id=\""+ buildMD5(url) +"\" parentID=\"-1\" restricted=\"1\">");
        didl.append("<upnp:storageMedium>UNKNOWN</upnp:storageMedium>");
        didl.append("<upnp:writeStatus>UNKNOWN</upnp:writeStatus>");
        didl.append("<dc:title>"+"新浪DLNA"+"</dc:title>");
        didl.append("<upnp:class>object.item.videoItem.movie</upnp:class>");
   
        if(url.contains(".m3u8")){
            didl.append("<res protocolInfo=\"http-get:*:application/x-mpegURL:*\">"+url+"</res>");
        }else if(url.contains(".mp4")){
            didl.append("<res protocolInfo=\"http-get:*:video/mp4:*\">"+url+"</res>");
        }else{
            didl.append("<res protocolInfo=\"http-get:*:*:*\">"+url+"</res>"); 
        }
        didl.append("</item>");
        didl.append("</DIDL-Lite>");
        return didl.toString();
    }
    
    private static StringBuilder buildMD5(String path) {
        StringBuilder sb = new StringBuilder();
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(path.getBytes());
            for (byte by : md5.digest()) {
                sb.append(String.format("%02X", by));//将生成的字节MD５值转换成字符串
            }
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            return new StringBuilder("000000000");
        } 
        return sb;
     }
  
    /**
     * 事件宏定义
     */
    private static final int SINA_DLNA_MR_EVENT = 1; // 设备事件
    private static final int SINA_DLNA_MR_STATE = 2; // 设备状态
    private static final int SINA_DLNA_CMD_EVENT = 3; // 命令结果事件

    private static final int SINA_DLNA_MR_EVENT_ADDED = 1; // 添加一个新的设备
    private static final int SINA_DLNA_MR_EVENT_REMOVED = 2; // 移除一个已有设备

    private static final int SINA_DLNA_CMD_EVENT_OPEN = 1; // 设置播放URL命令的结果事件
    private static final int SINA_DLNA_CMD_EVENT_PLAY = 2; // 播放开始命令的结果事件
    private static final int SINA_DLNA_CMD_EVENT_PAUSE = 3; // 播放暂停命令的结果事件
    private static final int SINA_DLNA_CMD_EVENT_STOP = 4; // 播放停止命令的结果事件
    private static final int SINA_DLNA_CMD_EVENT_SEEK = 5; // 播放进度命令的结果事件
    private static final int SINA_DLNA_CMD_EVENT_SET_MUTE = 6; // 静音设置命令的结果事件
    private static final int SINA_DLNA_CMD_EVENT_GET_MUTE = 7; // 获得静音设置命令的结果事件
    private static final int SINA_DLNA_CMD_EVENT_SET_VOLUME = 8; // 音量设置命令的结果事件
    private static final int SINA_DLNA_CMD_EVENT_GET_VOLUME = 9; // 获得音量设置命令的结果事件
    private static final int SINA_DLNA_CMD_EVENT_DURATION = 10; // 获得视频总时长命令的结果事件
    private static final int SINA_DLNA_CMD_EVENT_POSITION = 11; // 获得当前时间位置命令的结果事件

    private class EventHandler extends Handler {

        private SinaDLNA mDLNA;

        public EventHandler(SinaDLNA dlna, Looper looper) {
            super(looper);
            mDLNA = dlna;
        }

        @Override
        public void handleMessage(Message msg) {
            if (mDLNA.mNativeContext == 0) {
                Log.w(TAG, "mDLNA went away with unhandled events");
                return;
            }
            switch (msg.what) {
                case SINA_DLNA_MR_EVENT :
                    if (mSinaDLNAListener != null) {
                        switch (msg.arg1) {
                            case SINA_DLNA_MR_EVENT_ADDED : {
                                MediaRenderBean mr = (MediaRenderBean) msg.obj;
                                mSinaDLNAListener.onMediaRenderAdded(mr.getUuid(), mr.getName());
                                return;
                            }
                            case SINA_DLNA_MR_EVENT_REMOVED : {
                                MediaRenderBean mr = (MediaRenderBean) msg.obj;
                                mSinaDLNAListener.onMediaRenderRemoved(mr.getUuid(), mr.getName());
                                return;
                            }
                            default :
                                Log.e(TAG, "Unknown mr type, arg1=" + msg.arg1);
                                return;
                        }
                    }
                    return;

                case SINA_DLNA_CMD_EVENT :
                    if (mSinaDLNAListener != null) {
                        switch (msg.arg1) {
                            case SINA_DLNA_CMD_EVENT_OPEN :
                                mSinaDLNAListener.onOpen(msg.arg2);
                                return;
                            case SINA_DLNA_CMD_EVENT_PLAY :
                                mSinaDLNAListener.onPlay(msg.arg2);
                                return;
                            case SINA_DLNA_CMD_EVENT_PAUSE :
                                mSinaDLNAListener.onPause(msg.arg2);
                                return;
                            case SINA_DLNA_CMD_EVENT_STOP :
                                mSinaDLNAListener.onStop(msg.arg2);
                                return;
                            case SINA_DLNA_CMD_EVENT_SEEK :
                                mSinaDLNAListener.onSeek(msg.arg2);
                                return;
                            case SINA_DLNA_CMD_EVENT_SET_MUTE :
                                mSinaDLNAListener.onSetMute(msg.arg2);
                                return;
                            case SINA_DLNA_CMD_EVENT_GET_MUTE :
                                mSinaDLNAListener.onGetMute(msg.arg2, ((Integer) (msg.obj) == 1) ? true : false);
                                return;
                            case SINA_DLNA_CMD_EVENT_SET_VOLUME :
                                mSinaDLNAListener.onSetVolume(msg.arg2);
                                return;
                            case SINA_DLNA_CMD_EVENT_GET_VOLUME :
                                mSinaDLNAListener.onGetVolume(msg.arg2, (Integer) msg.obj);
                                return;
                            case SINA_DLNA_CMD_EVENT_DURATION :
                                mSinaDLNAListener.onGetDuration(msg.arg2, (Integer) msg.obj);
                                return;
                            case SINA_DLNA_CMD_EVENT_POSITION :
                                mSinaDLNAListener.onGetPosition(msg.arg2, (Integer) msg.obj);
                                return;
                            default :
                                Log.e(TAG, "Unknown command type,  arg1=" + msg.arg1);
                                return;
                        }
                    }
                    return;
                case SINA_DLNA_MR_STATE :
                    if (mSinaDLNAListener != null) {
                        MediaRenderState mr = (MediaRenderState) msg.obj;
                        mSinaDLNAListener.onMediaRenderStateChanged(mr.getName(), mr.getValue());
                    }
                    return;
                default :
                    Log.e(TAG, "Unknown what type " + msg.what);
                    return;
            }
        }
    }

    /**
     * command result event
     */
    private static void postCmdResultEvent(Object sinadlna_ref, int what, int result, int value) {
//        Log.d(TAG, "postCmdResultEvent() what=" + what + ",result=" + result + ",value=" + value);
        SinaDLNA sd = (SinaDLNA) ((WeakReference) sinadlna_ref).get();
        if (sd == null) {
            return;
        }

        if (sd.mEventHandler != null) {
            Message m = sd.mEventHandler.obtainMessage(SINA_DLNA_CMD_EVENT, what, result, value);
            sd.mEventHandler.sendMessage(m);
        }
    }

    public class MediaRenderBean {

        private String uuid;
        private String name;

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public String getUuid() {
            return this.uuid;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }
    }

    private MediaRenderBean createMediaRenderBean() {
        return new MediaRenderBean();
    }

    public class MediaRenderState {

        private String value;
        private String name;

        public void setValue(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }
    }

    private MediaRenderState createMediaRenderState() {
        return new MediaRenderState();
    }

    /**
     * media render event
     */
    private static void postMediaRenderEvent(Object sinadlna_ref, int what, String uuid, String name) {
//        Log.d(TAG, "postMediaRenderEvent() what=" + what + ",uuid=" + uuid + ",name=" + name);
        SinaDLNA sd = (SinaDLNA) ((WeakReference) sinadlna_ref).get();
        if (sd == null) {
            return;
        }

        if (sd.mEventHandler != null) {
            MediaRenderBean mr = sd.createMediaRenderBean();
            mr.setUuid(uuid);
            mr.setName(name);
            Message m = sd.mEventHandler.obtainMessage(SINA_DLNA_MR_EVENT, what, 0, mr);
            sd.mEventHandler.sendMessage(m);
        }
    }

    /**
     * media render state changed
     */
    private static void postMediaRenderStateChanged(Object sinadlna_ref, String name, String value) {
//        Log.d(TAG, "postMediaRenderStateChanged() name=" + name + ",value=" + value);
        SinaDLNA sd = (SinaDLNA) ((WeakReference) sinadlna_ref).get();
        if (sd == null) {
            return;
        }

        if (sd.mEventHandler != null) {
            MediaRenderState mr = sd.createMediaRenderState();
            mr.setName(name);
            mr.setValue(value);
            Message m = sd.mEventHandler.obtainMessage(SINA_DLNA_MR_STATE, 0, 0, mr);
            sd.mEventHandler.sendMessage(m);
        }
    }

    public static final String TRANSPORT_STATE = "TRANSPORTSTATE";
    public static final String TRANSPORT_STATE_UNKNOWN = "_UNKNOWN_";
    public static final String TRANSPORT_STATE_STOPPED = "STOPPED";
    public static final String TRANSPORT_STATE_PLAYING = "PLAYING";
    public static final String TRANSPORT_STATE_TRANSITIONIN = "TRANSITIONIN";
    public static final String TRANSPORT_STATE_PAUSED_PLAYBACK = "PAUSED_PLAYBACK";
    public static final String TRANSPORT_STATE_NO_MEDIA_PRESENT = "NO_MEDIA_PRESENT";
    public static final String TRANSPORT_STATE_CUSTOM = "CUSTOM";

    public static final String CURRENTMEDIADURATION = "CURRENTMEDIADURATION";
    public static final String CURRENTTRACKDURATION = "CURRENTTRACKDURATION";

    private static native final void _init();

    private native final void _setup(Object sinadlna_this) throws RuntimeException, IllegalStateException;

    private native final void _release();

    private native final void _finalize();

    private EventHandler mEventHandler;
    private Context mContext;
    private int mNativeContext;
    private SinaDLNAListener mSinaDLNAListener;

}
