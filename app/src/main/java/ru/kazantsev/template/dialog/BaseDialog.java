package ru.kazantsev.template.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;

import org.greenrobot.eventbus.EventBus;

import ru.kazantsev.template.R;
import ru.kazantsev.template.domain.Constants;
import ru.kazantsev.template.domain.event.Event;
import ru.kazantsev.template.mvp.compact.MvpCompactDialogImpl;
import ru.kazantsev.template.mvp.compact.MvpCompactFactory;
import ru.kazantsev.template.util.FragmentBuilder;
import ru.kazantsev.template.util.GuiUtils;

/**
 * Created by Dmitry on 20.10.2015.
 */
public  class BaseDialog extends AppCompatDialogFragment implements DialogInterface.OnClickListener {

    MvpCompactDialogImpl mvpCompact = null;

    public BaseDialog() {
        if(Constants.App.USE_MOXY) {
            mvpCompact = MvpCompactFactory.buildMvpCompactDialog(this);
        }
    }

    public static <F extends BaseDialog> F newInstance(Class<F> fragmentClass, Bundle args) {
        return FragmentBuilder.newInstance(fragmentClass, args);
    }

    public static <F extends BaseDialog> F newInstance(Class<F> fragmentClass) {
        return FragmentBuilder.newInstance(fragmentClass);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(mvpCompact != null) {
            mvpCompact.onCreate(savedInstanceState);
        }
        setRetainInstance(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && GuiUtils.getThemeColor(getContext(), R.attr.colorOverlay) != 0) {
            getDialog().getWindow().setBackgroundDrawableResource(R.drawable.base_dialog_background);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
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

    @Override
    public void onResume() {
        super.onResume();
        if(mvpCompact != null) {
            mvpCompact.onResume();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mvpCompact != null) {
            mvpCompact.onDestroy();
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        int i = 0;
        switch (which) {
            case Dialog.BUTTON_POSITIVE:
                onButtonPositive(dialog);
                break;
            case Dialog.BUTTON_NEGATIVE:
                onButtonNegative(dialog);
                break;
            case Dialog.BUTTON_NEUTRAL:
                onButtonNeutral(dialog);
                break;
        }

    }

    public void onButtonPositive(DialogInterface dialog) {

    }

    public void onButtonNegative(DialogInterface dialog) {

    }

    public void onButtonNeutral(DialogInterface dialog) {

    }

    protected void postEvent(Event event) {
        EventBus.getDefault().post(event);
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
        if(mvpCompact != null) {
            mvpCompact.onDestroyView();
        }
    }

}
