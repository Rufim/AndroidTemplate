package ru.kazantsev.template.adapter;

import android.support.annotation.LayoutRes;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import ru.kazantsev.template.util.SystemUtils;

/**
 * Created by 0shad on 13.07.2015.
 */
public abstract class MultiItemListAdapter<I> extends ItemListAdapter<I> {

    private static final String TAG = MultiItemListAdapter.class.getSimpleName();
    private static final int EMPTY_HEADER = -1;

    private final int[] layoutIds;
    protected int firstIsHeader;
    protected int lastIsFooter;
    protected boolean useFlatList = true;

    public MultiItemListAdapter(@LayoutRes int... layoutIds) {
        this(true, false, layoutIds);
    }

    public MultiItemListAdapter(boolean useFlatList, @LayoutRes int... layoutIds) {
        this(useFlatList, false, layoutIds);
    }

    public MultiItemListAdapter(boolean useFlatList, boolean lastIsFooter, @LayoutRes int... layoutIds) {
        super(-1);
        this.layoutIds = layoutIds;
        this.firstIsHeader = layoutIds.length > 1 && layoutIds[0] > 0 ? 1 : 0;
        this.useFlatList = useFlatList;
        this.lastIsFooter = lastIsFooter ? 1 : 0;
    }

    public MultiItemListAdapter(List<I> items, @LayoutRes int... layoutIds) {
        this(items,true, false, layoutIds);
    }

    public MultiItemListAdapter(List<I> items, boolean useFlatList, @LayoutRes int... layoutIds) {
        this(items,useFlatList, false, layoutIds);
    }

    public MultiItemListAdapter(List<I> items, boolean useFlatList, boolean lastIsFooter, @LayoutRes int... layoutIds) {
        this(useFlatList, lastIsFooter, layoutIds);
        if(useFlatList) {
            setItems(items);
        } else {
            replaceItems(items);
        }
    }

    public int getFirstIsHeader() {
        return firstIsHeader;
    }

    public int getLastIsFooter() {
        return lastIsFooter;
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
        if (!SystemUtils.contains(layoutIds, viewType)) {
            Log.e(TAG, "Cannot resolve layout view type id = " + viewType);
        }
        if(viewType > 0) {
            ViewHolder holder = newHolder(inflater.inflate(viewType, parent, false));
            if (bindViews) {
                holder.bindViews(MultiItemListAdapter.this, bindClicks, bindOnlyRootViews, bindRoot);
            }
            return holder;
        } else {
            throw new RuntimeException("Illegal view id " + viewType);
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
        if(getLastIsFooter() != 0 && position == getItemCount() - 1) {
            return layoutIds[layoutIds.length - 1];
        }
        I item = getItem(position);
        if (item != null) {
            return getLayoutId(item);
        }
        return 0;
    }

    @Override
    public void replaceItems(List<I> items) {
        super.replaceItems(items);
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
            if(position == 0 && getFirstIsHeader() == 1) {
                return null;
            }
            if(position == getItemCount() - 1 && getLastIsFooter() == 1) {
                return null;
            }
            if (items.size() > (position - getFirstIsHeader()) && items.size() > 0) {
                return this.items.get(position - getFirstIsHeader());
            } else {
                return null;
            }
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
        return super.getItemCount() + getFirstIsHeader() + getLastIsFooter();
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
