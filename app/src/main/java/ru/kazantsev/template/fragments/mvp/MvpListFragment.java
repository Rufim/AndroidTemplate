package ru.kazantsev.template.fragments.mvp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.arellomobile.mvp.presenter.ProvidePresenter;

import java.util.List;

import ru.kazantsev.template.fragments.ListFragment;
import ru.kazantsev.template.lister.DataSource;
import ru.kazantsev.template.mvp.presenter.DataSourcePresenter;
import ru.kazantsev.template.mvp.view.DataSourceView;

public abstract class MvpListFragment<I> extends ListFragment<I> implements DataSourceView<I> {

    int needMore = 0;

    @Override
    protected void loadItems(int count, boolean showProgress, AsyncTask onElementsLoadedTask, Object... params) {
        if (isLoading || isEnd) {
            return;
        }
        isLoading = true;
        if (getPresenter() != null && adapter != null) {
            getPresenter().loadItems(showProgress, adapter.getAbsoluteItemCount(), count, onElementsLoadedTask, params);
        }
    }

    @Override
    public void setDataSource(DataSource<I> dataSource) {
        if(getPresenter() != null) getPresenter().setDataSource(dataSource);
    }

    @Override
    public DataSource<I> getDataSource() {

        if(getPresenter() == null) return null;
        return getPresenter().getDataSource();
    }

    @Override
    public void saveLister() {
        savedDataSource = getPresenter().getDataSource();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    }

    public void addItems(List<I> items, int awaitedCount) {
        if (items == null || items.size() == 0 || isEnd) {
            isEnd = true;
        } else if(adapter != null) {
            if(adapter.getItems().size() == 0) {
                adapter.setItems(items);
                needMore = awaitedCount - adapter.getItems().size();
            } else {
                needMore = awaitedCount - adapter.addItems(items, false).size();
            }
        }
        needMore = 0;
    }

    @Override
    public void refreshData(boolean showProgress) {
        getPresenter().refreshData(showProgress);
    }

    public void refresh(boolean showProgress) {
        super.refreshData(showProgress);
    }

    public void finishLoad(AsyncTask onElementsLoadedTask, Object[] loadedTaskParams) {
        isLoading = false;
        if (needMore > 0 && !isEnd) {
            loadItems(needMore, true);
        } else {
            currentCount = adapter.getAbsoluteItemCount();
            if (onElementsLoadedTask != null) {
                onElementsLoadedTask.execute(loadedTaskParams);
            }
            if (itemList != null && adapter != null) {
                adapter.notifyChanged();
                stopLoading();
                if (isAdded()) {
                    onPostLoadItems();
                }
            }
        }
    }

    public abstract DataSourcePresenter<? extends DataSourceView<I>, I> getPresenter();

}
