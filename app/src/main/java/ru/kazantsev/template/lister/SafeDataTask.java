package ru.kazantsev.template.lister;

import android.os.AsyncTask;

import java.lang.ref.WeakReference;
import java.util.List;


public class SafeDataTask<I> extends AsyncTask<Void, Void, List<I>> {


    private int count = 0;
    private int skip = 0;
    private AsyncTask onElementsLoadedTask;
    private Object[] loadedTaskParams;
    private WeakReference<SafeAddItems<I>> weekInterface;
    private DataSource<I> dataSource;
    private Throwable exception = null;

    public SafeDataTask(DataSource<I> dataSource, SafeAddItems<I> safeAddItems, int skip ,int count) {
        this.dataSource = dataSource;
        this.count = count;
        this.skip = skip;
        weekInterface = new WeakReference<>(safeAddItems);
    }

    public SafeDataTask(DataSource<I> dataSource, SafeAddItems<I> safeAddItems, int skip ,int count, AsyncTask onElementsLoadedTask, Object[] loadedTaskParams) {
        this(dataSource, safeAddItems, skip, count);
        this.onElementsLoadedTask = onElementsLoadedTask;
        this.loadedTaskParams = loadedTaskParams;
    }

    @Override
    protected List<I> doInBackground(Void... params) {
        List<I> items = null;
        try {
            items = dataSource.getItems(skip, count);
            SafeAddItems<I> safeAddItems = weekInterface.get();
            if(safeAddItems != null) {
                safeAddItems.addItems(items, count);
            }
        } catch (Throwable ex) {
            exception = ex;
        }
        return items;
    }

    @Override
    protected void onPostExecute(List<I> is) {
        SafeAddItems<I> safeAddItems = weekInterface.get();
        if(safeAddItems != null) {
            if(exception == null) {
                safeAddItems.finishLoad(is, onElementsLoadedTask, loadedTaskParams);
            } else {
                safeAddItems.onDataTaskException(exception);
            }
        }
    }
}
