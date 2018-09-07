package ru.kazantsev.template.lister;

import net.vrallev.android.cat.Cat;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;

public abstract class ObservablePageDataSource<P> extends ObservableDataSource<P> implements PageDataSource<P> {

    public abstract Maybe<P> getObservablePage(int index) throws Exception;

    @Override
    public Maybe<List<P>> getObservableItems(int skip, int size) throws Exception {
        List<Maybe<P>> pages = new ArrayList<>();
        for (int i = skip; i < size; i++) {
            pages.add(getObservablePage(i));
        }
        return Maybe.merge(pages).toList().toMaybe();
    }

    @Override
    public P getPage(int index) throws Exception {
        try {
            return getObservablePage(index).blockingGet();
        } catch (Throwable empty) {
            Cat.e(empty);
            return null;
        }

    }
}