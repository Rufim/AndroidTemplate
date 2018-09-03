package ru.kazantsev.template.fragments.mvp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import ru.kazantsev.template.R;
import ru.kazantsev.template.fragments.ListFragment;
import ru.kazantsev.template.lister.DataSource;
import ru.kazantsev.template.mvp.presenter.DataSourcePresenter;
import ru.kazantsev.template.mvp.view.DataSourceView;

public abstract class MvpListFragment<I> extends ListFragment<I> implements DataSourceView<I> {

    @Override
    protected void loadItems(int count, boolean showProgress, AsyncTask onElementsLoadedTask, Object... params) {
        if (isLoading || isEnd) {
            return;
        }
        isLoading = true;
        if (getPresenter() != null && adapter != null) {
            getPresenter().loadItems(showProgress, currentCount, count, onElementsLoadedTask, params);
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
        if(!getPresenter().isLoading()) {
            stopLoading();
            if(adapter.getItems().isEmpty()) {
               showEmptyView();
            }
        }
    }

    @Override
    public void notifyItemChanged(int position) {
        notifyItemChanged(position, null);
    }

    @Override
    public void notifyItemChanged(I item) {
        notifyItemChanged(item, null);
    }

    @Override
    public void notifyItemChanged(int position, Object payload) {
        if(payload != null) {
            adapter.notifyItemChanged(position, payload);
        } else {
            adapter.notifyItemChanged(position);
        }
    }

    @Override
    public void notifyItemChanged(I item, Object payload) {
        int index =adapter.getItems().indexOf(item);
        if(index > 0) {
            notifyItemChanged(index, payload);
        }
    }

    @Override
    public void refreshData(boolean showProgress) {
        getPresenter().refreshData(showProgress);
    }

    public void refresh(boolean showProgress) {
        super.refreshData(showProgress);
    }


    public abstract DataSourcePresenter<? extends DataSourceView<I>, I> getPresenter();

}
