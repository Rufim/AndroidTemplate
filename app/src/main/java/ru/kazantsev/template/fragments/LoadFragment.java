package ru.kazantsev.template.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import ru.kazantsev.template.R;
import ru.kazantsev.template.util.FragmentBuilder;

import java.util.Arrays;

/**
 * Created by 0shad on 13.07.2015.
 */
public class LoadFragment<Params, Progress, Result> extends BaseFragment {

    private volatile AsyncTask task;
    private Params[] params;
    private FragmentManager manager;
    private String tag = LoadFragment.class.getSimpleName();

    public interface OnDoBackground<Result,Params> {
        Result doBackground(Params[] params);
    }

    public interface OnPostExecute<Result> {
        void onPostExecute(Result result, LoadFragment fragment);
    }

    public interface OnCancelled<Result> {
        void onCancelled(Result result);
    }

    public interface OnPreExecute {
        void onPreExecute(LoadFragment fragment);
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

    public LoadFragment<Params, Progress, Result> setParams(Params... params) {
        this.params = params;
        return this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final LoadFragment<Params, Progress, Result> current = this;
        this.task = new AsyncTask<Params, Progress, Result>() {

            @Override
            protected void onPreExecute() {
                current.onPreExecute();
            }

            @Override
            protected void onPostExecute(Result result) {
                current.onPostExecute(result);
                cancelTask();
            }

            @Override
            protected void onProgressUpdate(Progress... values) {

            }

            @Override
            protected void onCancelled(Result result) {
                current.onCancelled(result);
                cancelTask();
            }

            @Override
            protected void onCancelled() {
                current.onCancelled();
            }

            @Override
            protected Result doInBackground(Params... params) {
                return current.doInBackground(params);
            }
        };
        task.execute(params);
        View rootView = inflater.inflate(ru.kazantsev.template.R.layout.progressbar, container, false);
        return rootView;
    }

    protected Result doInBackground(Params[] params) {
        if(onDoBackground != null) {
            return onDoBackground.doBackground(params);
        }
        return null;
    }

    protected void onPostExecute(Result result) {
        if(onPostExecute != null) {
            onPostExecute.onPostExecute(result, this);
        }
    }

    protected void onPreExecute() {
        if(onPreExecute != null) {
            onPreExecute.onPreExecute(this);
        }
    }


    protected void onCancelled(Result result) {
        if(onCancelled != null) {
            onCancelled.onCancelled(result);
        }
    }

    protected void onCancelled() {
        if(onCancelled != null) {
            onCancelled.onCancelled(null);
        }
    }

    public boolean eq(LoadFragment o) {
        if (this == o) return true;
        if (!super.equals(o)) return false;
        LoadFragment<?, ?, ?> that = (LoadFragment<?, ?, ?>) o;
        if (!Arrays.equals(params, that.params)) return false;
        return !(tag != null ? !tag.equals(that.tag) : that.tag != null);
    }

    public void execute(BaseFragment baseFragment) {
        new FragmentBuilder(baseFragment.getFragmentManager()).newFragment().replaceFragment(baseFragment, this);
    }

    private void cancelTask() {
        if (manager == null) {
            manager = getFragmentManager();
        }
        if (manager != null) {
            if (!task.isCancelled()) {
                task.cancel(true);
            }
            manager.beginTransaction().remove(this).commit();
        }
    }
}
