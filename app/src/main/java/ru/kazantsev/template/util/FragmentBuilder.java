package ru.kazantsev.template.util;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import ru.kazantsev.template.fragments.ErrorFragment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by 0shad on 13.07.2015.
 */
public class FragmentBuilder {

    private static final String TAG = FragmentBuilder.class.getSimpleName();

    private static final int VIEW_ID_TAG = 101;


    private FragmentManager manager;
    private Bundle bundle = new Bundle();
    private Map<String, Object> args = new HashMap<>();
    private boolean toBackStack = false;
    private boolean newFragment = false;
    private boolean removeIfExists = false;
    private boolean clearBackStack = false;
    private int inAnimationId = -1;
    private int outAnimationId = -1;
    private int inPopupAnimationId = -1;
    private int outPopupAnimationId = -1;
    private String clearBackStackUpToName = null;
    private Fragment fragmentInvoker;

    public FragmentBuilder(FragmentManager manager) {
        this.manager = manager;
    }


    public static <F extends Fragment> F newInstance(Class<F> fragmentClass, Bundle args) {
        F fragment = newInstance(fragmentClass);
        fragment.setArguments(args);
        return fragment;
    }

    public static <F extends Fragment> F newInstance(Class<F> fragmentClass) {
        Fragment fragment;
        try {
            fragment = fragmentClass.newInstance();
        } catch (Exception e) {
            Log.e(TAG, "Fragment don't have default constructor", e);
            return (F) new ErrorFragment();
        }
        return (F) fragment;
    }

    private ClassType getArrayType(Object[] array) {
        if(array.length == 0) {
            return ClassType.cast(array.getClass().getComponentType());
        } else {
            return ClassType.cast(array[0].getClass());
        }
    }

    public FragmentBuilder putArg(String key, Object value) {
        ClassType type = ClassType.cast(value);
        ClassType baseType = type;
        boolean arrayFlag = false;
        boolean listFlag = false;
        if (type == ClassType.ARRAY) {
            arrayFlag = true;
            type = getArrayType((Object[]) value);
        }
        if (type == ClassType.ARRAYLIST) {
            listFlag = true;
            ArrayList list = (ArrayList) value;
            Object[] array = list.toArray();
            type = getArrayType(list.toArray());
            if (type != ClassType.STRING && type != ClassType.CHARSEQUENCE && type != ClassType.PARCELABLE) {
                type = ClassType.UNSUPPORTED;
            }
        }
        switch (type) {
            case PARCELABLE:
                if (arrayFlag) bundle.putParcelableArray(key, (Parcelable[]) value);
                else if (listFlag) bundle.putParcelableArrayList(key, (ArrayList<? extends Parcelable>) value);
                else bundle.putParcelable(key, (Parcelable) value);
                return this;
            case CHARSEQUENCE:
                if (arrayFlag) bundle.putCharSequenceArray(key, (CharSequence[]) value);
                else if (listFlag) bundle.putCharSequenceArrayList(key, (ArrayList<CharSequence>) value);
                else bundle.putCharSequence(key, (CharSequence) value);
                return this;
            case BUNDLE:
                if (arrayFlag) break;
                bundle.putBundle(key, (Bundle) value);
                return this;
            case STRING:
                if (arrayFlag) bundle.putStringArray(key, (String[]) value);
                else if (listFlag) bundle.putStringArrayList(key, (ArrayList<String>) value);
                else bundle.putString(key, (String) value);
                return this;
            case CHAR:
                if (arrayFlag) bundle.putCharArray(key, (char[]) value);
                else bundle.putChar(key, (char) value);
                return this;
            case BYTE:
                if (arrayFlag) bundle.putByteArray(key, (byte[]) value);
                else bundle.putByte(key, (byte) value);
                return this;
            case BOOLEAN:
                if (arrayFlag) bundle.putBooleanArray(key, (boolean[]) value);
                else bundle.putBoolean(key, (boolean) value);
                return this;
            case SHORT:
                if (arrayFlag) bundle.putShortArray(key, (short[]) value);
                else bundle.putShort(key, (short) value);
                return this;
            case INTEGER:
                if (arrayFlag) bundle.putIntArray(key, (int[]) value);
                else bundle.putInt(key, (int) value);
                return this;
            case LONG:
                if (arrayFlag) bundle.putLongArray(key, (long[]) value);
                else bundle.putLong(key, (long) value);
                return this;
            case FLOAT:
                if (arrayFlag) bundle.putFloatArray(key, (float[]) value);
                else bundle.putFloat(key, (float) value);
                return this;
            case DOUBLE:
                if (arrayFlag) bundle.putDoubleArray(key, (double[]) value);
                else bundle.putDouble(key, (double) value);
                return this;
            case SERIALIZABLE: case ENUM:
                bundle.putSerializable(key, (Serializable) value);
                return this;
            default:
                throw new IllegalArgumentException("Unsupported type " + value.getClass().getSimpleName());
        }
        return this;
    }

    public FragmentBuilder putArgs(Map<String, Object> args) {
        for (Map.Entry<String, Object> entry :  args.entrySet()) {
            putArg(entry.getKey(), entry.getValue());
        }
        return this;
    }

    public FragmentBuilder putArgs(Bundle args) {
        bundle.putAll(args);
        return this;
    }

    public FragmentBuilder fragmentInvoker(Fragment fragmentInvoker) {
        this.fragmentInvoker = fragmentInvoker;
        return this;
    }

    public FragmentBuilder addToBackStack() {
        this.toBackStack = true;
        return this;
    }

    public FragmentBuilder newFragment() {
        this.newFragment = true;
        return this;
    }

    public FragmentBuilder removeIfExists() {
        this.removeIfExists = true;
        return this;
    }

    public FragmentBuilder clearBackStack() {
        this.clearBackStack = true;
        return this;
    }

    public FragmentBuilder clearBackStack(Class<? extends Fragment> fragmentClass) {
        return clearBackStack(fragmentClass.getSimpleName());
    }

    public FragmentBuilder clearBackStack(String name) {
        this.clearBackStackUpToName = name;
        this.clearBackStack = true;
        return this;
    }

    public FragmentBuilder setAnimation(int inAnimationId, int outAnimationId) {
        this.inAnimationId = inAnimationId;
        this.outAnimationId = outAnimationId;
        return this;
    }

    public FragmentBuilder setPopupAnimation(int inPopupAnimationId, int outPopupAnimationId) {
        this.inPopupAnimationId = inPopupAnimationId;
        this.outPopupAnimationId = outPopupAnimationId;
        return this;
    }

    public <F extends Fragment> F replaceFragment(@IdRes int container, Class<F> fragmentClass) {
        String tag = fragmentClass.getSimpleName();
        return replaceFragment(container, fragmentClass, tag);
    }

    public <F extends Fragment> F replaceFragment(@IdRes int container, Class<F> fragmentClass, String name) {
        if (fragmentClass == null && name == null) {
            return null;
        }
        Fragment fragment = manager.findFragmentByTag(name);
        if (fragment == null || newFragment) {
            if (fragmentClass == null) {
                try {
                    fragmentClass = (Class<F>) Class.forName(name);
                } catch (ClassNotFoundException e) {
                    return null;
                }
            }
            fragment = newInstance(fragmentClass);
        }
        if(fragment != null) {
            return replaceFragment(container, fragment, name);
        } else {
            Log.e(TAG, "Cannot instantinate fragment!!! ");
            return null;
        }
    }


    public <F extends Fragment> F replaceFragment(@IdRes int container, Fragment fragment, String name) {
        clearBackStackUpToName();
        FragmentTransaction transaction = manager.beginTransaction();
        applyAnimation(transaction);
        applyBundle(fragment);
        removeIfExists(container, fragment, name, transaction);
        toBackStack(transaction);
        transaction.commitAllowingStateLoss();
        return (F) fragment;
    }

    private void clearBackStackUpToName() {
        if(clearBackStack) {
            manager.popBackStack(clearBackStackUpToName, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    private void applyAnimation(FragmentTransaction transaction) {
        if (inAnimationId > 0 && outAnimationId > 0) {
            if(inPopupAnimationId > 0 && outPopupAnimationId > 0) {
                transaction.setCustomAnimations(inAnimationId, outAnimationId, inPopupAnimationId, outPopupAnimationId);
            } else {
                transaction.setCustomAnimations(inAnimationId, outAnimationId);
            }
        }
    }

    private void applyBundle(Fragment fragment) {
        if(fragment.getArguments() != null) {
            fragment.getArguments().putAll(bundle);
        } else {
            fragment.setArguments(bundle);
        }
    }

    private void removeIfExists (@IdRes int container, Fragment fragment, String name, FragmentTransaction transaction) {
        if (manager.findFragmentById(container) == fragment && removeIfExists) {
            transaction.remove(fragment);
            transaction.add(container, fragment, name);
        } else {
            transaction.replace(container, fragment, name);
        }
    }

    private void toBackStack(FragmentTransaction transaction) {
        if (toBackStack) {
            transaction.addToBackStack(null);
        }
    }


    public <F extends Fragment> F refresh(F fragment) {
        FragmentTransaction transaction = manager.beginTransaction();
        if(fragment.getArguments() != null) {
            fragment.getArguments().putAll(bundle);
        } else {
            fragment.setArguments(bundle);
        }
        transaction.detach(fragment);
        transaction.attach(fragment);
        transaction.commitAllowingStateLoss();
        return fragment;
    }

    public <F extends Fragment> F replaceFragment(@IdRes int container, Fragment fragment) {
        return replaceFragment(container, fragment, fragment.getClass().getSimpleName());
    }

    public <F extends Fragment> F replaceFragment(Fragment fragment, Class<F> fragmentClass) {
        return replaceFragment(fragment.getId(), fragmentClass);
    }

    public <F extends Fragment> F replaceFragment(Fragment fragment, Fragment newFragment) {
        return replaceFragment(fragment.getId(), newFragment);
    }

    //TODO: need test
    public <F extends Fragment> F replaceFragment(View placeHolder, Class<F> fragmentClass) {
        if (placeHolder.getId() == View.NO_ID || placeHolder.getTag(VIEW_ID_TAG) != null) {
            int newId = placeHolder.hashCode();
            placeHolder.setTag(VIEW_ID_TAG, newId);
            placeHolder.setId(newId);
        }
        return replaceFragment(placeHolder.getId(), fragmentClass);
    }

    public <F extends Fragment> F newFragment(Class<F> fragmentClass) {
        return newInstance(fragmentClass, bundle);
    }
}
