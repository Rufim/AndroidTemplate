package ru.kazantsev.template.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Field;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import ru.kazantsev.template.R;
import ru.kazantsev.template.activity.BaseActivity;
import ru.kazantsev.template.activity.NavigationActivity;
import ru.kazantsev.template.domain.Constants;
import ru.kazantsev.template.domain.event.Event;
import ru.kazantsev.template.domain.event.FragmentAttachedEvent;
import ru.kazantsev.template.mvp.compact.MvpCompactFragmentImpl;
import ru.kazantsev.template.mvp.compact.MvpCompactFactory;
import ru.kazantsev.template.util.AndroidSystemUtils;
import ru.kazantsev.template.util.FragmentBuilder;
import ru.kazantsev.template.util.GuiUtils;

/**
 * A placeholder fragment containing a simple view.
 */
public class BaseFragment extends Fragment implements BaseActivity.BackCallback {

    /**
     * Returns a new instance of this fragment
     */
    public static <F extends BaseFragment> F newInstance(Class<F> fragmentClass, Bundle args) {
        return FragmentBuilder.newInstance(fragmentClass, args);
    }

    public static <F extends BaseFragment> F newInstance(Class<F> fragmentClass) {
        return FragmentBuilder.newInstance(fragmentClass);
    }

    // Базовые методы
    protected static <F extends BaseFragment> F show(FragmentBuilder builder, @IdRes int container, Class<F> fragmentClass) {
        return builder.newFragment().replaceFragment(container, fragmentClass);
    }

    protected static <F extends BaseFragment> F show(FragmentManager manager, @IdRes int container, Class<F> fragmentClass, String key, Object obj) {
        return new FragmentBuilder(manager).newFragment().putArg(key, obj).replaceFragment(container, fragmentClass);
    }

    protected static <F extends BaseFragment> F show(BaseFragment fragment, Class<F> fragmentClass, String key, Object obj) {
        if (fragment.isAdded()) {
            return new FragmentBuilder(fragment.getFragmentManager()).newFragment().addToBackStack().putArg(key, obj).replaceFragment(fragment, fragmentClass);
        } else {
            return null;
        }
    }

    protected static <F extends BaseFragment> F show(BaseFragment fragment, Class<F> fragmentClass, Map<String, Object> args) {
        if (fragment.isAdded()) {
            return new FragmentBuilder(fragment.getFragmentManager()).newFragment().addToBackStack().putArgs(args).replaceFragment(fragment, fragmentClass);
        } else {
            return null;
        }
    }

    protected static <F extends BaseFragment> F show(BaseFragment fragment, Class<F> fragmentClass) {
        if (fragment.isAdded()) {
            return new FragmentBuilder(fragment.getFragmentManager()).newFragment().addToBackStack().replaceFragment(fragment, fragmentClass);
        } else {
            return null;
        }
    }

    // Подобными методами должны вызыватся наследуемые фрагмент(их нужно реализовывать для кажного фрагмента заново)
    protected static BaseFragment show(FragmentBuilder builder, @IdRes int container) {
        return show(builder, container, BaseFragment.class);
    }

    public static BaseFragment show(FragmentManager manager, @IdRes int container, String message) {
        return show(manager, container, BaseFragment.class, Constants.ArgsName.MESSAGE, message);
    }

    public static BaseFragment show(FragmentBuilder builder, @IdRes int container, String message) {
        return show(builder.putArg(Constants.ArgsName.MESSAGE, message), container, BaseFragment.class);
    }

    public static BaseFragment show(BaseFragment fragment, String message) {
        return show(fragment, BaseFragment.class, Constants.ArgsName.MESSAGE, message);
    }

    protected boolean retainInstance = true;
    private Unbinder unbinder;
    private MvpCompactFragmentImpl mvpCompact = null;


    public BaseFragment() {
        if(Constants.App.USE_MOXY) {
              mvpCompact = MvpCompactFactory.buildMvpCompactFragment(this);
        }
    }

    /**
     * This method will only be called once when the retained
     * Fragment is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retain this fragment across configuration changes.
        setRetainInstance(retainInstance);
        if(mvpCompact != null) {
            mvpCompact.onCreate(savedInstanceState);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_base, container, false);
        bind(rootView);
        String message = getArguments().getString(Constants.ArgsName.MESSAGE, "В разработке...");
        GuiUtils.setText(rootView.findViewById(R.id.test_message), message);
        return rootView;
    }

    public BaseFragment show(FragmentManager manager, @IdRes int container, String key, Object obj) {
        return show(manager, container, this.getClass(), key, obj);
    }

    public int getContainerId() {
        return ((ViewGroup) getView().getParent()).getId();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        postEvent(new FragmentAttachedEvent(this));
    }

    public void setTitle(@StringRes int title) {
        if(title > 0) {
            getBaseActivity().setTitle(title);
        }
    }

    public void setTitle(String title) {
        if(title != null) {
            getBaseActivity().setTitle(title);
        }
    }

    public void setSelectNavBar(@IdRes int selectNavBar) {
        if(selectNavBar > 0) {
            if(getBaseActivity() instanceof NavigationActivity) {
                ((NavigationActivity) getBaseActivity()).selectItem(selectNavBar);
            } else {
                getBaseActivity().getNavigationView().setCheckedItem(selectNavBar);
            }
        }
    }
    public void setTitleAndSelectNavBar(@StringRes int title, @IdRes int selectNavBar) {
        setTitle(title);
        setSelectNavBar(selectNavBar);
    }

    public void restoreCachedBundle(String tag) {
        Bundle bundle = getBaseActivity().getCachedBoundle(tag);
        if(bundle != null) {
            if(getArguments() != null) {
                if(!getArguments().equals(bundle)) {
                    getArguments().putAll(bundle);
                }
            } else {
                setArguments(bundle);
            }
        }
    }

    public void safeInvalidateOptionsMenu() {
        if(isAdded()) {
            getActivity().invalidateOptionsMenu();
        }
    }

    public MenuItem safeGetMenuItem(@IdRes int id) {
        if (getBaseActivity() != null && getBaseActivity().getToolbar() != null) {
            return getBaseActivity().getToolbar().getMenu().findItem(id);
        }
        return null;
    }

    public void safeCheckMenuItem(@IdRes int id, boolean state) {
        MenuItem item = safeGetMenuItem(id);
        if (item != null) {
            item.setChecked(state);
        }

    }

    public void safeEnableMenuItem(@IdRes int id, boolean state) {
        MenuItem item = safeGetMenuItem(id);
        if (item != null) {
            item.setEnabled(state);
        }
    }

    public void restoreCachedBundle() {
        restoreCachedBundle(getTag());
    }

    public boolean allowBackPress() {
       return true;
    }

    protected void postEvent(Event event) {
        EventBus.getDefault().post(event);
    }

    public void bind(View view) {
        unbinder =  ButterKnife.bind(this, view);
    }

    public void unbind() {
        if(unbinder != null) {
            unbinder.unbind();
        }
    }

    public FragmentBuilder newFragmentBuilder() {
        return new FragmentBuilder(getFragmentManager());
    }

    protected BaseActivity getBaseActivity() {
        return (BaseActivity) getActivity();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(mvpCompact != null) {
            mvpCompact.onDestroyView();
        }
        unbind();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if(mvpCompact != null) {
            mvpCompact.onDestroy();
        }
        // Android support bug https://code.google.com/p/android/issues/detail?id=42601
        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T getArg(String key, T defValue) {
        Bundle bundle = getArguments();
        if(bundle == null) {
            return defValue;
        }
        T result =  AndroidSystemUtils.getFromBundle(bundle, key, defValue);
        if(result == null) {
            result = defValue;
        }
        return result;
    }

    public boolean containsArg(String key) {
        Bundle bundle = getArguments();
        if(bundle == null) {
            return false;
        }
        return bundle.containsKey(key);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(mvpCompact != null) {
            mvpCompact.onStart();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mvpCompact != null) {
            mvpCompact.onResume();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mvpCompact != null) {
            mvpCompact.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mvpCompact != null) {
            mvpCompact.onStop();
        }
    }

}
