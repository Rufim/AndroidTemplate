package ru.kazantsev.template.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.StringRes;

import java.util.Iterator;
import java.util.Set;

public class PreferenceMaster {

    final Context context;
    final SharedPreferences preferences;
    final SharedPreferences.Editor editor;

    public PreferenceMaster(Context context) {
        this(context, true);
    }

    public PreferenceMaster(Context context, boolean commitImmediate) {
        this.context = context;
        this.preferences = AndroidSystemUtils.getDefaultPreference(context);
        if(!commitImmediate) {
            this.editor = preferences.edit();
        } else {
            this.editor = null;
        }
    }


    public PreferenceMaster putValue(@StringRes int keyId, Object value) {
        putValue(context.getString(keyId), value);
        return this;
    }

    public PreferenceMaster putValue(String key, Object value) {
        SharedPreferences.Editor editor;
        if (this.editor == null) {
            editor = preferences.edit();
        } else {
            editor = this.editor;
        }
        ClassType type = ClassType.cast(value);
        switch (type) {
            case STRING:
                editor.putString(key, (String) value);
                break;
            case BOOLEAN:
                editor.putBoolean(key, (Boolean) value);
                break;
            case INTEGER:
                editor.putInt(key, (Integer) value);
                break;
            case LONG:
                editor.putLong(key, (Long) value);
                break;
            case FLOAT:
                editor.putFloat(key, (Float) value);
                break;
            case CHARSEQUENCE:
                editor.putString(key, value.toString());
                break;
            case SET:
                Set set = (Set) value;
                if(!set.isEmpty()) {
                    for (Object next : set) {
                        if (next != null && next.getClass() != String.class) {
                            throw new IllegalArgumentException("Supported only Set of Strings!");
                        }
                    }
                }
                editor.putStringSet(key, (Set<String>) set);
                break;
            default:
                throw new IllegalArgumentException("Unsupported type " + value.getClass().getSimpleName());
        }
        if(this.editor == null) {
            editor.commit();
        }
        return this;
    }


    public <C> C getValue(String key) {
        return AndroidSystemUtils.getPreference(preferences, key);
    }

    public <C> C getValue(@StringRes int keyId) {
        return getValue(context.getString(keyId));
    }

    public <C> C getValue(String key, C defValue) {
        return AndroidSystemUtils.getPreference(preferences, key, defValue);
    }

    public <C> C getValue(@StringRes int keyId, C defValue) {
        return getValue(context.getString(keyId), defValue);
    }

    public void commit() {
        if(editor != null) {
            editor.commit();
        }
    }

    public void applay(){
        if(editor != null) {
            editor.apply();
        }
    }
}
