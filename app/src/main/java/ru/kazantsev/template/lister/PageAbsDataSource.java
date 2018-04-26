package ru.kazantsev.template.lister;

import java.util.ArrayList;
import java.util.List;

public abstract class PageAbsDataSource<P> implements PageDataSource<P>, DataSource<P> {

    @Override
    public List<P> getItems(int skip, int size) throws Exception {
        List<P> all = new ArrayList<>(size);
        for (int i = skip; i < size; i++) {
            all.add(getPage(i));
        }
        return all;
    };
}
