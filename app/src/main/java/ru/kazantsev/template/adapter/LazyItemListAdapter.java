package ru.kazantsev.template.adapter;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import java.util.List;

/**
 * Created by Dmitry on 21.04.2016.
 */
public abstract class LazyItemListAdapter<I> extends ItemListAdapter<I>{

    public LazyItemListAdapter(@LayoutRes int layoutId) {
        super(layoutId);
    }

    public LazyItemListAdapter(List<I> items, @LayoutRes int layoutId) {
        super(items, layoutId);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if(items != null && items.size() > translatePosition(position)) {
            onBindHolder(holder, items.get(translatePosition(position)));
        } else {
            onBindHolder(holder, null);
        }
    }

    public abstract void onBindHolder(@NonNull ViewHolder holder, @Nullable I item);

    @Override
    public boolean onClick(@NonNull View view, int position) {
       return onClick(view, items.get(translatePosition(position)));
    }

    public boolean onClick(@NonNull View view, @Nullable I item){
       return false;
    }

    @Override
    public boolean onLongClick(@NonNull View view, int position) {
        return onLongClick(view, items.get(translatePosition(position)));
    }

    public boolean onLongClick(@NonNull View view, @Nullable I item) {
       return false;
    }

    public int translatePosition(int position) {
        return position;
    }

}
