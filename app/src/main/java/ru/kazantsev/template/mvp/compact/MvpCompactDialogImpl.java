package ru.kazantsev.template.mvp.compact;

import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;

/**
 * Created by 0shad on 26.05.2018.
 */

public class MvpCompactDialogImpl<D extends AppCompatDialogFragment> extends MvpCompactFragmentImpl<D> implements MvpCompact<D>  {


     MvpCompactDialogImpl(D view) {
        super(view);
    }

    @Override
    public void onStart() {}

}
