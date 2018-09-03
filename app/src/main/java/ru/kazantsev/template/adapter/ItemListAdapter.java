package ru.kazantsev.template.adapter;

import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import ru.kazantsev.template.domain.Findable;
import ru.kazantsev.template.domain.event.Event;
import ru.kazantsev.template.util.GuiUtils;
import ru.kazantsev.template.util.TextUtils;

/**
 * Created by Dmitry on 23.06.2015.
 */
public abstract class ItemListAdapter<I> extends RecyclerView.Adapter<ItemListAdapter.ViewHolder> implements View.OnClickListener,
        View.OnLongClickListener {

    protected List<I> items = new ArrayList<>();
    protected List<I> originalItems = null;
    protected Set<ViewHolder> currentHolders = Collections.newSetFromMap(new WeakHashMap<>());
    protected final int layoutId;
    protected FilterEvent lastQuery;
    protected boolean bindRoot = false;
    protected boolean bindViews = true;
    protected boolean bindOnlyRootViews = true;
    protected boolean bindClicks = true;
    protected boolean performSelectRoot = false;
    protected final Object lock = new Object();

    // Adapter's Constructor
    public ItemListAdapter(@LayoutRes int layoutId) {
        this.layoutId = layoutId;
    }

    // Adapter's Constructor
    public ItemListAdapter(List<I> items, @LayoutRes int layoutId) {
        this.layoutId = layoutId;
        replaceItems(items);
    }

    // Create new views. This is invoked by the layout manager.
    @Override
    public ItemListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Create a new view by inflating the row item xml.
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(layoutId, parent, false);
        ViewHolder holder = newHolder(itemView);
        if (bindViews) {
            holder.bindViews(ItemListAdapter.this, bindClicks, bindOnlyRootViews, bindRoot);
        }
        return holder;
    }

    protected ViewHolder newHolder(View item) {
        ViewHolder holder = new ViewHolder(item) {
            @Override
            public List<View> getViews(View itemView) {
                return GuiUtils.getAllChildren(itemView);
            }
        };
        currentHolders.add(holder);
        return holder;
    }

    public Set<ViewHolder> getCurrentHolders() {
        return currentHolders;
    }

    public ViewHolder getHolder(int itemIndex) {
        for (ItemListAdapter.ViewHolder holder : getCurrentHolders()) {
            if (holder.getAdapterPosition() == itemIndex) {
                return holder;
            }
        }
        return null;
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return this.items != null ? this.items.size() : 0;
    }

    public int getAbsoluteItemCount() {
        if (originalItems == null) {
            return this.items != null ? this.items.size() : 0;
        } else {
            return this.originalItems != null ? this.originalItems.size() : 0;
        }
    }

    public List<I> getItems() {
        return this.items;
    }

    public void replaceItems(List<I> items) {
        synchronized (lock) {
            this.items = items;
        }
    }

    public void setItems(List<I> items) {
        setItems(items, false);
    }

    public synchronized void setItems(List<I> items, boolean notify) {
        synchronized (lock) {
            if (originalItems == null) {
                this.items = new ArrayList<I>(items);
            } else {
                addItems(items, notify);
            }
        }
    }


    public void clear() {
        synchronized (lock) {
            originalItems = null;
            lastQuery = null;
            items.clear();
        }
    }


    public List<I> getOriginalItems() {
        return this.originalItems == null ? this.items : this.originalItems;
    }

    public void enterFilteringMode() {
        synchronized (lock) {
            if (originalItems == null) {
                this.originalItems = new ArrayList<>(items);
            }
        }
    }

    public void exitFilteringMode() {
        synchronized (lock) {
            if (originalItems != null) {
                items = originalItems;
                this.originalItems = null;
                notifyDataSetChanged();
            }
        }
    }

    public List<I> addItems(List<I> items) {
        return addItems(items, true);
    }

    public synchronized List<I> addItems(List<I> items, boolean notify) {
        synchronized (lock) {
            if (originalItems == null) {
                addItemsInternal(items, notify);
                return items;
            } else {
                this.originalItems.addAll(items);
                List<I> added = find(lastQuery, items);
                addItemsInternal(added, notify);
                return added;
            }
        }
    }

    private void addItemsInternal(List<I> items, boolean notify) {
        synchronized (lock) {
            if (notify) {
                for (I item : items) {
                    addItem(item);
                }
            } else {
                this.items.addAll(items);
            }
        }
    }

    public FilterEvent getLastQuery() {
        return lastQuery;
    }

    public void setLastQuery(FilterEvent lastQuery) {
        this.lastQuery = lastQuery;
    }

    public void addItem(I item) {
        synchronized (lock) {
            this.items.add(item);
            notifyItemInserted(this.items.size());
        }
    }


    public void addItem(int position, I item) {
        synchronized (lock) {
            this.items.add(position, item);
            notifyItemInserted(position);
        }
    }

    public I removeItem(int position) {
        synchronized (lock) {
            final I item = this.items.remove(position);
            notifyItemRemoved(position);
            return item;
        }
    }

    public void moveItem(int fromPosition, int toPosition) {
        synchronized (lock) {
            final I item = this.items.remove(fromPosition);
            this.items.add(toPosition, item);
            notifyItemMoved(fromPosition, toPosition);
        }
    }

    public void selectText(ViewHolder holder, boolean erase, String query, int color) {
        if ("".equals(query)) {
            query = null;
        }
        for (TextView textView : holder.getViews(TextView.class)) {
            GuiUtils.selectText(textView, erase, query, color);
        }
    }

    public void selectText(String query, boolean erase, int color) {
        for (ViewHolder holder : currentHolders) {
            selectText(holder, erase, query, color);
        }
    }

    public List<I> filter(FilterEvent query) {
        return filter(query, true);
    }

    public List<I> filter(FilterEvent query, boolean notify) {
        synchronized (lock) {
            if (query == null) {
                return items;
            }
            List<I> founded = find(query, originalItems != null);
            if (notify) {
                changeTo(founded);
            } else {
                items = founded;
            }
            lastQuery = query;
            return founded;
        }
    }

    public List<I> find(FilterEvent query, boolean original) {
        return find(query, original ? getOriginalItems() : getItems());
    }

    public List<I> find(FilterEvent query, List<I> items) {
        if (query == null) return items;
        final List<I> filteredList = new ArrayList<>();
        for (I item : items) {
            if (find(query, item)) {
                filteredList.add(item);
            }
        }
        return filteredList;
    }

    public boolean find(FilterEvent query, I item) {
        if (item instanceof Findable) {
            if (((Findable) item).find(query)) {
                return true;
            }
        } else {
            final String text = item.toString().toLowerCase();
            if (text.contains(query.toString().toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public void notifyChanged() {
        if (originalItems == null) {
            notifyDataSetChanged();
        } else {
            filter(lastQuery);
        }
    }

    public void changeTo(List<I> items) {
        synchronized (lock) {
            applyAndAnimateRemovals(items);
            applyAndAnimateAdditions(items);
            applyAndAnimateMovedItems(items);
        }
    }

    private void applyAndAnimateRemovals(List<I> newItems) {
        synchronized (lock) {
            for (int i = this.items.size() - 1; i >= 0; i--) {
                final I item = this.items.get(i);
                if (!newItems.contains(item)) {
                    removeItem(i);
                }
            }
        }
    }

    private void applyAndAnimateAdditions(List<I> newItems) {
        synchronized (lock) {
            for (int i = 0; i < newItems.size(); i++) {
                final I item = newItems.get(i);
                if (!this.items.contains(item)) {
                    if (this.items.size() > i)
                        addItem(i, item);
                    else
                        addItem(item);
                }
            }
        }
    }

    private void applyAndAnimateMovedItems(List<I> newItems) {
        synchronized (lock) {
            for (int toPosition = newItems.size() - 1; toPosition >= 0; toPosition--) {
                final I item = newItems.get(toPosition);
                final int fromPosition = this.items.indexOf(item);
                if (fromPosition >= 0 && fromPosition != toPosition) {
                    if (this.items.size() > toPosition)
                        moveItem(fromPosition, toPosition);
                    else
                        moveItem(fromPosition, this.items.size() - 1);
                }
            }
        }
    }

    @Override
    public void onClick(View view) {
        boolean handled = false;
        if(view.getTag() instanceof ViewHolder) {
            ViewHolder holder = (ViewHolder) view.getTag();
            if (holder.getView(view.getId()) != null) {
                handled = onClick(view, holder.getLayoutPosition());
                if(handled && performSelectRoot && view.getParent() instanceof View && !(view instanceof AdapterView)) {
                    clickEmulate((View) view.getParent());
                }
            }
        }
    }

    public void clickEmulate(final View view) {
        if(view != null) {
            view.setPressed(true);
            view.postDelayed(new Runnable() {
                @Override
                public void run() {
                    view.setPressed(false);
                }
            },100);
        }
    }

    public boolean onClick(View view, int position){
        return false;
    }

    @Override
    public boolean onLongClick(View view) {
        boolean handled = false;
        if (view.getTag() instanceof ViewHolder) {
            ViewHolder holder = (ViewHolder) view.getTag();
            if (holder.getView(view.getId()) != null) {
                handled = onLongClick(view, holder.getLayoutPosition());
                if(handled && performSelectRoot && view.getParent() instanceof View && !(view instanceof AdapterView)) {
                    ((View)view.getParent()).setSelected(true);
                }
            }
        }
        return handled;
    }

    public boolean onLongClick(View view, int position) {
        return false;
    }


    // Create the ViewHolder class to keep references to your views
    public static abstract class ViewHolder extends RecyclerView.ViewHolder {

        private static final String TAG = ViewHolder.class.getSimpleName();

        protected HashMap<Integer, View> views;
        protected Object tag;

        /**
         * Constructor
         *
         * @param v The container view which holds the elements from the row item xml
         */
        public ViewHolder(View v) {
            super(v);
            cacheViews(v);
        }

        private void cacheViews(View itemView) {
            List<View> views = getViews(itemView);
            this.views = new HashMap<>(views.size());
            for (View view : views) {
                this.views.put(view.getId(), view);
            }
            onCreateHolder(itemView);
        }

        public abstract List<View> getViews(View itemView);

        public void onCreateHolder(View itemView) {
        }

        public Object getTag() {
            return tag;
        }

        public void setTag(Object tag) {
            this.tag = tag;
        }

        public  <C extends View.OnClickListener & View.OnLongClickListener> ViewHolder bindViews(C clickable, boolean bindClicks, boolean onlyRoot, boolean bindRoot) {
            for (Map.Entry<Integer, View> viewEntry : views.entrySet()) {
                if (viewEntry != itemView) {
                    View view = viewEntry.getValue();
                    if (bindClicks) {
                        if (!(view instanceof AdapterView)) {
                            if(!onlyRoot || (view instanceof ViewGroup)) {
                                view.setOnClickListener(clickable);
                                view.setOnLongClickListener(clickable);
                            }
                        }
                    }
                    view.setTag(ViewHolder.this);
                }
            }
            if(bindRoot) {
                itemView.setTag(ViewHolder.this);
                if(bindClicks) {
                    itemView.setOnClickListener(clickable);
                    itemView.setOnLongClickListener(clickable);
                }
            }
            return this;
        }

        public <V extends View> V getView(int id) {
            return (V) views.get(id);
        }

        public void removeView(int id) {
            GuiUtils.removeView(views.get(id));
        }

        public View replaceView(int id, View newView) {
            return views.put(id, newView);
        }

        public View getItemView() {
            return super.itemView;
        }

        public <V extends View> List<V> getViews(Class<V> viewClass) {
            List<V> textViews = new ArrayList<>();
            for (Map.Entry<Integer, View> viewEntry : views.entrySet()) {
                View view = viewEntry.getValue();
                if (viewClass.isAssignableFrom(view.getClass())) {
                    textViews.add((V) view);
                }
            }
            return textViews;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ViewHolder)) return false;

            ViewHolder that = (ViewHolder) o;

            return !(views != null ? !views.equals(that.views) : that.views != null);

        }

        @Override
        public int hashCode() {
            return views != null ? views.hashCode() : 0;
        }
    }

    public static class FilterEvent implements Event {
        public String query;

        public FilterEvent(String query) {
            this.query = query;
        }

        public boolean isEmpty() {
            return TextUtils.isEmpty(query);
        }

        @Override
        public String toString() {
            return query;
        }
    }
}