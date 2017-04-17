package ru.kazantsev.template.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import ru.kazantsev.template.R;
import ru.kazantsev.template.util.FragmentBuilder;
import ru.kazantsev.template.util.GuiUtils;

import java.util.Arrays;

/**
 * Created by 0shad on 13.07.2015.
 */
public class LoadFragment<Params, Progress, Result> extends BaseFragment {

    private volatile AsyncTask task;
    private Params[] params;
    private FragmentManager manager;
    private String tag = LoadFragment.class.getSimpleName();

    protected ProgressBar progressBar;
    protected TextView loadingText;

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

    private LoadFragment.OnDoBackground<Result,Params> onDoBackground;
    private LoadFragment.OnPostExecute<Result> onPostExecute;
    private LoadFragment.OnCancelled<Result> onCancelled;
    private LoadFragment.OnPreExecute onPreExecute;

    public LoadFragment<Params, Progress, Result> setOnDoBackground(LoadFragment.OnDoBackground<Result, Params> onDoBackground) {
        this.onDoBackground = onDoBackground;
        return this;
    }

    public LoadFragment<Params, Progress, Result> setOnPostExecute(LoadFragment.OnPostExecute<Result> onPostExecute) {
        this.onPostExecute = onPostExecute;
        return this;
    }

    public LoadFragment<Params, Progress, Result> setOnCancelled(LoadFragment.OnCancelled<Result> onCancelled) {
        this.onCancelled = onCancelled;
        return this;
    }

    public LoadFragment<Params, Progress, Result> setOnPreExecute(LoadFragment.OnPreExecute onPreExecute) {
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
        if (task != null && !task.isCancelled()) {
            task.cancel(true);
        }
        this.task = new AsyncTask<Params, Progress, Result>() {

            @Override
            protected void onPreExecute() {
                current.onPreExecute();
            }

            @Override
            protected void onPostExecute(Result result) {
                //cancelTask();
                current.onPostExecute(result);
            }

            @Override
            protected void onProgressUpdate(Progress... values) {

            }

            @Override
            protected void onCancelled(Result result) {
                // cancelTask();
                current.onCancelled(result);
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
        View rootView = inflater.inflate(ru.kazantsev.template.R.layout.progressbar, container, false);
        loadingText = GuiUtils.getView(rootView, R.id.load_progress);
        loadingText = GuiUtils.getView(rootView, R.id.loading_text);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (task != null && !task.isCancelled() && task.getStatus().equals(AsyncTask.Status.PENDING)) {
            task.execute(params);
        }
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
        } else {
            cancelTask();
        }
    }

    protected void onCancelled() {
        if(onCancelled != null) {
            onCancelled.onCancelled(null);
        } else {
            cancelTask();
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
        new FragmentBuilder(baseFragment.getFragmentManager()).replaceFragment(baseFragment, this);
    }

    public void error(BaseFragment fragment, @StringRes int id) {
        (new FragmentBuilder(getFragmentManager()))
                .putArg("message", getString(id))
                .putArg("fragment_class", fragment.getClass())
                .putArg("fragment_args", fragment.getArguments())
                .replaceFragment(fragment, ErrorFragment.class);
    }

    private void cancelTask() {
        if (manager == null) {
            manager = getFragmentManager();
        }
        if (manager != null) {
            if (!task.isCancelled()) {
                task.cancel(true);
            }
            manager.beginTransaction().remove(this).commitAllowingStateLoss();
        }
    }
}
