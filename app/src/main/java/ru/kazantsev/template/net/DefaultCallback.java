package ru.kazantsev.template.net;

import net.vrallev.android.cat.Cat;

import org.greenrobot.eventbus.EventBus;

import java.net.HttpURLConnection;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.kazantsev.template.domain.event.NetworkEvent;

/**
 * Created by 0shad on 28.02.2016.
 */
public class DefaultCallback<T> implements Callback<T> {

    private static final String TAG = DefaultCallback.class.getSimpleName();

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        if (response.code() == HttpURLConnection.HTTP_OK && response.body() != null) {
            if (onSuccess != null) onSuccess.response(response, this);
            else postResponseEvent(response, response.raw().request());
        } else if (response.errorBody() != null) {
            postErrorEvent(response, response.raw().request());
        } else if (response.code() == HttpURLConnection.HTTP_FORBIDDEN) {
            Cat.e("Forbidden!");
            postErrorEvent(response, response.raw().request());
        } else if (response.code() == HttpURLConnection.HTTP_NOT_FOUND) {
            Cat.e("Not found :(");
            postErrorEvent(response, response.raw().request());
            // TODO: think about adding other stuff here
        } else {
            postErrorEvent(response, response.raw().request());
        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable throwable) {
        Cat.e(throwable);
        if (onSuccess != null) onFailure.response(throwable, this);
        else postErrorEvent(throwable, null);
    }

    interface OnSuccess<T> {
        void response(Response<T> response, DefaultCallback<T> callback);
    }

    interface OnFailure<T> {
        void response(Throwable throwable, DefaultCallback<T> callback);
    }

    private String errorMsg = "";
    private OnSuccess<T> onSuccess;
    private OnFailure<T> onFailure;

    public DefaultCallback() {
    }

    public DefaultCallback(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public DefaultCallback(OnSuccess<T> onSuccess) {
        this(onSuccess, null);
    }

    public DefaultCallback(OnSuccess<T> onSuccess, OnFailure<T> onFailure) {
        this.onSuccess = onSuccess;
        this.onFailure = onFailure;
    }

    public void postErrorEvent(Object response, okhttp3.Request request) {
        EventBus.getDefault().post(new NetworkEvent<>(NetworkEvent.Status.FAILURE, response, request));
    }

    public void postResponseEvent(Response<T> response, okhttp3.Request request) {
        EventBus.getDefault().post(new NetworkEvent<>(NetworkEvent.Status.SUCCESS, response, request));
    }

}
