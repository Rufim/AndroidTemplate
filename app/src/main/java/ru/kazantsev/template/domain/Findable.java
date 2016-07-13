package ru.kazantsev.template.domain;

import ru.kazantsev.template.adapter.ItemListAdapter;

/**
 * Created by Dmitry on 29.07.2015.
 */
public interface Findable {
    boolean find(ItemListAdapter.FilterEvent query);
}
