package ru.kazantsev.template.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import ru.kazantsev.template.R;
import ru.kazantsev.template.domain.Constants;
import ru.kazantsev.template.domain.event.Event;
import ru.kazantsev.template.domain.event.FragmentAttachedEvent;
import ru.kazantsev.template.util.FragmentBuilder;
import ru.kazantsev.template.util.GuiUtils;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by 0shad on 11.07.2015.
 */
public abstract class BaseActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener {

    protected FrameLayout container;
    protected FrameLayout containerDetails;
    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    protected Toolbar toolbar;
    protected View mainBorder;

    protected ActionBar actionBar;
    protected ActionBarDrawerToggle actionBarDrawerToggle;

    public interface BackCallback {
        boolean allowBackPress();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        container = GuiUtils.getView(this, R.id.container);
        containerDetails = GuiUtils.getView(this, R.id.container_details);
        drawerLayout = GuiUtils.getView(this, R.id.drawer_layout);
        navigationView = GuiUtils.getView(this, R.id.navigation_drawer);
        toolbar = GuiUtils.getView(this, R.id.toolbar);
        mainBorder = GuiUtils.getView(this, R.id.main_border);
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
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        shouldDisplayHomeUp();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (!isConfigChange(savedInstanceState)) {
            handleIntent(getIntent());
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(Constants.ArgsName.LAST_FRAGMENT_TAG, getCurrentFragment().getTag());
        outState.putBoolean(Constants.ArgsName.CONFIG_CHANGE, true);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        Fragment fr = getCurrentFragment();
        if (fr instanceof BackCallback) {
            if (((BackCallback) fr).allowBackPress()) super.onBackPressed();
        } else {
            super.onBackPressed();
        }

    }

    protected abstract void handleIntent(Intent intent);

    protected boolean isConfigChange(@Nullable Bundle savedInstanceState) {
        return savedInstanceState != null && savedInstanceState.getBoolean(Constants.ArgsName.CONFIG_CHANGE, false);
    }

    protected abstract boolean onNavigationItemSelected(MenuItem item);

    protected abstract void onDrawerClosed(View drawerView);

    protected abstract void onDrawerOpened(View drawerView);

    protected void setContainerWeight(float weight) {
        LinearLayout.LayoutParams p = (LinearLayout.LayoutParams) container.getLayoutParams();
        p.weight = weight;
        container.setLayoutParams(p);
    }

    protected void setDetailsWeight(float weight) {
        LinearLayout.LayoutParams p = (LinearLayout.LayoutParams) containerDetails.getLayoutParams();
        p.weight = weight;
        containerDetails.setLayoutParams(p);
    }

    @Subscribe
    public abstract void onEvent(FragmentAttachedEvent fragmentAttached);


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
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
    public void setTitle(CharSequence title) {
        super.setTitle(title);
    }

    protected void postEvent(Event event) {
        EventBus.getDefault().post(event);
    }

    protected String getResString(@StringRes int id) {
        return getResources().getString(id);
    }

    protected Fragment getCurrentFragment() {
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

    public NavigationView getNavigationView() {
        return null; // navigationView;
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    @Nullable
    public ActionBar getCurrentActionBar() {
        return actionBar;
    }


    // Использовать при изменении ориентации экрана.
    public <F extends Fragment> void replaceFragment(Class<F> fragmentClass) {
        replaceFragment(fragmentClass, new FragmentBuilder(getSupportFragmentManager()));
    }

    public <F extends Fragment> void replaceFragment(Class<F> fragmentClass, FragmentBuilder builder) {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            builder.clearBackStack();
        }
        builder.replaceFragment(R.id.container, fragmentClass);
        supportInvalidateOptionsMenu();
    }

    public <F extends Fragment> void replaceFragment(F fragment) {
        replaceFragment(fragment, new FragmentBuilder(getSupportFragmentManager()));
    }

    public <F extends Fragment> void replaceFragment(F fragment, FragmentBuilder builder) {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            builder.clearBackStack();
        }
        builder.replaceFragment(R.id.container, fragment);
        supportInvalidateOptionsMenu();
    }


    public void showSnackbar(@StringRes int message) {
        GuiUtils.showSnackbar(container, message);
    }

    @Override
    public void onBackStackChanged() {
        shouldDisplayHomeUp();
    }

    public void shouldDisplayHomeUp() {
        //Enable Up button only  if there are entries in the back stack
        if (actionBar != null) {
            boolean canback = getSupportFragmentManager().getBackStackEntryCount() > 0;
            if (canback) {
                actionBar.setHomeAsUpIndicator(R.drawable.ic_action_navigation_arrow_back);
            } else {
                actionBar.setHomeAsUpIndicator(null);
                actionBarDrawerToggle.syncState();
            }
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        //This method is called when the up button is pressed. Just the pop back stack.
        getSupportFragmentManager().popBackStack();
        return true;
    }

    public DrawerLayout getDrawerLayout() {
        return drawerLayout;
    }
}
