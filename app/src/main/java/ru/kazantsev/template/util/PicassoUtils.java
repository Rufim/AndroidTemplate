package ru.kazantsev.template.util;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

/**
 * Created by Admin on 28.02.2018.
 */

public class PicassoUtils {


    public static Picasso initCache(Application application, long maxSize) {
        Picasso.Builder picassoBuilder = new Picasso.Builder(application);

        picassoBuilder.listener(new Picasso.Listener() {
            @Override
            public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                Log.e("PICASSO", uri.toString(), exception);
            }
        });
        picassoBuilder.downloader(new OkHttp3Downloader(application, 1024 * 1024 * maxSize));
        // Picasso.Builder creates the Picasso object to do the actual requests
        Picasso picasso = picassoBuilder.build();
        try {
            Picasso.setSingletonInstance(picasso);
        } catch (IllegalStateException ignored) {
            // Picasso instance was already set
            // cannot set it after Picasso.with(Context) was already in use
        }
        return picasso;
    }

    public static void loadWithCache(Context context, String url, ImageView imageView) {
            loadWithCache(context, url, imageView, null);
    }

    public static void loadWithCache(Context context, String url, ImageView imageView, Callback callback) {
        Picasso.get()
                .load(url)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        if(callback != null) callback.onSuccess();
                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get()
                                .load(url)
                                .into(imageView, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                        if(callback != null) callback.onSuccess();
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        if(callback != null) callback.onError(e);
                                    }
                                });
                    }
                });
    }

}
