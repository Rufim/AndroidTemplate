package ru.kazantsev.template.net.interceptors;

import android.content.Context;

import java.io.IOException;
import java.util.HashSet;


import okhttp3.Interceptor;
import okhttp3.Response;
import ru.kazantsev.template.domain.Constants;
import ru.kazantsev.template.util.PreferenceMaster;

/**
 * This Interceptor add all received Cookies to the app DefaultPreferences.
 * Your implementation on how to save the Cookies on the Preferences MAY VARY.
 * <p>
 * Created by tsuharesu on 4/1/15.
 */
public class ReceivedCookiesInterceptor implements Interceptor {

    private final Context context;

    public ReceivedCookiesInterceptor(Context context) {
        this.context = context;
    }


    @Override
    public Response intercept(Chain chain) throws IOException {
        Response originalResponse = chain.proceed(chain.request());

        if (!originalResponse.headers("Set-Cookie").isEmpty()) {
            HashSet<String> cookies = new HashSet<>();

            for (String header : originalResponse.headers("Set-Cookie")) {
                cookies.add(header);
            }
            new PreferenceMaster(context).putValue(Constants.Preferences.PREF_COOKIES, cookies);
        }

        return originalResponse;
    }
}
