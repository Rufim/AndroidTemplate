package ru.kazantsev.template.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import ru.kazantsev.template.R;

/**
 * Created by 0shad on 13.07.2015.
 */
public  abstract class LoadFragment<Params, Progress, Result> extends AsyncTaskFragment<Params, Progress, Result> {

    interface OnDoBackground<Result,Params> {
        Result doBackground(Params[] params);
    }

    interface OnPostExecute<Result> {
        void onPostExecute(Result result);
    }

    interface OnCancelled<Result> {
        void onCancelled(Result result);
    }

    interface OnPreExecute {
        void onPreExecute();
    }

    public LoadFragment() {
    }

    private  OnDoBackground<Result,Params> onDoBackground;
    private  OnPostExecute<Result> onPostExecute;
    private  OnCancelled<Result> onCancelled;
    private  OnPreExecute onPreExecute;

    public LoadFragment<Params, Progress, Result> setOnDoBackground(OnDoBackground<Result, Params> onDoBackground) {
        this.onDoBackground = onDoBackground;
        return this;
    }

    public LoadFragment<Params, Progress, Result> setOnPostExecute(OnPostExecute<Result> onPostExecute) {
        this.onPostExecute = onPostExecute;
        return this;
    }

    public LoadFragment<Params, Progress, Result> setOnCancelled(OnCancelled<Result> onCancelled) {
        this.onCancelled = onCancelled;
        return this;
    }

    public LoadFragment<Params, Progress, Result> setOnPreExecute(OnPreExecute onPreExecute) {
        this.onPreExecute = onPreExecute;
        return this;
    }

    @Override
    public LoadFragment<Params, Progress, Result> setParams(Params... params) {
        super.setParams(params);
        return this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(ru.kazantsev.template.R.layout.progressbar, container, false);
        return rootView;
    }

    @Override
    protected Result doInBackground(Params[] params) {
        if(onDoBackground != null) {
            return onDoBackground.doBackground(params);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Result result) {
        if(onPostExecute != null) {
            onPostExecute.onPostExecute(result);
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Progress[] values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onCancelled(Result result) {
        super.onCancelled(result);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }
}
