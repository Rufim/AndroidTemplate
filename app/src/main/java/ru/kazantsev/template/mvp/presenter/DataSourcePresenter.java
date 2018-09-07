package ru.kazantsev.template.mvp.presenter;

import android.os.AsyncTask;

import net.vrallev.android.cat.Cat;

import java.util.List;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import ru.kazantsev.template.lister.DataSource;
import ru.kazantsev.template.lister.ObservableDataSource;
import ru.kazantsev.template.mvp.view.DataSourceView;
import ru.kazantsev.template.util.RxUtils;

public class DataSourcePresenter<V extends DataSourceView<I>, I> extends BasePresenter<V> {

    private DataSource<I> dataSource;
    private boolean isLoading = false;

    public DataSourcePresenter() {}

    public DataSourcePresenter(DataSource<I> dataSource) {
        this.dataSource = dataSource;
    }

    public DataSource<I> getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource<I> dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void onFirstViewAttach() {
        super.onFirstViewAttach();
        getViewState().firstLoad(true);
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void refreshData(boolean progress) {
        getViewState().refresh(progress);
    }

    @SuppressWarnings("uncheaked")
    public void loadItems(boolean showProgress, int skip, int size, AsyncTask onElementsLoadedTask, Object[] loadedTaskParams) {
        if (isLoading) {
            return;
        }
        isLoading = true;
        getViewState().startLoading(showProgress);
        final Maybe<List<I>> single;
        if (dataSource instanceof ObservableDataSource) {
            try {
                single = ((ObservableDataSource<I>) dataSource).getObservableItems(skip, size);
            } catch (Exception e) {
                Cat.e(e);
                onException(e);
                return;
            }
        } else {
            try {
                single = Maybe.fromCallable(() -> dataSource.getItems(skip, size));
            } catch (Exception e) {
                Cat.e(e);
                onException(e);
                return;
            }
        }
        Disposable disposable = rxSequence(single, size, onElementsLoadedTask, loadedTaskParams);
        dispouseOnDestroy(disposable);
    }

    protected void onException(Throwable ex) {
        isLoading = false;
        getViewState().onDataTaskException(ex);
    }

    protected void onSuccess(List<I> items,  AsyncTask onElementsLoadedTask, Object[] loadedTaskParams) {
        isLoading = false;
        getViewState().finishLoad(items, onElementsLoadedTask, loadedTaskParams);
    }

    protected Disposable rxSequence(Maybe<List<I>> observable, int size, AsyncTask onElementsLoadedTask, Object[] loadedTaskParams) {
        return observable.compose(RxUtils.applySchedulersMaybe())
                .map(items -> {
                    getViewState().addItems(items, size);
                    return items;
                })
                .subscribe(items -> this.onSuccess(items, onElementsLoadedTask, loadedTaskParams),
                        this::onException,
                        () -> this.onSuccess(null, onElementsLoadedTask, loadedTaskParams));
    }

}
