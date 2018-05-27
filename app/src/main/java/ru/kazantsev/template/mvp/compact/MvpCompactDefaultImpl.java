package ru.kazantsev.template.mvp.compact;

import android.os.Bundle;

import com.arellomobile.mvp.MvpDelegate;

/**
 * Created by 0shad on 26.05.2018.
 */

public abstract class MvpCompactDefaultImpl<V> implements MvpCompact<V> {

    private MvpDelegate<V> mMvpDelegate;
    private final V view;


    MvpCompactDefaultImpl(V view) {
        this.view = view;
    }


    public void onCreate(Bundle savedInstanceState) {
        getMvpDelegate().onCreate(savedInstanceState);
    }


    @Override
    public void onStop() {
        getMvpDelegate().onDetach();
    }

    /**
     * @return The {@link MvpDelegate} being used by this Fragment.
     */
    public MvpDelegate<V> getMvpDelegate() {
        if (mMvpDelegate == null) {
            mMvpDelegate = new MvpDelegate<>(view);
        }

        return mMvpDelegate;
    }

    public V getView() {
        return view;
    }
}
