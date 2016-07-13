package ru.kazantsev.template.view.watcher;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * Created by Dmitry on 27.04.2016.
 */
public abstract class TextChangedWatcher implements TextWatcher {

    public abstract void textChanged(Editable editable);

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        textChanged(s);
    }
}
