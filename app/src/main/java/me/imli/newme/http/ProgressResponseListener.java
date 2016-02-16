package me.imli.newme.http;

/**
 * 响应体进度回调接口，比如用于文件下载中
 * Created by Em on 2015/12/23.
 */
public interface ProgressResponseListener {
    public void onResponseProgress(long bytesRead, long contentLength, boolean done);
}
