package ru.kazantsev.template.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import ru.kazantsev.template.R;
import ru.kazantsev.template.adapter.FragmentPagerAdapter;
import ru.kazantsev.template.lister.DataSource;
import ru.kazantsev.template.lister.SafeAddItems;
import ru.kazantsev.template.lister.SafeDataTask;
import ru.kazantsev.template.util.GuiUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 0shad on 26.10.2015.
 */
public abstract class PagerFragment<I, F extends BaseFragment> extends BaseFragment implements SafeAddItems<I> {

    private static final String TAG = PagerFragment.class.getSimpleName();

    protected ProgressBar loadMoreBar;
    protected PagerTabStrip pagerHeader;
    protected ViewPager pager;
    protected FragmentPagerAdapter<I, F> adapter;
    protected DataSource<I> dataSource;
    protected TabLayout tabLayout;
    protected volatile boolean isLoading = false;
    protected volatile boolean isEnd = false;
    protected int pagesSize = 50;
    protected int currentCount = 0;
    protected SafeDataTask<I> dataTask;

    protected int needMore = 0;
    protected int currentItem = 0;
    protected List<I> currentItems;

    protected boolean autoLoadMore = true;
    protected boolean tabStripMode = true;

    public PagerFragment() {
    }

    public PagerFragment(DataSource<I> dataSource) {
        this.dataSource = dataSource;
    }

    public void setDataSource(DataSource<I> dataSource) {
        this.dataSource = dataSource;
    }

    protected  DataSource<I> newDataSource() throws Exception {
        return getDataSource();
    }

    public DataSource<I> getDataSource() {
        return dataSource;
    }

    public void onPostLoadItems() {
    }

    public boolean isLoading() {
        return isLoading;
    }

    public boolean isEnd() {
        return isEnd;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView;
        if(tabStripMode) {
           rootView = inflater.inflate(R.layout.fragment_pager, container, false);
        } else {
           rootView = inflater.inflate(R.layout.fragment_tabs, container, false);
        }
        pager = GuiUtils.getView(rootView, R.id.pager);
        pagerHeader = GuiUtils.getView(rootView, R.id.pager_header);
        loadMoreBar = GuiUtils.getView(rootView, R.id.load_more);
        if(adapter == null) {
            adapter = newAdapter(currentItems);
        }
        try {
            setDataSource(newDataSource());
        } catch (Exception e) {
            onDataTaskException(e);
        }
        if (currentItems == null) {
            if(adapter != null && adapter.getItems() != null) {
                currentItems = adapter.getItems();
            } else {
                currentItems = new ArrayList<>();
            }
        }
        pager.setAdapter(adapter);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                PagerFragment.this.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                PagerFragment.this.onPageSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                PagerFragment.this.onPageScrollStateChanged(state);
            }
        });
        if(!tabStripMode) {
            tabLayout = rootView.findViewById(R.id.tab_layout);
            tabLayout.setupWithViewPager(pager);
        }
        return rootView;
    }

    public void firstLoad(boolean scroll) {
        if (getAdapter() != null && getDataSource() != null && !isEnd && getAdapter().getItems().isEmpty()) {
            if (dataTask != null) {
                dataTask.cancel(true);
            }
            loadItems(pagesSize, true);
        } else {
            stopLoading();
        }
        if (scroll) {
            pager.setCurrentItem(currentItem);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        firstLoad(true);
    }

    public void startLoading(boolean showProgress) {
        isLoading = true;
        if (loadMoreBar != null && showProgress) {
            loadMoreBar.setVisibility(View.VISIBLE);
        }
    }

    public void stopLoading() {
        isLoading = false;
        if (loadMoreBar != null) {
            loadMoreBar.setVisibility(View.GONE);
        }
    }

    protected void loadItems(int count, boolean showProgress, AsyncTask onElementsLoadedTask, Object... params) {
        if (isLoading || isEnd) {
            return;
        }
        startLoading(showProgress);
        if (dataSource != null) {
            SafeDataTask<I> dataTask = new SafeDataTask<>(getDataSource(), this, currentCount, count, onElementsLoadedTask, params);
            if (this.dataTask == null) {
                isLoading = true;
                dataTask.execute();
            }
            this.dataTask = dataTask;
        }
    }

    protected void loadItems(int count, boolean showProgress) {
        loadItems(count, showProgress, null, null);
    }

    @Override
    public void addItems(List<I> items, int awaitedCount) {
        if (items == null || items.size() == 0) {
            isEnd = true;
        } else if(adapter != null) {
            adapter.addItems(items, false);
            needMore = awaitedCount - items.size();
        }
        needMore = 0;
    }

    @Override
    public void finishLoad(List<I> items, AsyncTask onElementsLoadedTask, Object[] loadedTaskParams) {
        isLoading = false;
        if (needMore > 0 && !isEnd && autoLoadMore) {
            loadItems(needMore, true);
        } else {
            currentCount = adapter.getCount();
            if (onElementsLoadedTask != null) {
                onElementsLoadedTask.execute(loadedTaskParams);
            }
            if (pager != null && adapter != null) {
                adapter.notifyDataSetChanged();
                stopLoading();
                if (isAdded()) {
                    onPostLoadItems();
                }
            }
        }
    }

    @Override
    public void addFinalItems(List<I> items) {
        addItems(items, items.size());
        finishLoad(items, null, null);
    }

    protected void clearData() {
        currentCount = 0;
        isEnd = false;
        if (adapter != null) {
            adapter.getItems().clear();
            adapter.notifyDataSetChanged();
            pager.setAdapter(adapter);
        }
    }

    public void refreshData(boolean showProgress) {
        clearData();
        loadItems(pagesSize, showProgress);
    }


    public abstract FragmentPagerAdapter<I, F> newAdapter(List<I> currentItems);

    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    public void onPageSelected(int position) {
        if(!isEnd && position == currentCount - 1) {
            loadItems(pagesSize, true);
        }
        currentItem = position;
    }

    public void onPageScrollStateChanged(int state) {

    }

    public void onDataTaskException(Throwable ex) {
        Log.e(TAG, "Cant get new Items: ", ex);
        ErrorFragment.show(PagerFragment.this, R.string.error);
    }

    public FragmentPagerAdapter<I,F> getAdapter() {
        return adapter;
    }

    public class SetAfterFirstLoad extends AsyncTask<Integer, Void, Integer> {
        @Override
        protected Integer doInBackground(Integer... integers) {
            return integers[0];
        }

        @Override
        protected void onPostExecute(Integer item) {
            pager.setCurrentItem(item);
        }
    }

}
