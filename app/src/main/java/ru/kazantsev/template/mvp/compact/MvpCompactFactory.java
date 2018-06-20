package ru.kazantsev.template.mvp.compact;



import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialogFragment;

/**
 * Created by 0shad on 26.05.2018.
 */

public class MvpCompactFactory {


    public static <F extends Fragment> MvpCompactFragmentImpl<F> buildMvpCompactFragment(F fragment) {
        return new MvpCompactFragmentImpl<>(fragment);
    }

    public static <A extends AppCompatActivity> MvpCompactActivityImpl<A> buildMvpCompactActivity(A activity) {
        return new MvpCompactActivityImpl<>(activity);
    }

    public static <D extends AppCompatDialogFragment> MvpCompactDialogImpl<D> buildMvpCompactDialog(D dialog) {
        return new MvpCompactDialogImpl<>(dialog);
    }

}
