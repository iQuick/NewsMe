package me.imli.newme.http;

/**
 * 请求体进度回调接口，比如用于文件上传中
 * Created by Em on 2015/12/23.
 */
public interface ProgressRequestListener {
    void onRequestProgress(long bytesWritten, long contentLength, boolean done);
}
