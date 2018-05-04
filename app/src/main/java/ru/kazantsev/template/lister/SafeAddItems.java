package ru.kazantsev.template.lister;

import android.os.AsyncTask;

import java.util.List;

public interface SafeAddItems<I> {

    void addItems(List<I> items, int awaitedCount);

    void onDataTaskException(Throwable ex);

    void finishLoad(List<I> items, AsyncTask onElementsLoadedTask, Object[] loadedTaskParams);

}
