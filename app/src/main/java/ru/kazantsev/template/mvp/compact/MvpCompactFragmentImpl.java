package ru.kazantsev.template.mvp.compact;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.arellomobile.mvp.MvpDelegate;

/**
 * Created by 0shad on 26.05.2018.
 */

public class MvpCompactFragmentImpl<F extends Fragment>  extends MvpCompactDefaultImpl<F> implements MvpCompact<F> {

    protected boolean mIsStateSaved;

    MvpCompactFragmentImpl(F view) {
        super(view);
    }

    public void onCreate(Bundle savedInstanceState) {
        getMvpDelegate().onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {

        mIsStateSaved = false;

        getMvpDelegate().onAttach();
    }

    public void onResume() {
        mIsStateSaved = false;

        getMvpDelegate().onAttach();
    }

    public void onSaveInstanceState(Bundle outState) {
        mIsStateSaved = true;

        getMvpDelegate().onSaveInstanceState(outState);
        getMvpDelegate().onDetach();
    }

    public void onDestroyView() {
        getMvpDelegate().onDetach();
        getMvpDelegate().onDestroyView();
    }

    @Override
    public void onDestroy() {
        //We leave the screen and respectively all fragments will be destroyed
        if (getView().getActivity().isFinishing()) {
            getMvpDelegate().onDestroy();
            return;
        }

        // When we rotate device isRemoving() return true for fragment placed in backstack
        // http://stackoverflow.com/questions/34649126/fragment-back-stack-and-isremoving
        if (mIsStateSaved) {
            mIsStateSaved = false;
            return;
        }

        // See https://github.com/Arello-Mobile/Moxy/issues/24
        boolean anyParentIsRemoving = false;
        Fragment parent = getView().getParentFragment();
        while (!anyParentIsRemoving && parent != null) {
            anyParentIsRemoving = parent.isRemoving();
            parent = parent.getParentFragment();
        }

        if (getView().isRemoving() || anyParentIsRemoving) {
            getMvpDelegate().onDestroy();
        }
    }

}
