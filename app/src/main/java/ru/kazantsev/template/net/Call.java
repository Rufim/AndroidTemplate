package ru.kazantsev.template.net;

import java.io.IOException;

import okhttp3.Request;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by 0shad on 28.02.2016.
 */
public class Call<T> implements retrofit2.Call<T> {
    final retrofit2.Call<T> delegate;

    public Call(retrofit2.Call<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public void enqueue(final Callback<T> callback) {
        delegate.enqueue(callback);
    }

    public void enqueue() {
        enqueue(new DefaultCallback<>());
    }

    public void enqueue(String errorMessage) {
        enqueue(new DefaultCallback<>(errorMessage));
    }

    @Override
    public boolean isExecuted() {
        return delegate.isExecuted();
    }

    @Override
    public Response<T> execute() throws IOException {
        return delegate.execute();
    }

    @Override
    public void cancel() {
        delegate.cancel();
    }

    @Override
    public boolean isCanceled() {
        return delegate.isCanceled();
    }

    @SuppressWarnings("CloneDoesntCallSuperClone") // Performing deep clone.
    @Override
    public retrofit2.Call<T> clone() {
        return new Call<>(delegate.clone());
    }

    @Override
    public Request request() {
        return delegate.request();
    }

}
