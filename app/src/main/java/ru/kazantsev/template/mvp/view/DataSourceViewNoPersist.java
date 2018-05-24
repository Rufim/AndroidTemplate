package ru.kazantsev.template.mvp.view;

import android.os.AsyncTask;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;

import java.util.List;

import ru.kazantsev.template.lister.SafeAddItems;

@StateStrategyType(SkipStrategy.class)
public interface DataSourceViewNoPersist<I> extends DataSourceView<I>, MvpView, SafeAddItems<I> {

    void startLoading(boolean showProgress);

    void stopLoading();

    void addItems(List<I> items, int awaitedCount);

    void finishLoad(List<I> items, AsyncTask onElementsLoadedTask, Object[] loadedTaskParams);

    void refresh(boolean showProgress);

    void firstLoad(boolean scroll);

    void onDataTaskException(Throwable ex);

}