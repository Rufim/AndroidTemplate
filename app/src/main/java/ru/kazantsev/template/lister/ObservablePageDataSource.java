package ru.kazantsev.template.lister;

import net.vrallev.android.cat.Cat;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

public abstract class ObservablePageDataSource<P> extends ObservableDataSource<P> implements PageDataSource<P> {

    public abstract Observable<P> getObservablePage(int index) throws Exception;

    @Override
    public Observable<P> getObservableItems(int skip, int size) throws Exception {
        List<Observable<P>> pages = new ArrayList<>();
        for (int i = skip; i < size; i++) {
            pages.add(getObservablePage(i));
        }
        return Observable.merge(pages);
    }

    @Override
    public P getPage(int index) throws Exception {
        try {
            return getObservablePage(index).singleElement().blockingGet();
        } catch (Throwable empty) {
            Cat.e(empty);
            return null;
        }

    }
}