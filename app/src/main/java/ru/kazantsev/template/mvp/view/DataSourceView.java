package ru.kazantsev.template.mvp.view;

import android.os.AsyncTask;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.AddToEndStrategy;
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.SingleStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;

import java.util.List;

import ru.kazantsev.template.lister.SafeAddItems;


@StateStrategyType(AddToEndSingleStrategy.class)
public interface DataSourceView<I> extends MvpView, SafeAddItems<I> {

    void startLoading(boolean showProgress);

    void stopLoading();

    @StateStrategyType(AddToEndStrategy.class)
    void addItems(List<I> items, int awaitedCount);

    void finishLoad(List<I> items, AsyncTask onElementsLoadedTask, Object[] loadedTaskParams);

    @StateStrategyType(SingleStateStrategy.class)
    void refresh(boolean showProgress);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void firstLoad(boolean scroll);

    void onDataTaskException(Throwable ex);

}
