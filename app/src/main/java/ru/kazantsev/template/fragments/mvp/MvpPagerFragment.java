package ru.kazantsev.template.fragments.mvp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import java.util.List;

import ru.kazantsev.template.fragments.BaseFragment;
import ru.kazantsev.template.fragments.PagerFragment;
import ru.kazantsev.template.lister.DataSource;
import ru.kazantsev.template.mvp.presenter.DataSourcePresenter;
import ru.kazantsev.template.mvp.view.DataSourceView;

public abstract class MvpPagerFragment<I, F extends BaseFragment> extends PagerFragment<I, F> implements DataSourceView<I> {

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
    public void onResume() {
        super.onResume();
        pager.setCurrentItem(currentItem);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
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
