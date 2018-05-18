package ru.kazantsev.template.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;
import android.widget.ProgressBar;

import net.vrallev.android.cat.Cat;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.regex.Pattern;

/**
 * Created by Rufim on 17.05.14.
 */
public class AndroidSystemUtils {

    private static final String TAG = SystemUtils.getClassName();

    public static void copyAssets(AssetManager assetManager) {
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        for (String filename : files) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(filename);
                String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                File outFile = new File(path, filename);
                out = new FileOutputStream(outFile);
                SystemUtils.copy(in, out);
                in.close();
                in = null;
                out.flush();
                out.close();
                out = null;
            } catch (IOException e) {
                Log.w("tag", "Failed to copy asset file: " + filename, e);
            }
        }
    }

    public static void directoryCopy(File sourceLocation, File targetLocation, String regex, ProgressBar progress) throws IOException {
        if (sourceLocation == null || targetLocation == null || !sourceLocation.exists()) {
            return;
        }

        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }

            String[] children = sourceLocation.list();
            for (int i = 0; i < children.length; i++) {
                File sourceChildren = new File(sourceLocation, children[i]);
                File targetChildren = new File(targetLocation, children[i]);
                if (Pattern.matches(regex, children[i]) && !SystemUtils.isSubDirectory(sourceChildren, targetChildren)) {
                    directoryCopy(sourceChildren, targetChildren, regex, null);
                }
                if (progress != null) {
                    if (progress.getVisibility() == progress.VISIBLE) {
                        progress.setProgress((int) (((double) (i + 1) / children.length) * 100));
                    }
                }
            }
        } else if (Pattern.matches(regex, sourceLocation.getName())) {
            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(targetLocation);

            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
    }

    static public boolean hasStorage(boolean requireWriteAccess) {
        //TODO: After fix the bug,  add "if (VERBOSE)" before logging errors.
        String state = Environment.getExternalStorageState();
        Log.v(TAG, "storage state is " + state);

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            if (requireWriteAccess) {
                boolean writable = checkFsWritable();
                Log.v(TAG, "storage writable is " + writable);
                return writable;
            } else {
                return true;
            }
        } else if (!requireWriteAccess && Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    private static boolean checkFsWritable() {
        // Create a temporary file to see whether a volume is really writeable.
        // It's important not to put it in the root directory which may have a
        // limit on the number of files.
        String directoryName = Environment.getExternalStorageDirectory().toString() + "/DCIM";
        File directory = new File(directoryName);
        if (!directory.isDirectory()) {
            if (!directory.mkdirs()) {
                return false;
            }
        }
        return directory.canWrite();
    }

    public static String getDataDirectory(Context context) throws IOException {
        try {
            return context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0)
                    .applicationInfo.dataDir;
        } catch (PackageManager.NameNotFoundException nnfe) {
            throw new IOException("Cannot access to data directory", nnfe);
        }
    }

    public static void initPreferences(Context context, int resId) {
        PreferenceManager.setDefaultValues(context, resId, false);
    }

    public static SharedPreferences getDefaultPreference(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static Map<String, String> getStringPreferences(Context context, String ... prefNames) {
        SharedPreferences preferences = getDefaultPreference(context);
        Map<String, String> prefMap = new HashMap<String, String>();
        for(String name : prefNames) {
            prefMap.put(name, preferences.getString(name, ""));
        }
        return prefMap;
    }

    public static <T> T getPreference(SharedPreferences preferences, String key, T defValue) {
        T value = (T) preferences.getAll().get(key);
        if (value == null) {
            return defValue;
        }
        return value;
    }

    public static <T> T getPreference(SharedPreferences preferences, String key) {
        return getPreference(preferences, key, null);
    }

    public static <T> T getStringResPreference(Context context, @StringRes int idResString) {
        return getStringResPreference(context, idResString, null);
    }

    public static <T> T getStringResPreference(Context context, @StringRes int idResString, T defValue) {
        SharedPreferences preferences = getDefaultPreference(context);
        String key = context.getString(idResString);
        T value = (T) preferences.getAll().get(key);
        if(value == null) {
            return defValue;
        }
        return value;
    }


    public static long getMemory(Context context) {
        ActivityManager actManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        actManager.getMemoryInfo(memInfo);
        return memInfo.availMem;
    }

    public static String getInternalMemory(Context context) {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return Formatter.formatFileSize(context, availableBlocks * blockSize);
    }

    public static boolean isWifiConnectionAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo != null) {
            return activeNetworkInfo.isConnected() && activeNetworkInfo.getType() == connectivityManager.TYPE_WIFI;
        } else {
            return false;
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    public static void openFileInExtApp(Context context, File file) throws IOException {
        // Create URI
        if(file == null) return;
        Uri uri = Uri.fromFile(file);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        // Check what kind of file you are trying to open, by comparing the url with extensions.
        // When the if condition is matched, plugin sets the correct intent (mime) type,
        // so Android knew what application to use to open the file
        if (file.toString().contains(".doc") || file.toString().contains(".docx")) {
            // Word document
            intent.setDataAndType(uri, "application/msword");
        } else if (file.toString().contains(".pdf")) {
            // PDF file
            intent.setDataAndType(uri, "application/pdf");
        } else if (file.toString().contains(".ppt") || file.toString().contains(".pptx")) {
            // Powerpoint file
            intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
        } else if (file.toString().contains(".xls") || file.toString().contains(".xlsx")) {
            // Excel file
            intent.setDataAndType(uri, "application/vnd.ms-excel");
        } else if (file.toString().contains(".zip") || file.toString().contains(".rar")) {
            // WAV audio file
            intent.setDataAndType(uri, "application/x-wav");
        } else if (file.toString().contains(".rtf")) {
            // RTF file
            intent.setDataAndType(uri, "application/rtf");
        } else if (file.toString().contains(".wav") || file.toString().contains(".mp3")) {
            // WAV audio file
            intent.setDataAndType(uri, "audio/x-wav");
        } else if (file.toString().contains(".gif")) {
            // GIF file
            intent.setDataAndType(uri, "image/gif");
        } else if (file.toString().contains(".jpg") || file.toString().contains(".jpeg") || file.toString().contains(".png")) {
            // JPG file
            intent.setDataAndType(uri, "image/jpeg");
        } else if (file.toString().contains(".txt")) {
            // Text file
            intent.setDataAndType(uri, "text/plain");
        } else if (file.toString().contains(".html")) {
            // Html file
            intent.setDataAndType(uri, "text/html");
        } else if (file.toString().contains(".3gp") || file.toString().contains(".mpg") || file.toString().contains(".mpeg") || file.toString().contains(".mpe") || file.toString().contains(".mp4") || file.toString().contains(".avi")) {
            // Video files
            intent.setDataAndType(uri, "video/*");
        } else {
            //if you want you can also define the intent type for any other file

            //additionally use else clause below, to manage other unknown extensions
            //in this case, Android will show all applications installed on the device
            //so you can choose which application to use
            intent.setDataAndType(uri, "*/*");
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void shareText(Context context, String title, String subject,  String text, String mime) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType(mime);
        share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_NEW_DOCUMENT);

        // Add data to the intent, the receiving app will decide
        // what to do with it.
        share.putExtra(Intent.EXTRA_SUBJECT, subject);
        share.putExtra(Intent.EXTRA_TEXT, text);

        context.startActivity(Intent.createChooser(share, title));
    }

    public static void shareFile(Context context, String title, String subject, String text, String mime, String path) {
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType(mime);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + path));
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        context.startActivity(Intent.createChooser(sendIntent, title));
    }

    public static boolean currentVersionSupportBigNotification() {
        int sdkVersion = android.os.Build.VERSION.SDK_INT;
        if (sdkVersion >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            return true;
        }
        return false;
    }

    public static boolean currentVersionSupportLockScreenControls() {
        int sdkVersion = android.os.Build.VERSION.SDK_INT;
        if (sdkVersion >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return true;
        }
        return false;
    }

    /**
     * Check if service is running or not
     *
     * @param serviceName
     * @param context
     * @return
     */
    public static boolean isServiceRunning(String serviceName, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceName.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @SuppressLint("MissingPermission")
    public static String getDeviceId(Context context) {
        final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
        String deviceId = deviceUuid.toString();
        return deviceId;
    }


    public static void openApplicationSettings(Activity context, int requestCode) {
        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + context.getPackageName()));
        context.startActivityForResult(appSettingsIntent, requestCode);
    }

    public static int getNumberOfCores() {
        if(Build.VERSION.SDK_INT >= 17) {
            return Runtime.getRuntime().availableProcessors();
        }
        else {
            // Use saurabh64's answer
            return getNumCoresOldPhones();
        }
    }

    /**
     * Gets the number of cores available in this device, across all processors.
     * Requires: Ability to peruse the filesystem at "/sys/devices/system/cpu"
     * @return The number of cores, or 1 if failed to get result
     */
    private static int getNumCoresOldPhones() {
        //Private Class to display only CPU devices in the directory listing
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                //Check if filename is "cpu", followed by a single digit number
                if(Pattern.matches("cpu[0-9]+", pathname.getName())) {
                    return true;
                }
                return false;
            }
        }

        try {
            //Get directory containing CPU info
            File dir = new File("/sys/devices/system/cpu/");
            //Filter to only list the devices we care about
            File[] files = dir.listFiles(new CpuFilter());
            //Return the number of cores (virtual CPU devices)
            return files.length;
        } catch(Exception e) {
            //Default to return 1 core
            return 1;
        }
    }

    public static void putToBundle(Bundle bundle, String key, Object value) {
        if(bundle == null || key == null) throw new NullPointerException("bundle or key is null");
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
                return;
            case CHARSEQUENCE:
                if (arrayFlag) bundle.putCharSequenceArray(key, (CharSequence[]) value);
                else if (listFlag) bundle.putCharSequenceArrayList(key, (ArrayList<CharSequence>) value);
                else bundle.putCharSequence(key, (CharSequence) value);
                return;
            case BUNDLE:
                if (arrayFlag) break;
                bundle.putBundle(key, (Bundle) value);
                return;
            case STRING:
                if (arrayFlag) bundle.putStringArray(key, (String[]) value);
                else if (listFlag) bundle.putStringArrayList(key, (ArrayList<String>) value);
                else bundle.putString(key, (String) value);
                return;
            case CHAR:
                if (arrayFlag) bundle.putCharArray(key, (char[]) value);
                else bundle.putChar(key, (char) value);
                return;
            case BYTE:
                if (arrayFlag) bundle.putByteArray(key, (byte[]) value);
                else bundle.putByte(key, (byte) value);
                return;
            case BOOLEAN:
                if (arrayFlag) bundle.putBooleanArray(key, (boolean[]) value);
                else bundle.putBoolean(key, (boolean) value);
                return;
            case SHORT:
                if (arrayFlag) bundle.putShortArray(key, (short[]) value);
                else bundle.putShort(key, (short) value);
                return;
            case INTEGER:
                if (arrayFlag) bundle.putIntArray(key, (int[]) value);
                else bundle.putInt(key, (int) value);
                return;
            case LONG:
                if (arrayFlag) bundle.putLongArray(key, (long[]) value);
                else bundle.putLong(key, (long) value);
                return;
            case FLOAT:
                if (arrayFlag) bundle.putFloatArray(key, (float[]) value);
                else bundle.putFloat(key, (float) value);
                return;
            case DOUBLE:
                if (arrayFlag) bundle.putDoubleArray(key, (double[]) value);
                else bundle.putDouble(key, (double) value);
                return;
            case SERIALIZABLE: case ENUM:
                bundle.putSerializable(key, (Serializable) value);
                return;
            default:
                throw new IllegalArgumentException("Unsupported type " + value.getClass().getSimpleName());
        }
    }

    public static <T> T getFromBundle(Bundle bundle, String key, T defValue) {
        if(bundle == null || key == null) throw new NullPointerException("bundle or key is null");
        try {
            if(!bundle.isEmpty()) {
                Map<String, Object> args = (Map<String, Object>) ReflectionUtils.getField("mMap", bundle);
                Object val = args.get(key);
                return (T) val;
            }
        } catch (Throwable e) {
            Cat.e(e);
        }
        return defValue;
    }


    public static ClassType getArrayType(Object[] array) {
        if(array.length == 0) {
            return ClassType.cast(array.getClass().getComponentType());
        } else {
            return ClassType.cast(array[0].getClass());
        }
    }

    public static <C> SparseArray<C> asSparseArray(Collection<C> collection) {
        if(collection == null) return null;
        Iterator<C> iterator = collection.iterator();
        SparseArray<C> sparseArray = new SparseArray<>(collection.size());
        while (iterator.hasNext()) {
            sparseArray.put(sparseArray.size() - 1, iterator.next());
        }
        return sparseArray;
    }

    public static <C> List<C> asList(SparseArray<C> sparseArray) {
        if (sparseArray == null) return null;
        List<C> arrayList = new ArrayList<C>(sparseArray.size());
        for (int i = 0; i < sparseArray.size(); i++)
            arrayList.add(sparseArray.valueAt(i));
        return arrayList;
    }
}
