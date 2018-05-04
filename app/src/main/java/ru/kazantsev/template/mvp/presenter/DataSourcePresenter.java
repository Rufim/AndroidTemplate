package ru.kazantsev.template.mvp.presenter;

import android.os.AsyncTask;

import net.vrallev.android.cat.Cat;

import io.reactivex.Observable;
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
        final Observable<I> observable;
        if (dataSource instanceof ObservableDataSource) {
            try {
                observable = ((ObservableDataSource<I>) dataSource).getObservableItems(skip, size);
            } catch (Exception e) {
                Cat.e(e);
                getViewState().onDataTaskException(e);
                getViewState().stopLoading();
                return;
            }
        } else {
            try {
                observable = Observable.fromCallable(() -> dataSource.getItems(skip, size)).flatMap(Observable::fromIterable);
            } catch (Exception e) {
                Cat.e(e);
                getViewState().onDataTaskException(e);
                getViewState().stopLoading();
                return;
            }
        }
        Disposable disposable = rxSequence(observable, size, onElementsLoadedTask, loadedTaskParams);
        dispouseOnDestroy(disposable);
    }


    protected Disposable rxSequence(Observable<I> observable, int size, AsyncTask onElementsLoadedTask, Object[] loadedTaskParams) {
        return observable.compose(RxUtils.applySchedulers())
                .toList()
                .map(items -> {
                    getViewState().addItems(items, size);
                    return items;
                })
                .subscribe(items -> {
                    isLoading = false;
                    getViewState().finishLoad(items, onElementsLoadedTask, loadedTaskParams);
                }, error -> {
                    isLoading = false;
                    getViewState().onDataTaskException(error);
                    getViewState().stopLoading();
                });
    }

}
