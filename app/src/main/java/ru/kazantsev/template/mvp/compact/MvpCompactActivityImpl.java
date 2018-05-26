package ru.kazantsev.template.mvp.compact;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by 0shad on 26.05.2018.
 */

public class MvpCompactActivityImpl<A extends AppCompatActivity> extends MvpCompactDefaultImpl<A> implements  MvpCompact<A> {


     MvpCompactActivityImpl(A view) {
        super(view);
    }

    @Override
    public void onStart() {
        getMvpDelegate().onAttach();
    }

    @Override
    public void onResume() {
        getMvpDelegate().onAttach();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        getMvpDelegate().onSaveInstanceState(outState);
        getMvpDelegate().onDetach();
    }


    @Override
    public void onDestroy() {
        getMvpDelegate().onDestroyView();

        if (getView().isFinishing()) {
            getMvpDelegate().onDestroy();
        }
    }

}
