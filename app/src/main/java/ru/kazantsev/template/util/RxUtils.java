package ru.kazantsev.template.util;


import io.reactivex.Maybe;
import io.reactivex.MaybeTransformer;
import io.reactivex.ObservableTransformer;
import io.reactivex.SingleTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class RxUtils {
   public static <T> ObservableTransformer<T, T> applySchedulers() {
       return observable -> observable.subscribeOn(Schedulers.io())
               .observeOn(AndroidSchedulers.mainThread());
   }

    public static <T> SingleTransformer<T, T> applySchedulersSingle() {
        return observable -> observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static <T> MaybeTransformer<T, T> applySchedulersMaybe() {
        return observable -> observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
