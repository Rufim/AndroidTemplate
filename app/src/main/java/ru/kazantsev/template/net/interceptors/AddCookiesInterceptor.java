package ru.kazantsev.template.net.interceptors;


import android.content.Context;

import net.vrallev.android.cat.Cat;

import java.io.IOException;
import java.util.HashSet;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import ru.kazantsev.template.domain.Constants;
import ru.kazantsev.template.util.PreferenceMaster;

public class AddCookiesInterceptor implements Interceptor {

    private final Context context;

    public AddCookiesInterceptor(Context context) {
        this.context = context;
    }


    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder builder = chain.request().newBuilder();
        HashSet<String> preferences = new PreferenceMaster(context).getValue(Constants.Preferences.PREF_COOKIES, new HashSet<>());
        for (String cookie : preferences) {
            builder.addHeader("Cookie", cookie);
            Cat.v("Adding Header: " + cookie); // This is done so I know which headers are being added; this interceptor is used after the normal logging of OkHttp
        }

        return chain.proceed(builder.build());
    }
}
