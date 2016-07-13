package ru.kazantsev.template.net;

import android.util.Log;
import com.google.gson.Gson;
import org.greenrobot.eventbus.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.kazantsev.template.domain.entity.json.JsonError;
import ru.kazantsev.template.domain.event.NetworkEvent;

import java.io.IOException;
import java.net.HttpURLConnection;

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
            try {
                JsonError error = new Gson().fromJson(response.errorBody().string(), JsonError.class);
                if (error.getRequesterInformation() != null) {
                    error.setAction(error.getRequesterInformation().getReceivedParams().get("action"));
                }
                postErrorEvent(error, response.raw().request());
            } catch (IOException e) {
                Log.e(TAG, "Cant read error", e);
            }
        } else if (response.code() == HttpURLConnection.HTTP_FORBIDDEN) {
            System.out.println("Forbidden!");
            postErrorEvent(response, response.raw().request());
        } else if (response.code() == HttpURLConnection.HTTP_NOT_FOUND) {
            System.out.println("Not found :(");
            postErrorEvent(response, response.raw().request());
            // TODO: think about adding other stuff here
        } else {
            postErrorEvent(response, response.raw().request());
        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable throwable) {
        System.out.println(errorMsg + ": " + throwable.getLocalizedMessage());
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
