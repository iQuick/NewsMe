
package me.imli.newme.listener;

import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

/**
 *
 * @param <T>
 * @param <R>
 */
public class GlideRequestListenerAdapter<T, R> implements RequestListener<T, R> {

    @Override
    public boolean onException(Exception e, T model, Target<R> target, boolean isFirstResource) {
        onComplete();
        return false;
    }

    @Override
    public boolean onResourceReady(R resource, T model, Target<R> target,
                                   boolean isFromMemoryCache, boolean isFirstResource) {
        onComplete();
        onSuccess(resource);
        return false;
    }

    protected void onComplete() {
    }

    protected void onSuccess(R resource) {
    }

}
