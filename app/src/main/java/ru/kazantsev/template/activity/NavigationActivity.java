package ru.kazantsev.template.activity;

import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import ru.kazantsev.template.R;
import ru.kazantsev.template.util.GuiUtils;

import java.util.ArrayList;
import java.util.List;


public abstract class NavigationActivity<T> extends BaseActivity {

    ArrayAdapter<T> menuAdapter;
    ListView navigationListMenu;
    FrameLayout navigationHeader;
    LinearLayout tabBar;
    boolean enableButtomTabs = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (enableButtomTabs) {
            tabBar = GuiUtils.inflate(getContentLayout(), R.layout.tab_bar);
            getContentLayout().addView(tabBar);
        }
        ViewGroup.LayoutParams params = getNavigationView().getLayoutParams();
        Point size = GuiUtils.getScreenSize(this);
        params.width = (int) ((size.x < size.y ? size.x : size.y) * 0.8);
        getNavigationView().setLayoutParams(params);
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
                T item = getItem(position);
                view.setTag(item);
                onBindNavigationView(position, item, view);
                return view;
            }

        };
        navigationListMenu.setAdapter(menuAdapter);
        navigationListMenu.setOnItemClickListener((parent, view, position, id) -> {
            if(onNavigationItemSelected(position, (T) view.getTag(), view)) {
                drawerLayout.closeDrawers();
            }
        });
    }

    protected abstract
    @LayoutRes
    int getNavigationViewId();

    protected List<T> getNavigationIds() {
        return new ArrayList<T>();
    }

    protected abstract void onBindNavigationView(int position, T item, View navigationView);

    protected abstract boolean onNavigationItemSelected(int position, T item, View navigationView);

    protected
    @LayoutRes
    int getNavigationHeaderId() {
        return -1;
    }

    protected void onTabClick(View tab) {}

    public void addNavigationMenu(T item) {
        menuAdapter.add(item);
    }

    public void setNavigationMenu(int position, T item) {
        if(menuAdapter.getCount() > position) {
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
