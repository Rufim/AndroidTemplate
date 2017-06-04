package ru.kazantsev.template.util;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Browser;
import android.text.TextPaint;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;

/**
 * Created by 0shad on 31.10.2015.
 */
@SuppressLint("ParcelCreator")
public class URLSpanNoUnderline extends URLSpan {

    public URLSpanNoUnderline(String url) {
        super(url);
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.setUnderlineText(false);
    }

    @Override
    public void onClick(View widget) {
        String stringUrl = getURL();
        if(stringUrl != null) {
            Context context = widget.getContext();
            Intent intent;
            if (stringUrl.contains("@")) {
                intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + stringUrl));
            } else {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(stringUrl));
                intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.getPackageName());
            }
            try {
                context.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Log.w("URLSpan", "Actvity was not found for intent, " + intent.toString());
            }
        }
    }
}
