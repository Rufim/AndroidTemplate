package ru.kazantsev.template.fragments;

import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v4.widget.SwipeRefreshLayout;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_error, container, false);
        errorImage = GuiUtils.getView(rootView, R.id.error_image);
        errorMessage = GuiUtils.getView(rootView, R.id.error_text);
        swipeRefresh = GuiUtils.getView(rootView, R.id.refresh);
        fragmentClass = (Class<BaseFragment>) getArguments().getSerializable(Constants.ArgsName.FRAGMENT_CLASS);
        fragmentArgs = getArguments().getParcelable(Constants.ArgsName.FRAGMENT_ARGS);
        int icon_id = getArguments().getInt(Constants.ArgsName.RESOURCE_ID, R.drawable.ic_action_report_problem);
        errorImage.setImageResource(icon_id);
        swipeRefresh.setOnRefreshListener(() -> {
            getFragmentManager().executePendingTransactions();
            new FragmentBuilder(getFragmentManager())
                    .putArgs(fragmentArgs)
                    .replaceFragment(getId(), fragmentClass);
        });
        String message = getArguments().getString(Constants.ArgsName.MESSAGE);
        if(message == null) {
            message = getString(R.string.error);
        }
        errorMessage.setText(message);
        return rootView;
    }

    @Override
    public boolean allowBackPress() {
        getFragmentManager().executePendingTransactions();
        new FragmentBuilder(getFragmentManager())
                .putArgs(fragmentArgs)
                .replaceFragment(getId(), fragmentClass);
        return false;
    }

    public static void show(BaseFragment fragment, @StringRes int message, @DrawableRes int icon_id) {
        new FragmentBuilder(fragment.getFragmentManager())
                .putArg(Constants.ArgsName.MESSAGE, fragment.getString(message))
                .putArg(Constants.ArgsName.FRAGMENT_CLASS, fragment.getClass())
                .putArg(Constants.ArgsName.FRAGMENT_ARGS, fragment.getArguments())
                .putArg(Constants.ArgsName.RESOURCE_ID, icon_id)
                .replaceFragment(fragment, ErrorFragment.class);
    }

    public static void show(BaseFragment fragment, @StringRes int message) {
        new FragmentBuilder(fragment.getFragmentManager())
                .putArg(Constants.ArgsName.MESSAGE, fragment.getString(message))
                .putArg(Constants.ArgsName.FRAGMENT_CLASS, fragment.getClass())
                .putArg(Constants.ArgsName.FRAGMENT_ARGS, fragment.getArguments())
                .replaceFragment(fragment, ErrorFragment.class);
    }

    public static void show(BaseFragment fragment) {
        new FragmentBuilder(fragment.getFragmentManager())
                .putArg(Constants.ArgsName.FRAGMENT_CLASS, fragment.getClass())
                .putArg(Constants.ArgsName.FRAGMENT_ARGS, fragment.getArguments())
                .replaceFragment(fragment, ErrorFragment.class);
    }
}
