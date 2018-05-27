package ru.kazantsev.template.fragments;

import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import ru.kazantsev.template.R;
import ru.kazantsev.template.domain.Constants;
import ru.kazantsev.template.util.FragmentBuilder;
import ru.kazantsev.template.util.GuiUtils;

/**
 * Created by 0shad on 13.07.2015.
 */
public class ErrorFragment extends BaseFragment {

    private static final String TAG = ErrorFragment.class.getSimpleName();

    ImageView errorImage;
    TextView errorMessage;
    SwipeRefreshLayout swipeRefresh;
    Class<BaseFragment> fragmentClass;
    Bundle fragmentArgs;
    Throwable exception;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_error, container, false);
        errorImage = GuiUtils.getView(rootView, R.id.error_image);
        errorMessage = GuiUtils.getView(rootView, R.id.error_text);
        swipeRefresh = GuiUtils.getView(rootView, R.id.refresh);
        fragmentClass = (Class<BaseFragment>) getArguments().getSerializable(Constants.ArgsName.FRAGMENT_CLASS);
        fragmentArgs = getArguments().getParcelable(Constants.ArgsName.FRAGMENT_ARGS);
        int icon_id = getArguments().getInt(Constants.ArgsName.RESOURCE_ID,0);
        if(icon_id == 0) {
           icon_id = GuiUtils.getThemeResource(getContext(), R.attr.iconErrorFragment);
           if(icon_id == 0) {
             icon_id = R.drawable.ic_action_report_problem;
           }
        }
        errorImage.setImageResource(icon_id);
        exception = (Throwable) getArguments().getSerializable(Constants.ArgsName.FRAGMENT_EXCEPTION);
        swipeRefresh.setOnRefreshListener(() -> {
            getFragmentManager().executePendingTransactions();
            new FragmentBuilder(getFragmentManager())
                    .putArgs(fragmentArgs)
                    .replaceFragment(getId(), fragmentClass);
        });
        String message = getArguments().getString(Constants.ArgsName.MESSAGE);
        if (message == null) {
            message = getString(R.string.error);
        }
        int color = GuiUtils.getThemeColor(getContext(), R.attr.textColorErrorFragment);
        if(color != 0) errorMessage.setTextColor(color);
        errorMessage.setText(message);
        Log.e(fragmentClass.getSimpleName(), message, exception);
        return rootView;
    }

    @Override
    public boolean allowBackPress() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.remove(this);
        transaction.commitAllowingStateLoss();
        return super.allowBackPress();
    }

    public static void show(BaseFragment fragment, @StringRes Integer message,  @DrawableRes Integer iconId, Throwable exception) {
        if (fragment != null && fragment.isAdded()) {
            FragmentManager manager;
            if (fragment.getParentFragment() == null) {
                manager = fragment.getFragmentManager();
            } else {
                manager = fragment.getParentFragment().getChildFragmentManager();
            }
            FragmentBuilder builder = new FragmentBuilder(manager)
                    .putArg(Constants.ArgsName.FRAGMENT_CLASS, fragment.getClass())
                    .putArg(Constants.ArgsName.FRAGMENT_ARGS, fragment.getArguments());
            if (message != null) builder.putArg(Constants.ArgsName.MESSAGE, fragment.getString(message));
            if (exception != null) builder.putArg(Constants.ArgsName.FRAGMENT_EXCEPTION, exception);
            if (iconId != null) builder.putArg(Constants.ArgsName.RESOURCE_ID, iconId);
            if (fragment.getParentFragment() == null) {
                builder.replaceFragment(fragment, ErrorFragment.class);
            } else {
                builder.replaceFragment(fragment, ErrorFragment.class);
            }

        }
    }


    public static void show(BaseFragment fragment, @StringRes int message, @DrawableRes int iconId) {
        show(fragment, message, iconId, null);
    }

    public static void show(BaseFragment fragment, @StringRes int message) {
        show(fragment, message, (Integer)null, null);
    }

    public static void show(BaseFragment fragment) {
        show(fragment, (Integer)null, (Integer)null, null);
    }

    public static void show(BaseFragment fragment, @StringRes int message, Throwable exception) {
        show(fragment, message, (Integer)null, exception);
    }

    public static void show(BaseFragment fragment, Throwable exception) {
        show(fragment, (Integer)null, (Integer) null, exception);
    }
}
