package ru.kazantsev.template.mvp.view;

import android.os.AsyncTask;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.AddToEndStrategy;
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;

import java.util.List;

import ru.kazantsev.template.lister.SafeAddItems;
import ru.kazantsev.template.mvp.strategy.SingleStateOneExecutionStrategy;


@StateStrategyType(AddToEndSingleStrategy.class)
public interface DataSourceView<I> extends MvpView, SafeAddItems<I> {

    void startLoading(boolean showProgress);

    void stopLoading();

    void notifyItemChanged(int position);

    void notifyItemChanged(I item);

    void notifyItemChanged(int position, Object payload);

    void notifyItemChanged(I item, Object payload);

    @StateStrategyType(AddToEndStrategy.class)
    void addItems(List<I> items, int awaitedCount);

    void finishLoad(List<I> items, AsyncTask onElementsLoadedTask, Object[] loadedTaskParams);

    @StateStrategyType(SingleStateOneExecutionStrategy.class)
    void refresh(boolean showProgress);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void firstLoad(boolean scroll);

    @StateStrategyType(SingleStateOneExecutionStrategy.class)
    void addFinalItems(List<I> items);

    void onDataTaskException(Throwable ex);

}
