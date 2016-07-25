package ru.kazantsev.template.activity;

import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import ru.kazantsev.template.R;
import ru.kazantsev.template.util.FragmentBuilder;
import ru.kazantsev.template.util.GuiUtils;


public abstract class NavigationActivity extends BaseActivity {

    ArrayAdapter<Integer> menuAdapter;
    ListView navigationListMenu;
    FrameLayout navigationHeader;
    LinearLayout tabBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        toolbarClassic = true;
        super.onCreate(savedInstanceState);
        tabBar = GuiUtils.inflate(getContentLayout(), R.layout.tab_bar);
        getContentLayout().addView(tabBar);
        ViewGroup.LayoutParams params = getNavigationView().getLayoutParams();
        Point size = GuiUtils.getScreenSize(this);
        params.width = (int) ((size.x < size.y ? size.x : size.y) * 0.8);
        getNavigationView().setLayoutParams(params);
        getNavigationView().addView(GuiUtils.inflate(getNavigationView(), R.layout.navigation_menu));
        navigationHeader= (FrameLayout) getNavigationView().findViewById(R.id.navigation_menu_header);
        if(getNavigationHeaderId() > 0) {
            navigationHeader.addView(GuiUtils.inflate(navigationHeader, getNavigationHeaderId()));
        }
        navigationListMenu = GuiUtils.getView(getNavigationView(), R.id.navigation_menu_list);
        menuAdapter = new ArrayAdapter<Integer>(this, getNavigationViewId(), getNavigationIds()) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view;
                if (convertView == null) {
                    view = GuiUtils.inflate(parent, getNavigationViewId());
                } else {
                    view = convertView;
                }
                Integer item = getItem(position);
                view.setTag(item);
                onBindNavigationView(item, view);
                return view;
            }

        };
        navigationListMenu.setAdapter(menuAdapter);
        navigationListMenu.setOnItemClickListener((parent, view, position, id) -> onNavigationItemSelected((Integer) view.getTag(), view));
    }

    protected abstract @LayoutRes int getNavigationViewId();

    protected abstract Integer[] getNavigationIds();

    protected abstract void onBindNavigationView(Integer id, View navigationView);

    protected abstract void onNavigationItemSelected(Integer id, View item);

    protected @LayoutRes int getNavigationHeaderId() {
        return -1;
    }

    protected View addTab (@LayoutRes int tabId, @LayoutRes int border, Class<? extends Fragment> fragmentClass, Bundle bundle) {
        LinearLayout tab = GuiUtils.inflate(tabBar, tabId);
        tab.setTag(fragmentClass);
        tab.setOnClickListener(v -> {
            FragmentBuilder builder = new FragmentBuilder(getSupportFragmentManager());
            builder.putArgs(bundle);
            builder.newFragment();
            replaceFragment((Class<Fragment>) v.getTag(), builder);
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
