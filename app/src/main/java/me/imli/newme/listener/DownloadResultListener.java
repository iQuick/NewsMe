package me.imli.newme.listener;

/**
 * Created by Em on 2015/12/23.
 */
public interface DownloadResultListener {

    /**
     * onStart
     */
    public void onStart();

    /**
     * onProgress
     * @param progress
     */
    public void onProgress(int progress);

    /**
     * onEnd
     */
    public void onEnd();

}
