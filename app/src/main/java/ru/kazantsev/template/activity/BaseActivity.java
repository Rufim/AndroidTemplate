package ru.kazantsev.template.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import ru.kazantsev.template.R;
import ru.kazantsev.template.domain.Constants;
import ru.kazantsev.template.domain.event.Event;
import ru.kazantsev.template.domain.event.FragmentAttachedEvent;
import ru.kazantsev.template.fragments.ListFragment;
import ru.kazantsev.template.mvp.compact.MvpCompactActivityImpl;
import ru.kazantsev.template.mvp.compact.MvpCompactFactory;
import ru.kazantsev.template.util.FragmentBuilder;
import ru.kazantsev.template.util.GuiUtils;
import ru.kazantsev.template.util.PermissionUtils;
import ru.kazantsev.template.util.TextUtils;

import static android.R.attr.textColor;

/**
 * Created by 0shad on 11.07.2015.
 */
public abstract class BaseActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    protected LinearLayout rootLayout;
    protected BottomNavigationView bottomNavigationView;
    protected RelativeLayout contentLayout;
    protected CoordinatorLayout coordinatorLayout;
    protected AppBarLayout appBarLayout;
    protected CollapsingToolbarLayout collapsingToolbarLayout;
    //protected NestedScrollView nestedScrollView;

    protected FrameLayout container;
    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    protected Toolbar toolbar;
    protected View toolbarShadow;

    protected ActionBar actionBar;
    protected ActionBarDrawerToggle actionBarDrawerToggle;

    protected boolean disableNavigationBar = false;
    protected boolean toolbarClassic = false;
    protected boolean enableFragmentCache = false;
    protected boolean clearBackStack = true;
    protected boolean enableBottomNavigation = false;

    ArrayList<BundleCache> fragmentBundleCache = new ArrayList<>();
    HashMap<String, List<PermissionAction>> waitingPermissionActions = new HashMap<>();

    private MvpCompactActivityImpl mvpCompact = null;

    public interface BackCallback {
        boolean allowBackPress();
    }

    public interface MenuAction {
        void doAction(MenuItem item);
    }

    public interface PermissionAction {
        void doAction(boolean permissionGained);
    }

    public BaseActivity() {
        if(Constants.App.USE_MOXY) {
            mvpCompact = MvpCompactFactory.buildMvpCompactActivity(this);
        }
    }

    public BaseActivity(boolean disableNavigationBar, boolean toolbarClassic, boolean enableFragmentCache, boolean clearBackStack, boolean enableBottomNavigation) {
        this();
        this.disableNavigationBar = disableNavigationBar;
        this.toolbarClassic = toolbarClassic;
        this.enableFragmentCache = enableFragmentCache;
        this.clearBackStack = clearBackStack;
        this.enableBottomNavigation = enableBottomNavigation;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(mvpCompact != null) {
            mvpCompact.onCreate(savedInstanceState);
        }
        if (toolbarClassic) {
            setContentView(R.layout.activity_main_classic);
        } else {
            setContentView(R.layout.activity_main);
        }
        rootLayout = GuiUtils.getView(this, R.id.root_layout);
        contentLayout = GuiUtils.getView(this, R.id.content_layout);
        container = GuiUtils.getView(this, R.id.container);
        drawerLayout = GuiUtils.getView(this, R.id.drawer_layout);
        navigationView = GuiUtils.getView(this, R.id.navigation_drawer);
        toolbar = GuiUtils.getView(this, R.id.toolbar);
        toolbarShadow = GuiUtils.getView(this, R.id.toolbar_shadow);
        appBarLayout = GuiUtils.getView(this, R.id.app_bar);
        collapsingToolbarLayout = GuiUtils.getView(this, R.id.collapsing_toolbar);
       // nestedScrollView = GuiUtils.getView(this, R.id.nested_scroll);
        coordinatorLayout = GuiUtils.getView(this, R.id.coordinator_layout);
        bottomNavigationView = GuiUtils.getView(this, R.id.navigation);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        actionBar = getSupportActionBar();
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            if (!menuItem.isChecked()) menuItem.setChecked(true);
            drawerLayout.closeDrawers();
            onNavigationItemSelected(menuItem);
            return true;
        });
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
                BaseActivity.this.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank
                super.onDrawerOpened(drawerView);
                BaseActivity.this.onDrawerOpened(drawerView);
            }

        };
        //Listen for changes in the back stack
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        //Handle when activity is recreated like on orientation Change
        //Setting the actionbarToggle to drawer layout
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        if(enableBottomNavigation) {
            onCreateBottomNavigation(bottomNavigationView);
        } else {
            bottomNavigationView.setVisibility(View.GONE);
        }
        if(!disableNavigationBar) {
            onCreateNavigation(navigationView);
        } else {
            lockDrawerClosed();
        }
        shouldDisplayHomeUp();
        if (savedInstanceState != null && savedInstanceState.containsKey(Constants.ArgsName.FRAGMENT_CACHE)) {
            fragmentBundleCache = savedInstanceState.getParcelableArrayList(Constants.ArgsName.FRAGMENT_CACHE);
        }
        float elev = GuiUtils.getThemeDimen(this, R.attr.toolbarElevationSize);
        if (elev > 0) {
            setToolbarElevation((int) elev);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mvpCompact != null) {
            mvpCompact.onResume();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mvpCompact != null) {
            mvpCompact.onDestroy();
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (!isConfigChange(savedInstanceState)) {
            if(getIntent() != null) {
                handleIntent(getIntent());
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (getCurrentFragment() != null)
            outState.putString(Constants.ArgsName.LAST_FRAGMENT_TAG, getCurrentFragment().getTag());
        outState.putBoolean(Constants.ArgsName.CONFIG_CHANGE, true);
        if (enableFragmentCache) {
            outState.putParcelableArrayList(Constants.ArgsName.FRAGMENT_CACHE, fragmentBundleCache);
        }
        super.onSaveInstanceState(outState);
        if(mvpCompact != null) {
            mvpCompact.onSaveInstanceState(outState);
        }
    }

    protected void onCreateBottomNavigation(BottomNavigationView bottomNavigationView) {

    }

    protected void onCreateNavigation(NavigationView navigationView) {

    }

    public void openDrawer() {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    public void closeDrawer() {
        drawerLayout.closeDrawer(GravityCompat.START);
    }

    public void lockDrawerClosed() {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    public void hideBottomNavigation() {
        bottomNavigationView.setVisibility(View.GONE);
    }

    public void showBottomNavication() {
        bottomNavigationView.setVisibility(View.VISIBLE);
    }

    public void unlockDrawer() {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    public void hideActionBar() {
        if (appBarLayout != null) {
            appBarLayout.setVisibility(View.GONE);
            View behaviorLayout = toolbarClassic ? contentLayout : drawerLayout;
            ViewGroup.LayoutParams layoutParams = behaviorLayout.getLayoutParams();
            if (layoutParams instanceof CoordinatorLayout.LayoutParams) {
                CoordinatorLayout.LayoutParams params =
                        (CoordinatorLayout.LayoutParams) behaviorLayout.getLayoutParams();

                params.setBehavior(null);
            }
            behaviorLayout.requestLayout();
        }
        if (toolbarShadow != null) {
            toolbarShadow.setVisibility(View.GONE);
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        if (getActionBar() != null) {
            getActionBar().hide();
        }
    }

    public void showActionBar() {
        if (appBarLayout != null) {
            appBarLayout.setVisibility(View.VISIBLE);
            View behaviorLayout = toolbarClassic ? contentLayout : drawerLayout;
            ViewGroup.LayoutParams layoutParams = behaviorLayout.getLayoutParams();
            if (layoutParams instanceof CoordinatorLayout.LayoutParams) {
                CoordinatorLayout.LayoutParams params =
                        (CoordinatorLayout.LayoutParams) behaviorLayout.getLayoutParams();

                params.setBehavior(new AppBarLayout.ScrollingViewBehavior());
            }
            behaviorLayout.requestLayout();
        }
        if (toolbarShadow != null) {
            toolbarShadow.setVisibility(View.VISIBLE);
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().show();
        }
        if (getActionBar() != null) {
            getActionBar().show();
        }
    }

    public void showKeyboard(EditText editText) {
        if(editText != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if(intent != null) {
            // fix for earlier android  versions that send intent even if onQueryTextSubmit returns true
            if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
                Fragment fragment = getCurrentFragment();
                if (fragment instanceof ListFragment) {
                    ListFragment list = (ListFragment) fragment;
                    if (list.isEnableFiltering()) {
                        return;
                    }
                }
            }
            handleIntent(intent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mvpCompact != null) {
            mvpCompact.onStart();
        }
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
        if(mvpCompact != null) {
            mvpCompact.onStop();
        }
    }



    @Override
    public void onBackPressed() {
        Fragment fr = getCurrentFragment();
        if (fr instanceof BackCallback) {
            if (((BackCallback) fr).allowBackPress()) {
                super.onBackPressed();
                System.gc();
            }
        } else {
            super.onBackPressed();
            System.gc();
        }
    }

    @SuppressLint("ResourceType")
    public void setToolbarElevation(int elevation) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) toolbarShadow.getLayoutParams();
            params.height = 1;
            toolbarShadow.setBackgroundResource(GuiUtils.getThemeResource(this, R.attr.colorPrimary));
            toolbarShadow.setElevation(elevation);
            toolbarShadow.setLayoutParams(params);
            toolbarShadow.requestLayout();
            if(appBarLayout != null) {
                appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {

                    boolean stateHidden = false;

                    @SuppressLint("ResourceType")
                    @Override
                    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                        if(-verticalOffset == appBarLayout.getHeight() && !stateHidden) {
                            stateHidden = true;
                            toolbarShadow.setBackgroundResource(GuiUtils.getThemeResource(BaseActivity.this, R.attr.colorPrimaryDark));
                        } else if(-verticalOffset < appBarLayout.getHeight() && stateHidden) {
                            stateHidden = false;
                            toolbarShadow.setBackgroundResource(GuiUtils.getThemeResource(BaseActivity.this, R.attr.colorPrimary));
                        }
                    }
                });
            }
        } else {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) toolbarShadow.getLayoutParams();
            params.height = elevation;
            toolbarShadow.setLayoutParams(params);
            toolbarShadow.requestLayout();
        }
    }

    protected void onBackPressedOriginal() {
        super.onBackPressed();
    }

    protected abstract void handleIntent(@NonNull Intent intent);

    protected boolean isConfigChange(@Nullable Bundle savedInstanceState) {
        return savedInstanceState != null && savedInstanceState.getBoolean(Constants.ArgsName.CONFIG_CHANGE, false);
    }

    protected boolean onNavigationItemSelected(MenuItem item) {
        return false;
    }

    protected void onDrawerClosed(View drawerView) {
    }

    protected void onDrawerOpened(View drawerView) {
    }

    protected void onFragmentAttached(Fragment fragment) {
    }

    @Subscribe
    public void onEvent(FragmentAttachedEvent fragmentAttached) {
        onFragmentAttached(fragmentAttached.fragment);
    }


    public int getCheckedNavigationItem() {
        for (int i = 0; i < navigationView.getMenu().size(); i++) {
            MenuItem item = navigationView.getMenu().getItem(i);
            if (item.isChecked()) return item.getItemId();
        }
        return -1;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home && clearBackStack) {
            if (isHomeBack()) {
                onBackPressed();
                return true;
            }
            if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        try {
            List<String> unhandledPermissions = PermissionUtils.getUnhandledPermissions(requestCode, permissions, grantResults);
            if (unhandledPermissions.size() > 0) {
                for (String unhandledPermission : unhandledPermissions) {
                    onDenyPermission(unhandledPermission);
                }
            }
            for (String permission : permissions) {
                if (!unhandledPermissions.contains(permission)) {
                    onGainPermission(permission);
                }
            }
        } catch (Exception e) {
            Log.w(BaseActivity.class.getSimpleName(), e.getMessage());
        }
    }

    public void doActionWithPermission(String permission, PermissionAction permissionAction) {
        if (PermissionUtils.hasPermissions(this, permission)) {
            permissionAction.doAction(true);
        } else {
            PermissionUtils.requestPermissions(this, permission);
            addWaitingPermissionAction(permission, permissionAction);
        }
    }

    private void addWaitingPermissionAction(String permission, PermissionAction action) {
        if (waitingPermissionActions.containsKey(permission)) {
            waitingPermissionActions.get(permission).add(action);
        } else {
            ArrayList<PermissionAction> actions = new ArrayList<>();
            actions.add(action);
            waitingPermissionActions.put(permission, actions);
        }
    }

    public void onGainPermission(String gainedPermission) {
        Iterator<PermissionAction> it = waitingPermissionActions.get(gainedPermission).iterator();
        while (it.hasNext()) {
            it.next().doAction(true);
            it.remove();
        }
    }

    public void onDenyPermission(String deniedPermission) {
        Iterator<PermissionAction> it = waitingPermissionActions.get(deniedPermission).iterator();
        while (it.hasNext()) {
            it.next().doAction(false);
            it.remove();
        }
    }

    protected boolean isHomeBack() {
        return getSupportFragmentManager().getBackStackEntryCount() > 0 || !isTaskRoot();
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
    }

    protected void postEvent(Event event) {
        EventBus.getDefault().post(event);
    }

    protected String getResString(@StringRes int id) {
        return getResources().getString(id);
    }

    public @Nullable Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.container);
    }

    protected Fragment getLastFragment(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            String lastTag = savedInstanceState.getString(Constants.ArgsName.LAST_FRAGMENT_TAG);
            if (lastTag != null) {
                FragmentManager manager = getSupportFragmentManager();
                return manager.findFragmentByTag(lastTag);
            }
        }
        return null;
    }

    public @NonNull NavigationView getNavigationView() {
        return navigationView;
    }

    public @NonNull Toolbar getToolbar() {
        return toolbar;
    }

    public @NonNull View getToolbarShadow() {
        return toolbarShadow;
    }

    public @Nullable ActionBar getCurrentActionBar() {
        return actionBar;
    }

    public @NonNull LinearLayout getRootLayout() {
        return rootLayout;
    }

    public @NonNull BottomNavigationView getBottomNavigationView() {
        return bottomNavigationView;
    }

    public @NonNull RelativeLayout getContentLayout() {
        return contentLayout;
    }

    public @NonNull CoordinatorLayout getCoordinatorLayout() {
        return coordinatorLayout;
    }

    public @NonNull AppBarLayout getAppBarLayout() {
        return appBarLayout;
    }

    public @NonNull FrameLayout getContainer() {
        return container;
    }

    public @NonNull ActionBarDrawerToggle getActionBarDrawerToggle() {
        return actionBarDrawerToggle;
    }

    public @NonNull DrawerLayout getDrawerLayout() {
        return drawerLayout;
    }

    // Использовать при изменении ориентации экрана.
    public <F extends Fragment> void restoreFragment(F fragment) {
        new FragmentBuilder(getSupportFragmentManager()).replaceFragment(R.id.container, fragment);
    }

    public <F extends Fragment> void restoreFragment(Class<F> fragmentClass) {
        new FragmentBuilder(getSupportFragmentManager()).replaceFragment(R.id.container, fragmentClass);
    }

    public FragmentBuilder newFragmentBuilder() {
        return new FragmentBuilder(getSupportFragmentManager());
    }

    public <F extends Fragment> void replaceFragment(Class<F> fragmentClass) {
        replaceFragment(fragmentClass, new FragmentBuilder(getSupportFragmentManager()));
    }

    public <F extends Fragment> void replaceFragment(Class<F> fragmentClass, FragmentBuilder builder) {
        replaceFragment(fragmentClass, builder, null);
    }

    public <F extends Fragment> void replaceFragment(Class<F> fragmentClass, FragmentBuilder builder, String name) {
        if (clearBackStack && getSupportFragmentManager().getBackStackEntryCount() > 0) {
            builder.clearBackStack();
        }
        if (enableFragmentCache) {
            cacheBundle(getCurrentFragment());
        }
        if (name == null) {
            builder.replaceFragment(R.id.container, fragmentClass);
        } else {
            builder.replaceFragment(R.id.container, fragmentClass, name);
        }
        supportInvalidateOptionsMenu();
    }

    public <F extends Fragment> void replaceFragment(F fragment) {
        replaceFragment(fragment, new FragmentBuilder(getSupportFragmentManager()));
    }

    public <F extends Fragment> void replaceFragment(F fragment, FragmentBuilder builder) {
        replaceFragment(fragment, builder, null);
    }


    public <F extends Fragment> void replaceFragment(F fragment, FragmentBuilder builder, String name) {
        if (clearBackStack && getSupportFragmentManager().getBackStackEntryCount() > 0) {
            builder.clearBackStack();
        }
        if (enableFragmentCache) {
            cacheBundle(getCurrentFragment());
        }
        if (name == null) {
            builder.replaceFragment(R.id.container, fragment);
        } else {
            builder.replaceFragment(R.id.container, fragment, name);
        }
        supportInvalidateOptionsMenu();
    }

    public void cacheBundle(Fragment fragment) {
        cacheBundle(fragment, null);
    }

    public void cacheBundle(Fragment fragment, String tag) {
        if (fragment != null && fragment.getArguments() != null && fragment.getArguments().size() > 0) {
            tag = tag != null ? tag : fragment.getTag();
            if (TextUtils.isEmpty(tag)) {
                tag = fragment.getClass().getSimpleName();
            }
            fragmentBundleCache.add(new BundleCache(tag, fragment.getArguments()));
        }
    }

    public Bundle getCachedBoundle(String tag) {
        for (BundleCache bundleCache : fragmentBundleCache) {
            if ((tag == null && bundleCache.tag == null) || (tag != null && tag.equals(bundleCache.tag))) {
                return bundleCache.bundle;
            }
        }
        return null;
    }

    public void showSnackbar(@StringRes int message) {
        showSnackbar(getResString(message));
    }

    public void showSnackbar(String message) {
        int defaultBackgroundResource = android.R.attr.windowBackground;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            defaultBackgroundResource = android.R.attr.colorPrimary;
        }
        showSnackbar(message, GuiUtils.getThemeColor(this, defaultBackgroundResource));
    }

    protected void showSnackbar(String message, @ColorInt int backgroundColor) {
        int textColorSnackbar = GuiUtils.getThemeResource(this, R.attr.textColorSnackbar);
        if (textColorSnackbar == 0) {
            textColorSnackbar = textColor;
        } else {
            textColorSnackbar = R.attr.textColorSnackbar;
        }
        showSnackbar(message, GuiUtils.getThemeColor(this, textColorSnackbar), backgroundColor);
    }

    protected void showSnackbar(String message, @ColorInt int textColor, @ColorInt int backgroundColor) {
        GuiUtils.makeCustomSnackbar(getContainer(),
                message,
                textColor,
                backgroundColor).show();
    }

    @Override
    public void onBackStackChanged() {
        shouldDisplayHomeUp();
    }

    public void shouldDisplayHomeUp() {
        //Enable Up button only  if there are entries in the back stack
        if (actionBar != null && clearBackStack) {
            boolean canback = isHomeBack();
            if (canback) {
                actionBar.setHomeAsUpIndicator(ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_material));
                if (disableNavigationBar) {
                    actionBar.setDisplayHomeAsUpEnabled(true);
                }
            } else {
                actionBar.setHomeAsUpIndicator(null);
                if (disableNavigationBar) {
                    actionBar.setDisplayHomeAsUpEnabled(false);
                }
                actionBarDrawerToggle.syncState();
            }
            if (!disableNavigationBar) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        //This method is called when the up button is pressed. Just the pop back stack.
        getSupportFragmentManager().popBackStack();
        return true;
    }

    public void enableFullCollapsingToolbar() {
       AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) collapsingToolbarLayout.getLayoutParams();
       params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
               | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED
               | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
       collapsingToolbarLayout.requestLayout();
    }

    public void disableFullCollapsingToolbar() {
       AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) collapsingToolbarLayout.getLayoutParams();
       params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
               | AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED
               | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
       collapsingToolbarLayout.requestLayout();
    }

    public static class BundleCache implements Parcelable {
        final String tag;
        final Bundle bundle;

        private BundleCache(String tag, Bundle bundle) {
            this.tag = tag;
            this.bundle = bundle;
        }

        protected BundleCache(Parcel in) {
            tag = in.readString();
            bundle = in.readBundle(getClass().getClassLoader());
        }

        public static final Creator<BundleCache> CREATOR = new Creator<BundleCache>() {
            @Override
            public BundleCache createFromParcel(Parcel in) {
                return new BundleCache(in);
            }

            @Override
            public BundleCache[] newArray(int size) {
                return new BundleCache[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(tag);
            dest.writeBundle(bundle);
        }
    }
}
