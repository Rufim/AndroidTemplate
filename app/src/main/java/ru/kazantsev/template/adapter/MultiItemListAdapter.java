package ru.kazantsev.template.adapter;

import android.support.annotation.LayoutRes;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import ru.kazantsev.template.util.SystemUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by 0shad on 13.07.2015.
 */
public abstract class MultiItemListAdapter<I> extends ItemListAdapter<I> {

    private static final String TAG = MultiItemListAdapter.class.getSimpleName();
    private static final int EMPTY_HEADER = -1;

    private final int[] layoutIds;
    protected int firstIsHeader;
    protected boolean useFlatList = true;

    public MultiItemListAdapter(boolean firstIsHeader, @LayoutRes int... layoutIds) {
        super(-1);
        this.layoutIds = layoutIds;
        this.firstIsHeader = firstIsHeader ? 1 : 0;
    }

    public MultiItemListAdapter(boolean firstIsHeader, boolean useFlatList, @LayoutRes int... layoutIds) {
        super(-1);
        this.layoutIds = layoutIds;
        this.firstIsHeader = firstIsHeader ? 1 : 0;
        this.useFlatList = useFlatList;
    }

    public MultiItemListAdapter(List<I> items, boolean firstIsHeader, @LayoutRes int... layoutIds) {
        this(firstIsHeader, layoutIds);
        setItems(items);
    }

    public MultiItemListAdapter(List<I> items, boolean firstIsHeader, boolean useFlatList, @LayoutRes int... layoutIds) {
        this(firstIsHeader, useFlatList, layoutIds);
        setItems(items);
    }

    public int getFirstIsHeader() {
        return firstIsHeader;
    }

    @Override
    public ViewHolder getHolder(int itemIndex) {
        for (ItemListAdapter.ViewHolder holder : getCurrentHolders()) {
            if (holder.getAdapterPosition() - getFirstIsHeader() == itemIndex) {
                return holder;
            }
        }
        return null;
    }

    // Create new views. This is invoked by the layout manager.
    @Override
    public ItemListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Create a new view by inflating the row item xml.
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (SystemUtils.contains(layoutIds, viewType)) {
            ViewHolder holder = newHolder(inflater.inflate(viewType, parent, false));
            if (bindViews) {
                holder.bindViews(MultiItemListAdapter.this, bindClicks);
            }
            return holder;
        } else {
            Log.e(TAG, "Cannot resolve layout view type");
            return null;
        }
    }

    public abstract
    @LayoutRes
    int getLayoutId(I item);

    public List<I> getSubItems(I item) {
        return null;
    }

    public boolean hasSubItems(I item) {
        return false;
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return countItems();
    }


    @Override
    public int getItemViewType(int position) {
        if (getFirstIsHeader() != 0 && position == 0) {
            return layoutIds[0];
        }
        I item = getItem(position);
        if (item != null) {
            return getLayoutId(item);
        }
        return 0;
    }

    @Override
    public void setItems(List<I> items) {
        super.setItems(toFlatList(items));
    }

    @Override
    public List<I> addItems(List<I> items) {
        return super.addItems(toFlatList(items));
    }

    @Override
    public void addItem(I item) {
        synchronized (lock) {
            this.items.add(item);
            notifyItemInserted(this.items.size() + getFirstIsHeader());
        }
    }

    @Override
    public void addItem(int position, I item) {
        synchronized (lock) {
            this.items.add(position, item);
            notifyItemInserted(position + getFirstIsHeader());
        }
    }

    @Override
    public I removeItem(int position) {
        synchronized (lock) {
            final I item = this.items.remove(position);
            notifyItemRemoved(position + getFirstIsHeader());
            return item;
        }
    }

    @Override
    public void moveItem(int fromPosition, int toPosition) {
        synchronized (lock) {
            final I item = this.items.remove(fromPosition);
            this.items.add(toPosition, item);
            notifyItemMoved(fromPosition + getFirstIsHeader(), toPosition + getFirstIsHeader());
        }
    }

    private List<I> toFlatList(List<I> items) {
        synchronized (lock) {
            if (!useFlatList) {
                return items;
            }
            List<I> flatList = new ArrayList<>();
            for (int i = 0; i < countItems(items); i++) {
                flatList.add(i, getItem(items, i, new AtomicInteger(0)));
            }
            return flatList;
        }
    }

    public I getItem(int position) {
        synchronized (lock) {
            if (items.size() > position && items.size() > 0)
                return this.items.get(position - getFirstIsHeader());
            else return null;
        }
        //return getItem(items, position - getFirstIsHeader(), new AtomicInteger(0));
    }

    private I getItem(List<I> items, int position, AtomicInteger countWrapper) {
        int count = countWrapper.get();
        I result = null;
        for (I item : items) {
            if (position == count) {
                return item;
            }
            count++;
            if (hasSubItems(item)) {
                countWrapper.set(count);
                result = getItem(getSubItems(item), position, countWrapper);
                if (result != null) {
                    break;
                } else {
                    count = countWrapper.get();
                }
            }
        }
        countWrapper.set(count);
        return result;
    }


    private int countItems() {
        return super.getItemCount() + getFirstIsHeader();
    }

    private int countItems(List<I> items) {
        int count = 0;
        if (items != null) {
            for (I item : items) {
                count++;
                if (hasSubItems(item)) {
                    count += countItems(getSubItems(item));
                }
            }
        }
        return count;
    }

}
