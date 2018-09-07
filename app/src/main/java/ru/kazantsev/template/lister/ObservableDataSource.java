package ru.kazantsev.template.lister;

import net.vrallev.android.cat.Cat;

import java.util.List;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;

public abstract class ObservableDataSource<I>  implements DataSource<I> {

    public abstract Maybe<List<I>> getObservableItems(int skip, int size) throws Exception;

    @Override
    public List<I> getItems(int skip, int size) throws Exception {
        try {
            return getObservableItems(skip, size).blockingGet();
        } catch (Throwable empty) {
            Cat.e(empty);
            return null;
        }
    };
}