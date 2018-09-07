package ru.kazantsev.template.fragments;

import android.app.SearchManager;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.vrallev.android.cat.Cat;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ru.kazantsev.template.R;
import ru.kazantsev.template.adapter.ItemListAdapter;
import ru.kazantsev.template.domain.Constants;
import ru.kazantsev.template.lister.DataSource;
import ru.kazantsev.template.lister.SafeAddItems;
import ru.kazantsev.template.lister.SafeDataTask;
import ru.kazantsev.template.util.GuiUtils;
import ru.kazantsev.template.view.AdvancedRecyclerView;
import ru.kazantsev.template.view.scroller.FastScroller;

/**
 * Created by Rufim on 17.01.2015.
 */
public abstract class ListFragment<I> extends BaseFragment implements SearchView.OnQueryTextListener, SafeAddItems<I> {

    private static final String TAG = ListFragment.class.getSimpleName();

    protected ProgressBar progressBar;
    protected TextView progressBarText;
    protected ProgressBar loadMoreBar;
    protected AdvancedRecyclerView itemList;
    protected SwipeRefreshLayout swipeRefresh;
    protected TextView emptyView;

    protected SearchView searchView;
    protected ItemListAdapter<I> adapter;
    protected LinearLayoutManager layoutManager;
    protected FastScroller scroller;
    protected DataSource<I> savedDataSource;
    protected DataSource<I> dataSource;

    protected int listLayout = R.layout.fragment_loading_list;

    //
    protected int pageSize = 50;
    protected volatile boolean isLoading = false;
    protected volatile boolean isEnd = false;
    protected int currentCount = 0;
    protected int pastVisibleItems = 0;
    protected int needMore = 0;
    protected SafeDataTask<I> dataTask;
    protected FilterTask filterTask;
    protected MoveTask moveToIndex;
    protected boolean enableFiltering = false;
    protected boolean enableSearch = false;
    protected boolean enableScrollbar = false;
    protected boolean autoLoadMoreOnFinish = true;
    protected boolean autoLoadMoreOnScroll = true;
    protected boolean isEndOnEmptyResult = true;
    protected final static BlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();
    protected final static ThreadPoolExecutor executor = new ThreadPoolExecutor(Constants.App.CORES, Constants.App.CORES, 30L, TimeUnit.SECONDS, tasks);


    public boolean isEnableFiltering() {
        return enableFiltering;
    }

    public boolean isEnableSearch() {
        return enableSearch;
    }

    public boolean isEnableScrollbar() {
        return enableScrollbar;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public boolean isEnd() {
        return isEnd;
    }

    public int getCurrentCount() {
        return currentCount;
    }

    public void setCurrentCount(int currentCount) {
        this.currentCount = currentCount;
    }

    public ItemListAdapter<I> getAdapter() {
        return adapter;
    }

    public ListFragment() {
    }

    public ListFragment(DataSource<I> dataSource) {
        setDataSource(dataSource);
    }

    public void setDataSource(DataSource<I> dataSource) {
        this.dataSource = dataSource;
    }

    public void saveLister() {
        savedDataSource = dataSource;
    }

    public boolean restoreLister() {
        if (savedDataSource != null) {
            setDataSource(savedDataSource);
            savedDataSource = null;
            refreshData(true);
            return true;
        } else {
            return false;
        }
    }

    protected ItemListAdapter.FilterEvent newFilterEvent(String query) {
        return new ItemListAdapter.FilterEvent(query);
    }

    @Override
    public boolean onQueryTextChange(String query) {
        if (query.isEmpty() && !searchView.isIconified()) {
//            searchView.clearFocus();
            onSearchViewClose(searchView);
        }
        return enableFiltering;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        if (enableFiltering) {
            adapter.enterFilteringMode();
            filter(newFilterEvent(query));
            return true;
        } else {
            return false;
        }
    }

    public void filter(ItemListAdapter.FilterEvent filterEvent) {
        adapter.enterFilteringMode();
        if (filterTask == null) {
            adapter.setLastQuery(null);
            filterTask = newFilterTask(filterEvent);
            getActivity().runOnUiThread(filterTask);
        }
    }

    public FilterTask newFilterTask(ItemListAdapter.FilterEvent filterEvent) {
        return new FilterTask(filterEvent);
    }

    protected void onSearchViewClose(SearchView searchView) {
        if (searchView != null) {
            if (enableFiltering) {
                adapter.setLastQuery(null);
                adapter.exitFilteringMode();
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (enableSearch) {
            inflater.inflate(R.menu.search, menu);
            MenuItem searchItem = menu.findItem(R.id.search);
            if (searchItem != null) {
                searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
                SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
                searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
                searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
                if (enableFiltering) {
                    searchView.setQueryHint(getString(R.string.filter_hint));
                    searchView.setSuggestionsAdapter(null);
                }
                searchView.setOnQueryTextListener(this);
                searchView.setOnCloseListener(() -> {
                    onSearchViewClose(searchView);
                    return false;
                });
                LinearLayout linearLayout1 = (LinearLayout) searchView.getChildAt(0);
                LinearLayout linearLayout2 = (LinearLayout) linearLayout1.getChildAt(2);
                LinearLayout linearLayout3 = (LinearLayout) linearLayout2.getChildAt(1);
                AutoCompleteTextView autoComplete = (AutoCompleteTextView) linearLayout3.getChildAt(0);
                //Set the input text color
                autoComplete.setTextColor(GuiUtils.getThemeColor(getContext(), android.R.attr.textColor));
                // set the hint text color
                autoComplete.setHintTextColor(GuiUtils.getThemeColor(getContext(), android.R.attr.textColorHint));
                //Some drawable (e.g. from xml)
                autoComplete.setDropDownBackgroundDrawable(new ColorDrawable(GuiUtils.getThemeColor(getContext(), R.attr.colorOverlay)));
            }
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public void startLoading(boolean showProgress) {
        isLoading = true;
        if (loadMoreBar != null && showProgress && progressBar.getVisibility() != View.VISIBLE) {
            loadMoreBar.setVisibility(View.VISIBLE);
        }
    }

    public void stopLoading() {
        isLoading = false;
        if (loadMoreBar != null) {
            loadMoreBar.setVisibility(View.GONE);
        }
        itemList.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        progressBarText.setVisibility(View.GONE);
        swipeRefresh.setRefreshing(false);
    }

    protected void loadItems(int count, boolean showProgress, AsyncTask onElementsLoadedTask, Object... params) {
        if (isLoading || isEnd) {
            return;
        }
        if (getDataSource() != null) {
            if (dataTask != null && !dataTask.getStatus().equals(AsyncTask.Status.FINISHED)) {
                Cat.e("Warning!!! You already have some tasks in thread pool!");
            }
            startLoading(showProgress);
            dataTask = new SafeDataTask<I>(getDataSource(), this, currentCount, count, onElementsLoadedTask, params);
            dataTask.executeOnExecutor(executor);
        }
    }

    public void loadItems(boolean showProgress) {
        loadItems(pageSize, showProgress, null, null);
    }

    protected void loadItems(boolean showProgress, AsyncTask onElementsLoadedTask, Object... params) {
        loadItems(pageSize, showProgress, onElementsLoadedTask, params);
    }


    public void loadItems(int count, boolean showProgress) {
        loadItems(count, showProgress, null, null);
    }

    @Override
    public void addItems(List<I> items, int awaitedCount) {
        if ((items == null || items.size() == 0) && isEndOnEmptyResult) {
            isEnd = true;
        } else if (adapter != null && items != null) {
            if (adapter.getItems().size() == 0) {
                adapter.setItems(items);
                needMore = awaitedCount - adapter.getItems().size();
            } else {
                needMore = awaitedCount - adapter.addItems(items, false).size();
            }
        }
    }

    @Override
    public void finishLoad(List<I> items, AsyncTask onElementsLoadedTask, Object[] loadedTaskParams) {
        isLoading = false;
        currentCount = adapter.getAbsoluteItemCount();
        if (needMore > 0 && !isEnd && autoLoadMoreOnFinish && !(items == null || items.size() == 0)) {
            loadItems(needMore, true);
        } else {
            if (onElementsLoadedTask != null) {
                onElementsLoadedTask.execute(loadedTaskParams);
            }
            if (itemList != null && adapter != null) {
                adapter.notifyChanged();
                if (adapter.getItems().isEmpty()) {
                    showEmptyView();
                } else {
                    hideEmptyView();
                }
                stopLoading();
                if (isAdded()) {
                    onPostLoadItems();
                }
            }
        }
    }

    public void addFinalItems(List<I> items) {
        addItems(items, items.size());
        finishLoad(items, null, null);
    }

    public void hideEmptyView() {
        emptyView.setVisibility(View.GONE);
    }


    public void showEmptyView() {
        emptyView.setVisibility(View.VISIBLE);
    }

    public void setEmptyViewText(String message) {
        if (message != null) {
            GuiUtils.setText(emptyView, message);
        }
    }

    public void setEmptyViewText(@StringRes int message) {
        if (message != 0) {
            GuiUtils.setText(emptyView, getText(message));
        }
    }

    public void showEmptyView(@StringRes int message) {
        setEmptyViewText(message);
        showEmptyView();
    }

    public void showEmptyView(String message) {
        setEmptyViewText(message);
        showEmptyView();
    }

    public void onPostLoadItems() {

    }

    protected abstract ItemListAdapter newAdapter();

    protected DataSource<I> newDataSource() throws Exception {
        return getDataSource();
    }

    public DataSource<I> getDataSource() {
        return dataSource;
    }

    public void clearData() {
        currentCount = 0;
        pastVisibleItems = 0;
        isEnd = false;

        if (adapter != null) {
            adapter.clear();
            adapter.notifyDataSetChanged();
        }
    }

    public void refreshData(boolean showProgress) {
        try {
            if (getDataSource() == null) {
                setDataSource(newDataSource());
            }
            if (getDataSource() != null) {
                clearData();
                loadItems(showProgress);
            }
        } catch (Exception e) {
            onDataTaskException(e);
        }
    }


    public int findFirstVisibleItemPosition(boolean completelyVisible) {
        return itemList.findFirstVisibleItemPosition(completelyVisible);
    }

    public int findLastVisibleItemPosition(boolean completelyVisible) {
        return itemList.findLastVisibleItemPosition(completelyVisible);
    }

    protected View findOneVisibleChild(int fromIndex, int toIndex, boolean completelyVisible,
                                       boolean acceptPartiallyVisible) {
        return itemList.findOneVisibleChild(fromIndex, toIndex, completelyVisible, acceptPartiallyVisible);
    }

    private boolean isItemVisible(int index) {
        return itemList.isItemVisible(index);
    }

    public void scrollToIndex(int index) {
        scrollToIndex(index, 0);
    }

    public void toIndex(int index, int offset) {
        layoutManager.scrollToPositionWithOffset(index, offset);
    }

    public void scrollToIndex(int index, int textOffset) {
        if (adapter.getItemCount() > index) {
            toIndex(index, textOffset);
        } else {
            moveToIndex = new MoveTask();
            loadItems(index + pageSize, true, moveToIndex, index, textOffset);
        }
    }

    public void onSwipeRefresh() {
        if (!isLoading) {
            refreshData(false);
        } else {
            swipeRefresh.setRefreshing(false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(listLayout, container,
                false);
        progressBar = GuiUtils.getView(rootView, R.id.load_progress);
        progressBarText = GuiUtils.getView(rootView, R.id.loading_text);
        loadMoreBar = GuiUtils.getView(rootView, R.id.load_more);
        itemList = GuiUtils.getView(rootView, R.id.items);
        swipeRefresh = GuiUtils.getView(rootView, R.id.refresh);
        emptyView = GuiUtils.getView(rootView, R.id.empty_view);
        swipeRefresh.setOnRefreshListener(this::onSwipeRefresh);
        if (adapter == null) {
            adapter = newAdapter();
        }
        try {
            setDataSource(newDataSource());
        } catch (Exception e) {
            onDataTaskException(e);
        }
        layoutManager = new LinearLayoutManager(rootView.getContext());
        itemList.setLayoutManager(layoutManager);
        itemList.setAdapter(adapter);
        itemList.setItemAnimator(new DefaultItemAnimator());
        itemList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int visibleItemCount, totalItemCount;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                RecyclerView.LayoutManager mLayoutManager = itemList.getLayoutManager();
                visibleItemCount = mLayoutManager.getChildCount();
                totalItemCount = mLayoutManager.getItemCount();
                pastVisibleItems = layoutManager.findFirstVisibleItemPosition();
                if (autoLoadMoreOnScroll && (visibleItemCount + pastVisibleItems) >= totalItemCount) {
                    loadItems(true);
                }
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH && enableScrollbar) {
            scroller = (FastScroller) rootView.findViewById(R.id.fast_scroller);
            scroller.setRecyclerView(itemList);
            GuiUtils.fadeOut(scroller, 0, 100);
            itemList.addOnScrollListener(scroller.getOnScrollListener());
            final Handler scrollHandler = new Handler();
            itemList.addOnScrollListener(new RecyclerView.OnScrollListener() {

                private Runnable fadeOut;
                private boolean visible = false;

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    Runnable newFadeOut = new Runnable() {
                        @Override
                        public void run() {
                            visible = false;
                            GuiUtils.fadeOut(scroller, 0, 1000);
                        }
                    };
                    if (fadeOut != null) {
                        scrollHandler.removeCallbacks(fadeOut);
                    }
                    if (!visible || scroller.getVisibility() == View.INVISIBLE) {
                        GuiUtils.fadeIn(scroller, 0, 100);
                        visible = true;
                    }
                    scrollHandler.postDelayed(fadeOut = newFadeOut, 2000);
                }
            });
        } else {
            ((ViewGroup) rootView).removeView(rootView.findViewById(R.id.fast_scroller));
        }
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        firstLoad(true);
    }

    public void firstLoad(boolean scroll) {
        if (getAdapter() != null && getDataSource() != null && !isEnd && getAdapter().getItems().isEmpty()) {
            if (dataTask != null) {
                dataTask.cancel(true);
            }
            loadMoreBar.setVisibility(View.GONE);
            loadItems(false);
        } else {
            stopLoading();
        }
        if (pastVisibleItems > 0 && scroll) {
            layoutManager.scrollToPositionWithOffset(pastVisibleItems, 0);
        }
    }

    @Override
    public void onStart() {
        onStartList();
        super.onStart();
    }

    @Override
    public void onStop() {
        onStopList();
        super.onStop();
    }

    protected void onStopList() {
        if (retainInstance && Constants.App.HIDE_TOOLBAR_BY_DEFAULT) {
            getBaseActivity().disableFullCollapsingToolbar();
        }
    }

    protected void onStartList() {
        if (retainInstance && Constants.App.HIDE_TOOLBAR_BY_DEFAULT) {
            getBaseActivity().enableFullCollapsingToolbar();
        }
    }

    public void onDataTaskException(Throwable ex) {
        Log.e(TAG, "Cant get new Items", ex);
        stopLoading();
        ErrorFragment.show(ListFragment.this, R.string.error_network);
    }


    public class MoveTask extends AsyncTask<Object, Void, Void> {
        protected int index = 0;
        protected int offsetLines = 0;

        @Override
        protected Void doInBackground(Object... params) {
            index = (int) params[0];
            offsetLines = (int) params[1];
            return null;
        }

        @Override
        protected void onPostExecute(Void empty) {
            if (this == moveToIndex) {
                toIndex(index, offsetLines);
            }
        }
    }

    public class FilterTask implements Runnable {

        protected final ItemListAdapter.FilterEvent query;

        public FilterTask(ItemListAdapter.FilterEvent query) {
            this.query = query;
        }

        @Override
        public void run() {
            if (itemList != null) {
                itemList.scrollToPosition(adapter.getItemCount() - adapter.getItems().size());
                adapter.filter(query);
                filterTask = null;
                loadItems(true);
            }
        }
    }

}


