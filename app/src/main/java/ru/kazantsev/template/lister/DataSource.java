package ru.kazantsev.template.lister;

import java.util.List;

/**
 * Created by Rufim on 01.07.2015.
 */
public interface DataSource<I> {
    List<I> getItems(int skip, int size) throws Exception;
}
