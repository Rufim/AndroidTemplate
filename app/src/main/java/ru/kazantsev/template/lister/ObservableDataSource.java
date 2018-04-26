package ru.kazantsev.template.lister;

import net.vrallev.android.cat.Cat;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

public abstract class ObservableDataSource<I>  implements DataSource<I> {

    public abstract Observable<I> getObservableItems(int skip, int size) throws Exception;

    @Override
    public List<I> getItems(int skip, int size) throws Exception {
        try {
            return getObservableItems(skip, size).toList().blockingGet();
        } catch (Throwable empty) {
            Cat.e(empty);
            return null;
        }
    };
}