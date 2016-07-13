package ru.kazantsev.template.lister;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import java.io.IOException;
import java.util.List;

/**
 * Created by 0shad on 21.06.2016.
 */
public class ListDataSource<I> implements DataSource<I> {

    final private List<I> items;

    public ListDataSource(List<I> items) {
        this.items = items;
    }

    @Override
    public List<I> getItems(int skip, int size) throws IOException {
        return Stream.of(items).skip(skip).limit(size).collect(Collectors.toList());
    }

    public List<I> getItems() {
        return items;
    }
}
