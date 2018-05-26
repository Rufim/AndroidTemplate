package ru.kazantsev.template.mvp.compact;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.arellomobile.mvp.MvpAppCompatActivity;
import com.arellomobile.mvp.MvpDelegate;

/**
 * Created by 0shad on 26.05.2018.
 */

public interface MvpCompact<V>  {

    void onCreate(Bundle savedInstanceState);

    void onStart();

    void onResume();

    void onSaveInstanceState(Bundle outState);

    void onStop();

    void onDestroy();

    MvpDelegate<V> getMvpDelegate();
}
