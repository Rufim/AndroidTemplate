package ru.kazantsev.template.activity;

import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import ru.kazantsev.template.R;
import ru.kazantsev.template.util.GuiUtils;


public abstract class NavigationActivity<T> extends BaseActivity {

    protected ArrayAdapter<T> menuAdapter;
    protected ListView navigationListMenu;
    protected FrameLayout navigationHeader;
    protected LinearLayout tabBar;
    protected boolean enableButtomTabs = false;
    protected int selectedBackground = -1;
    protected Drawable usualBackground = null;
    protected int selectedIndex = -1;
    protected float navigationInPercentWidth = -1;
    protected int navigationFixedDpWidth = 250;

    WeakHashMap<Integer,View> scrapHeap = new WeakHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (enableButtomTabs) {
            tabBar = GuiUtils.inflate(getContentLayout(), R.layout.tab_bar);
            getContentLayout().addView(tabBar);
        }
        if(navigationInPercentWidth > 0) {
            Point size = GuiUtils.getScreenSize(this);
            setNavigationLayoutWidth((int) ((size.x < size.y ? size.x : size.y) * navigationInPercentWidth));
        } else if(navigationFixedDpWidth > 0) {
            setNavigationLayoutWidth(GuiUtils.dpToPx(navigationFixedDpWidth, this));
        }
        getNavigationView().addView(GuiUtils.inflate(getNavigationView(), R.layout.navigation_menu));
        navigationHeader = (FrameLayout) getNavigationView().findViewById(R.id.navigation_menu_header);
        if (getNavigationHeaderId() > 0) {
            navigationHeader.addView(GuiUtils.inflate(navigationHeader, getNavigationHeaderId()));
        }
        navigationListMenu = GuiUtils.getView(getNavigationView(), R.id.navigation_menu_list);
        menuAdapter = new ArrayAdapter<T>(this, getNavigationViewId(), getNavigationIds()) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view;
                if (convertView == null) {
                    view = GuiUtils.inflate(parent, getNavigationViewId());
                } else {
                    view = convertView;
                }
                if(selectedIndex > 0) {
                    if (position == selectedIndex) {
                        applyViewSelection(view);
                    } else if (convertView != null) {
                        cleanViewSelection(view);
                    }
                }
                T item = getItem(position);
                view.setTag(position);
                onBindNavigationView(position, item, view);
                return view;
            }

        };
        navigationListMenu.setAdapter(menuAdapter);
        navigationListMenu.setOnItemClickListener((parent, view, position, id) -> {
            if (onNavigationItemSelected(position, menuAdapter.getItem(position), view)) {
                setSelected(position);
                drawerLayout.closeDrawers();
            }
        });
        selectedBackground = GuiUtils.getThemeColor(this, R.attr.colorControlActivated);
        navigationListMenu.setRecyclerListener(new AbsListView.RecyclerListener() {
            @Override
            public void onMovedToScrapHeap(View view) {
                if(view.getTag() != null && view.getTag() instanceof Integer) {
                    scrapHeap.put((Integer) view.getTag(), view);
                } else {
                    Log.e(NavigationActivity.class.getSimpleName(), "Error while try to get scrap view please not use Tag or Navigation menu will work incorrectly");
                }
            }
        });
    }

    public void setNavigationLayoutWidth(int size) {
        ViewGroup.LayoutParams params = getNavigationView().getLayoutParams();
        params.width = size;
        getNavigationView().setLayoutParams(params);

    }

    protected abstract @LayoutRes int getNavigationViewId();

    protected List<T> getNavigationIds() {
        return new ArrayList<T>();
    }

    protected abstract void onBindNavigationView(int position, T item, View navigationView);

    protected abstract boolean onNavigationItemSelected(int position, T item, View navigationView);

    public void selectItem(int position) {
        if (navigationListMenu.getCount() > position && navigationListMenu.getCount() > 0) {
            if (onNavigationItemSelected(position, menuAdapter.getItem(position), navigationListMenu.getChildAt(position))) {
                setSelected(position);
            }
        }
    }

    public void cleanSelection() {
        if (selectedIndex >= 0) {
            if (navigationListMenu.getCount() > selectedIndex && navigationListMenu.getCount() > 0) {
                cleanViewSelection(getNavigationViewByPosition(selectedIndex, false));
            }
            selectedIndex = -1;
        }
    }

    public synchronized void setSelected(int position) {
        if (selectedIndex >= 0) {
            cleanViewSelection(getNavigationViewByPosition(selectedIndex, false));
        }
        View navView = getNavigationViewByPosition(position, true);
        if (navView != null) {
            usualBackground = navView.getBackground();
            applyViewSelection(navView);
            selectedIndex = position;
        }
    }

    protected View getNavigationViewByPosition(int position, boolean createNew) {
        if (menuAdapter.getCount() > position && menuAdapter.getCount() > 0) {
            final int firstListItemPosition = navigationListMenu.getFirstVisiblePosition();
            final int lastListItemPosition = firstListItemPosition + navigationListMenu.getChildCount() - 1;

            if (position < firstListItemPosition || position > lastListItemPosition) {
                View scrapView = scrapHeap.get(position);
                if(scrapView == null) {
                    return createNew ? navigationListMenu.getAdapter().getView(position, null, navigationListMenu) : null;
                } else {
                    return scrapView;
                }
            } else {
                final int childIndex = position - firstListItemPosition;
                return navigationListMenu.getChildAt(childIndex);
            }
        } else {
            return null;
        }
    }

    protected void applyViewSelection(View navigationView) {
        if (navigationView != null) {
            navigationView.setBackgroundColor(selectedBackground);
        }
    }


    protected void cleanViewSelection(View navigationView) {
         if (navigationView != null) {
            navigationView.setBackgroundDrawable(usualBackground);
        }
    }

    protected
    @LayoutRes
    int getNavigationHeaderId() {
        return -1;
    }

    protected void onTabClick(View tab) {
    }

    public void addNavigationMenu(T item) {
        menuAdapter.add(item);
    }

    public void setNavigationMenu(int position, T item) {
        if (menuAdapter.getCount() > position) {
            menuAdapter.add(item);
        } else {
            menuAdapter.insert(item, position);
        }
    }

    public void clearNavigationMenu() {
        menuAdapter.clear();
    }

    public void removeHeaderView() {
        navigationHeader.removeAllViews();
    }

    public void addHeader(View view) {
        navigationHeader.addView(view);
    }

    protected View addTab(@LayoutRes int tabId, @LayoutRes int border) {
        LinearLayout tab = GuiUtils.inflate(tabBar, tabId);
        tab.setOnClickListener(v -> {
            onTabClick(v);
        });
        if (tabBar.getChildCount() > 0) {
            tabBar.addView(GuiUtils.inflate(tabBar, border));
        }
        tabBar.addView(tab);
        return tab;
    }

    public LinearLayout getTabBar() {
        return tabBar;
    }

}
