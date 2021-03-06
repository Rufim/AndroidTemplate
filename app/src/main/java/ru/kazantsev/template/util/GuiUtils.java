package ru.kazantsev.template.util;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.ArrayRes;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorRes;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.widget.RecyclerView;
import android.text.ParcelableSpan;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.annimon.stream.Stream;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.R.attr.textColor;

/**
 * Created with IntelliJ IDEA.
 * User: Rufim
 * Date: 16.11.13
 * Time: 15:21
 * To change this template use File | Settings | File Templates.
 */
public class GuiUtils {

    private static String thousandSeparator;
    private static String decimalSeparator;


    public static void setThousandSeparator(String thousandSeparator) {
        GuiUtils.thousandSeparator = thousandSeparator;
    }

    public static void setDecimalSeparator(String decimalSeparator) {
        GuiUtils.decimalSeparator = decimalSeparator;
    }


    public static class EmptyTextException extends Exception {
        static final String MESSAGE = "TextView do not contains string";

        @Override
        public String getMessage() {
            return MESSAGE;
        }
    }

    private static final String TAG = SystemUtils.getClassName();

    public static <A extends Activity> void startActivity(Context context, Class<A> aClass) {
        Intent intent = new Intent(context, aClass);
        context.startActivity(intent);
    }

    public static <A extends Activity, V extends Serializable> void startActivity(Context context, Class<A> aClass, HashMap<String, V> putValues) {
        Intent intent = new Intent(context, aClass);
        for (Map.Entry<String, V> entry : putValues.entrySet()) {
            intent.putExtra(entry.getKey(), entry.getValue());
        }
        context.startActivity(intent);
    }

    public static <A extends Activity, V extends Serializable> void startActivity(Context context, Class<A> aClass, String key, V value) {
        Intent intent = new Intent(context, aClass);
        intent.putExtra(key, value);
        context.startActivity(intent);
    }

    public static void setImageToImageView(Activity activity, int resid, ImageView view) {
        int width = view.getWidth();
        int height = view.getHeight();
        view.setImageBitmap(GuiUtils.decodeSampleImage(activity, resid, width, height));
    }

    public static Bitmap decodeSampleImage(Context context, int resid, int width, int height) {
        try {
            System.gc(); // First of all free some memory

            // Decode image size

            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(context.getResources(), resid, o);

            // The new size we want to scale to

            final int requiredWidth = width;
            final int requiredHeight = height;

            // Find the scale value (as a power of 2)

            int sampleScaleSize = 1;

            while (o.outWidth / sampleScaleSize / 2 >= requiredWidth && o.outHeight / sampleScaleSize / 2 >= requiredHeight)
                sampleScaleSize *= 2;

            // Decode with inSampleSize

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = sampleScaleSize;

            return BitmapFactory.decodeResource(context.getResources(), resid, o2);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage()); // We don't want the application to just throw an exception
        }

        return null;
    }

    public static Bitmap decodeSampleImage(File f, int width, int height) {
        try {
            System.gc(); // First of all free some memory

            // Decode image size

            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);

            // The new size we want to scale to

            final int requiredWidth = width;
            final int requiredHeight = height;

            // Find the scale value (as a power of 2)

            int sampleScaleSize = 1;

            while (o.outWidth / sampleScaleSize / 2 >= requiredWidth && o.outHeight / sampleScaleSize / 2 >= requiredHeight)
                sampleScaleSize *= 2;

            // Decode with inSampleSize

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = sampleScaleSize;

            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage(), e); // We don't want the application to just throw an exception
        }

        return null;
    }

    public static Bitmap combineImages(Bitmap left, Bitmap right) { // can add a 3rd parameter 'String loc' if you want to save the new image - left some code to do that at the bottom

        System.gc();

        Bitmap cs = null;

        int width, height = 0;

        width = left.getWidth() + right.getWidth();

        if (left.getHeight() > right.getHeight()) {
            height = left.getHeight();
        } else {
            height = right.getHeight();
        }

        cs = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

        Canvas comboImage = new Canvas(cs);
        comboImage.drawBitmap(left, 0f, 0f, null);
        comboImage.drawBitmap(right, left.getWidth(), 0f, null);
        return cs;
    }

    public static Bitmap getSafeBitmap(String path, int imageMaxSize) {
        try {
            return getSafeBitmap(new FileInputStream(path), imageMaxSize);
        } catch (IOException e) {
            Log.d(TAG, e.getMessage() + " image path = " + path, e);
            return null;
        }
    }

    public static Bitmap getSafeBitmap(InputStream is, int imageMaxSize) throws IOException {
        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BufferedInputStream inputStream = new BufferedInputStream(is);
        inputStream.markSupported();
        inputStream.mark(inputStream.available());
        BitmapFactory.decodeStream(inputStream, null, o);
        inputStream.reset();


        int scale = 1;
        while ((o.outWidth * o.outHeight) * (1 / Math.pow(scale, 2)) >
                imageMaxSize) {
            scale++;
        }

        Bitmap b = null;
        if (scale > 1) {
            scale--;
            // scale to max possible inSampleSize that still yields an image
            // larger than target
            BitmapFactory.Options bfOptions = new BitmapFactory.Options();
            bfOptions.inDither = false;                     //Disable Dithering mode
            bfOptions.inPurgeable = true;                   //Tell to gc that whether it needs free memory, the Bitmap can be cleared
            bfOptions.inInputShareable = true;              //Which kind of reference will be used to recover the Bitmap data after being clear, when it will be used in the future
            bfOptions.inTempStorage = new byte[32 * 1024];
            bfOptions.inSampleSize = scale;
            bfOptions.inPreferredConfig = Bitmap.Config.RGB_565;
            b = BitmapFactory.decodeStream(inputStream, null, bfOptions);

            if (b == null) {
                throw new IOException("Cannot decode stream!");
            }

            // resize to desired dimensions
            int height = b.getHeight();
            int width = b.getWidth();

            double y = Math.sqrt(imageMaxSize
                    / (((double) width) / height));
            double x = (y / height) * width;

            Bitmap scaledBitmap = Bitmap.createScaledBitmap(b, (int) x,
                    (int) y, true);
            b = scaledBitmap;

            System.gc();
        } else {
            b = BitmapFactory.decodeStream(inputStream);
            if (b == null) {
                throw new IOException("Cannot decode stream!");
            }
        }
        inputStream.close();

        Log.d(TAG, "bitmap size - width: " + b.getWidth() + ", height: " +
                b.getHeight());
        return b;
    }

    public static Bitmap getResizedBitmap(String path, int reqWidth,
                                          int reqHeight) {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap bmp = BitmapFactory.decodeFile(path, options);
        return bmp;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {

        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
        }
        return inSampleSize;
    }

    public static <V extends View> V inflate(ViewGroup group, @LayoutRes int id) {
        return (V) LayoutInflater.from(group.getContext()).inflate(id, group, false);
    }

    public static <V extends View> V inflate(Context context, @LayoutRes int id) {
        return (V) LayoutInflater.from(context).inflate(id, null);
    }

    public static <L extends ViewGroup> L getLayout(Context context, Integer id) {
        return (L) ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(id, null, false);
    }

    public static <V extends View> V getView(Activity activity, Integer id) {
        return (V) activity.findViewById(id);
    }

    public static <V extends View> V getView(View view, Integer id) {
        return (V) view.findViewById(id);
    }

    public static ArrayList<View> getAllChildren(View v) {
        return getAllChildren(v, null);
    }

    public static <V> ArrayList<V> getAllChildren(View v, Class<V> viewClass) {
        if (v == null) {
            return new ArrayList<V>();
        }

        if (!(v instanceof ViewGroup)) {
            ArrayList<V> viewArrayList = new ArrayList<V>(1);
            if(viewClass == null || v.getClass() == viewClass) {
                viewArrayList.add((V) v);
            }
            return viewArrayList;
        }

        ArrayList<V> result = new ArrayList<V>();

        ViewGroup viewGroup = (ViewGroup) v;
        if(viewClass == null || viewGroup.getClass() == viewClass) {
            result.add((V) viewGroup);
        }
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if(child instanceof ViewGroup) {
                result.addAll(getAllChildren(child, viewClass));
            } else {
                if(viewClass == null || child.getClass() == viewClass) {
                    result.add((V) child);
                }
            }

        }
        return result;
    }

    public static int dpToPx(int dp, Context context) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    public static int pxToDp(int px, Context context) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) px / density);
    }


    public static boolean isScreenHaveDpWidth(Context context, int dpWidth) {
        return getScreenSize(context).x >= dpWidth;
    }

    public static PointF getScreenSizeDp(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        return new PointF(dpWidth, dpHeight);
    }

    public static boolean isLandscape(Context context) {
        int orientation = getScreenOrientation(context);
        return orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE ||
                orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
    }

    public static int getScreenOrientation(Context context) {
        WindowManager wm = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE));
        int rotation = wm.getDefaultDisplay().getRotation();
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        int orientation;
        // if the device's natural orientation is portrait:
        if ((rotation == Surface.ROTATION_0
                || rotation == Surface.ROTATION_180) && height > width ||
                (rotation == Surface.ROTATION_90
                        || rotation == Surface.ROTATION_270) && width > height) {
            switch (rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_180:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                case Surface.ROTATION_270:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                default:
                    Log.e(TAG, "Unknown screen orientation. Defaulting to " +
                            "portrait.");
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
            }
        }
        // if the device's natural orientation is landscape or if the device
        // is square:
        else {
            switch (rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_180:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                case Surface.ROTATION_270:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                default:
                    Log.e(TAG, "Unknown screen orientation. Defaulting to " +
                            "landscape.");
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
            }
        }

        return orientation;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }


    public static ViewGroup getParent(View view) {
        return (ViewGroup) view.getParent();
    }

    public static void removeView(View view) {
        if (view == null) {
            return;
        }
        ViewGroup parent = getParent(view);
        if (parent != null) {
            parent.removeView(view);
        }
    }

    public static void replaceView(View currentView, View newView) {
        if (currentView == null) {
            return;
        }
        ViewGroup parent = getParent(currentView);
        if (parent == null) {
            return;
        }
        final int index = parent.indexOfChild(currentView);
        removeView(currentView);
        removeView(newView);
        parent.addView(newView, index);
        parent.invalidate();
    }

    public static Point calculateTextWith(CharSequence text, TextView textView) {
        Rect bounds = new Rect();
        Paint textPaint = textView.getPaint();
        textPaint.getTextBounds(text.toString(), 0, text.length(), bounds);
        int height = bounds.height();
        int width = bounds.width();
        return new Point(width, height);
    }

    public static Point getScreenSize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    public static void fadeOut(View view, int delay, int duration) {
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
        fadeOut.setStartOffset(delay);
        fadeOut.setDuration(duration);
        view.setAnimation(fadeOut);
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                view.setVisibility(View.INVISIBLE);
            }
        }, delay);
    }

    public static void fadeIn(View view, int delay, int duration) {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
        fadeIn.setStartOffset(delay);
        fadeIn.setDuration(duration);
        view.setAnimation(fadeIn);
        view.setVisibility(View.VISIBLE);
    }

    public static int animatedHide(View view, boolean vertical) {
        Animation animation;
        int size = 0;
        if (vertical) {
            size = view.getHeight();
            animation = new ResizeAnimation(view, 0, -view.getHeight());
        } else {
            size = view.getWidth();
            animation = new ResizeAnimation(view, -view.getWidth(), 0);
        }
        view.setAnimation(animation);
        animation.start();
        if (view.getParent() != null) {
            ((View) view.getParent()).invalidate();
        } else {
            view.invalidate();
        }
        return size;
    }

    public static void animatedResize(View view, int deltaWidth, int deltaHeight) {
        Animation animation;
        animation = new ResizeAnimation(view, deltaWidth, deltaHeight);
        view.setAnimation(animation);
        animation.start();
        ((View) view.getParent()).invalidate();
    }

    public static void slide(View target, int px, boolean vertical) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(target, "Translation" + (vertical ? "Y" : "X"), px);
        objectAnimator.start();
    }

    public static void toast(Context context, String message) {
        toast(context, message, true);
    }

    public static void toast(Context context, String message, boolean shortDuration) {
        Toast toast = Toast.makeText(context, message, shortDuration ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static void toast(Context context, @StringRes int messageId) {
        toast(context, messageId, true);
    }

    public static void toast(Context context, @StringRes int messageId, boolean shortDuration) {
        Toast toast = Toast.makeText(context, messageId, shortDuration ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static void showSnackbar(View viewContainer, CharSequence message, int textColor, int backgroundColor) {
        makeCustomSnackbar(viewContainer, message, textColor, backgroundColor).show();
    }

    public static Snackbar makeCustomSnackbar(View viewContainer, CharSequence message, int textColor, int backgroundColor) {
        Snackbar snackbar = Snackbar.make(viewContainer, message, Snackbar.LENGTH_LONG);
        Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) snackbar.getView();
        TextView tv = (TextView) Stream.of(GuiUtils.getAllChildren(snackbarLayout)).filter(v -> v instanceof TextView).findFirst().get();
        tv.setTextColor(textColor);
        snackbarLayout.setBackgroundColor(backgroundColor);
        return snackbar;
    }

    public static void showSnackbar(View viewContainer, @StringRes int message, int textColor, int backgroundColor) {
        showSnackbar(viewContainer, viewContainer.getContext().getString(message), textColor, backgroundColor);
    }

    public static void showSnackbar(View viewContainer, @StringRes int message) {
        showSnackbar(viewContainer, viewContainer.getContext().getString(message));
    }

    public static void showSnackbar(View viewContainer, CharSequence message) {
        makeThemeSnackbar(viewContainer, message).show();
    }

    public static Snackbar makeThemeSnackbar(View viewContainer, CharSequence message) {
        int defaultBackgroundResource = android.R.attr.windowBackground;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            defaultBackgroundResource = android.R.attr.colorPrimary;
        }
        return makeCustomSnackbar(viewContainer, message, GuiUtils.getThemeColor(viewContainer.getContext(), textColor), GuiUtils.getThemeColor(viewContainer.getContext(), defaultBackgroundResource));
    }

    public static String getText(TextView textView) throws EmptyTextException {
        String text = textView.getText().toString();
        if (text == null || text.isEmpty()) {
            throw new EmptyTextException();
        }
        return text;
    }

    public static void setText(RecyclerView.ViewHolder holder, @IdRes int textViewId, CharSequence text) {
        setText((ViewGroup) holder.itemView, textViewId, text);
    }

    public static void setText(ViewGroup root, @IdRes int textViewId, CharSequence text) {
        TextView textView = getView(root, textViewId);
        if (textView != null) {
            if (text != null) {
                textView.setText(text);
            }
        }
    }

    public static void setText(View textView, CharSequence text) {
        if (textView != null) {
            if (text != null) {
                if (textView instanceof TextView) {
                    ((TextView) textView).setText(text);
                }
            }
        }
    }

    public static String setText(TextView textView, Locale locale, @StringRes int format, Object... args) {
        if (textView != null) {
            if (args != null) {
                String formatted = String.format(locale, textView.getContext().getString(format), args);
                DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(locale);
                if (decimalSeparator != null) {
                    formatted = formatted.replace(String.valueOf(symbols.getDecimalSeparator()), decimalSeparator);
                }
                if (thousandSeparator != null) {
                    Pattern pattern = Pattern.compile("\\d{3}(?=\\d)");
                    Matcher digitsMatcher = pattern.matcher(formatted);
                    StringBuffer buffer = new StringBuffer();
                    while (digitsMatcher.find()) {
                        digitsMatcher.appendReplacement(buffer, digitsMatcher.group() + thousandSeparator);
                    }
                    digitsMatcher.appendTail(buffer);
                    formatted = buffer.toString();
                }
                ((TextView) textView).setText(formatted);
                return formatted;
            }
        }
        return null;
    }

    public static String setText(ViewGroup root, @IdRes int textViewId, @StringRes int format, Object... args) {
        TextView textView = getView(root, textViewId);
        if (textView != null) {
            if (args.length == 1) {
                return setText(textView, format, new Object[]{args[0]});
            } else {
                return setText(textView, format, args);
            }
        }
        return null;
    }

    public static String setText(TextView textView, @StringRes int format, Object... args) {
        return setText(textView, Locale.getDefault(), format, args);
    }

    public static void setVisibility(int code, View... views) {
        for (View view : views) {
            view.setVisibility(code);
        }
    }

    public static void setVisibility(int code, ViewGroup viewGroup , @IdRes int ... ids) {
        for (int id : ids) {
            View view = viewGroup.findViewById(id);
            if(view != null) {
                view.setVisibility(code);
            }
        }
    }

    public interface OnTextState {
        void onTextState(View view, boolean isSetText);
    }

    public static void setTextOrHide(View textView, CharSequence text, View... views) {
        if (views == null || views.length == 0) {
            views = new View[]{textView};
        } else {
            views = SystemUtils.concat(new View[]{textView}, views);
        }
        if (textView != null) {
            if (!TextUtils.isEmpty(text)) {
                if (textView instanceof TextView) {
                    ((TextView) textView).setText(text);
                    setVisibility(View.VISIBLE, views);
                }
            } else {
                setVisibility(View.GONE, views);
            }
        }
    }

    public static void setTextAnd(View textView, CharSequence text, OnTextState onTextState,  View... views) {
        if (views == null || views.length == 0) {
            views = new View[]{textView};
        } else {
            views = SystemUtils.concat(new View[]{textView}, views);
        }
        boolean isSetText = false;
        if (textView != null) {
            if (!TextUtils.isEmpty(text)) {
                if (textView instanceof TextView) {
                    ((TextView) textView).setText(text);
                     isSetText = true;
                }
            } else {
                setVisibility(View.GONE, views);
            }
        }
        if(onTextState != null) {
            for (View view : views) {
                onTextState.onTextState(view, isSetText);
            }
        }
    }

    public static void stripUnderlines(TextView textView) {
        Spannable s = (Spannable) textView.getText();
        URLSpan[] spans = s.getSpans(0, s.length(), URLSpan.class);
        for (URLSpan span : spans) {
            int start = s.getSpanStart(span);
            int end = s.getSpanEnd(span);
            s.removeSpan(span);
            span = new URLSpanNoUnderline(span.getURL());
            s.setSpan(span, start, end, 0);
        }
    }

    public static StateListDrawable createStateList(int[][] stateSet, int[] colors) {
        StateListDrawable stateListDrawable = new StateListDrawable();
        for (int i = 0; i < colors.length && i < stateSet.length; i++) {
            addStateToStateList(stateListDrawable, colors[i], stateSet[i]);
        }
        return stateListDrawable;
    }

    public static void addStateToStateList(StateListDrawable stateListDrawable, int color, int[] stateSet) {
        Rect rect = new Rect(0, 0, 1, 1);

        Bitmap bitmap = Bitmap.createBitmap(rect.width(), rect.height(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paintPressed = new Paint();
        paintPressed.setColor(color);
        canvas.drawRect(rect, paintPressed);
        RectF bounds = new RectF();
        bounds.round(rect);

        stateListDrawable.addState(stateSet, new BitmapDrawable(bitmap));
    }

    public static SpannableStringBuilder coloredText(Context context, CharSequence text, @ColorRes int colorRes) {
        int color = context.getResources().getColor(colorRes);
        return spannableText(text, new ForegroundColorSpan(color));
    }

    public static SpannableStringBuilder coloredText(Context context, CharSequence text, int from, int to, @ColorRes int colorRes) {
        int color = context.getResources().getColor(colorRes);
        return spannableText(text, new ForegroundColorSpan(color), from, to);
    }

    public static void colorText(TextView textView, int from, int to, @ColorRes int colorRes) {
        int color = textView.getContext().getResources().getColor(colorRes);
        textView.setText(spannableText(textView.getText().toString(), new ForegroundColorSpan(color), from, to));
    }

    public static SpannableStringBuilder appendSpannableText(SpannableStringBuilder stringBuilder, CharSequence text, ParcelableSpan ... spans) {
        if (text != null) {
            int start = stringBuilder.length();
            stringBuilder.append(text);
            if(spans != null && spans.length > 0) {
                for (ParcelableSpan span : spans) {
                    stringBuilder.setSpan(span, start, stringBuilder.length(), 0);
                }
            }
        }
        return stringBuilder;
    }


    /**
     * @param stringBuilder builder
     * @param drawable image
     * @param verticalAlignment one of {@link DynamicDrawableSpan#ALIGN_BOTTOM} or
     * {@link DynamicDrawableSpan#ALIGN_BASELINE}.
     * @return
     */
    public static SpannableStringBuilder appendDrawableSpan(SpannableStringBuilder stringBuilder, Drawable drawable, int verticalAlignment) {
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        ImageSpan span = new ImageSpan(drawable, verticalAlignment);
        int start = stringBuilder.length();
        stringBuilder.append("\uFFFC");
        stringBuilder.setSpan(span, start, stringBuilder.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        return stringBuilder;
    }

    public static SpannableStringBuilder spannableText(CharSequence text, ParcelableSpan span, int from, int to) {
        SpannableStringBuilder sb = new SpannableStringBuilder(text);
        if (span != null) {
            sb.setSpan(span, from, to, 0);
        }
        return sb;
    }

    public static SpannableStringBuilder spannableText(CharSequence text, ParcelableSpan span) {
        SpannableStringBuilder sb = new SpannableStringBuilder(text);
        if (span != null) {
            sb.setSpan(span, 0, sb.length(), 0);
        }
        return sb;
    }

    public static void selectText(TextView textView, boolean erase, int start, int end, int color) {
        if (textView == null) {
            return;
        }
        Spannable raw = new SpannableString(textView.getText());
        if(erase) {
            BackgroundColorSpan[] spans = raw.getSpans(0, raw.length(), BackgroundColorSpan.class);
            if (spans != null && spans.length > 0) {
                for (BackgroundColorSpan span : spans) {
                    raw.removeSpan(span);
                }
            }
        }
        if (end > raw.length()) {
            end = raw.length();
        }
        if(end < start) {
            return;
        }
        raw.setSpan(new BackgroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(raw, TextView.BufferType.SPANNABLE);
    }

    public static void selectText(TextView textView, boolean erase, String query, int color) {
        if (textView == null) {
            return;
        }
        if (query != null) {
            query = query.trim().toLowerCase();
        }

        Spannable raw = new SpannableString(textView.getText());

        if (erase) {
            BackgroundColorSpan[] spans = raw.getSpans(0,
                    raw.length(),
                    BackgroundColorSpan.class);

            if (spans != null && spans.length > 0) {
                for (BackgroundColorSpan span : spans) {
                    raw.removeSpan(span);
                }
                if (query == null) {
                    textView.setText(raw);
                    return;
                }
            }
        }

        if (query == null) {
            return;
        }

        if (query.isEmpty()) {
            raw.setSpan(new BackgroundColorSpan(color), 0, raw.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            int index = TextUtils.indexOf(raw.toString().toLowerCase(), query);
            while (index >= 0) {
                raw.setSpan(new BackgroundColorSpan(color), index, index + query.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                index = TextUtils.indexOf(raw.toString().toLowerCase(), query, index + query.length());
            }
        }
        textView.setText(raw);
    }

    public static int getThemeColor(Context context, @AttrRes int id) {
        TypedArray a = getAttr(context, id);
        int result = a.getColor(0, 0);
        a.recycle();
        return result;
    }

    public static Drawable getThemeDrawable(Context context, @AttrRes int id) {
        TypedArray a = getAttr(context, id);
        Drawable result = a.getDrawable(0);
        a.recycle();
        return result;
    }

    public static @IdRes int getThemeResource(Context context, @AttrRes int id) {
        TypedArray a = getAttr(context, id);
        int result = a.getResourceId(0, 0);
        a.recycle();
        return result;
    }

    public static float getThemeDimen(Context context, int id) {
        TypedArray a = getAttr(context, id);
        float result = a.getDimension(0, 0);
        a.recycle();
        return result;
    }

    public static TypedArray getAttr(Context context, int id) {
        Resources.Theme theme = context.getTheme();
        return theme.obtainStyledAttributes(new int[]{id});
    }

    public static TypedArray getStyledAttrs(Context context) {
        int[] attrs = {android.R.attr.textColor,
                android.R.attr.textSize,
                android.R.attr.background,
                android.R.attr.textStyle,
                android.R.attr.textAppearance,
                android.R.attr.textColorLink,
                android.R.attr.orientation,
                android.R.attr.text};
        return context.obtainStyledAttributes(attrs);
    }

    public static List<Integer> getTypedArray(Context context, @ArrayRes int arrayId) {
        ArrayList<Integer> array = new ArrayList<Integer>();
        TypedArray typedArray = context.getResources().obtainTypedArray(arrayId);
        for (int i = 0; i < typedArray.length(); i++) {
            array.add(typedArray.getResourceId(i, 0));
        }
        typedArray.recycle();
        return array;
    }

    public static int getDarkerColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f; // value component
        return Color.HSVToColor(hsv);
    }

    public static boolean isDarkColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        float brightness = hsv[2];
        return brightness < 0.20;
    }

    public static boolean isBrightColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        float brightness = hsv[2];
        return brightness > 0.93;
    }

    public interface RunUIThread {
        void run(Object... var);
    }

    public static void runInUI(final Context context, final RunUIThread uiThread, final Object... var) {
        if (context != null) {
            Handler mainHandler = new Handler(context.getMainLooper());
            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    uiThread.run(var);
                }
            };
            mainHandler.postAtFrontOfQueue(myRunnable);
        } else {
            Log.e(TAG, "Context is Null in method: runInUI");
        }
    }

}
