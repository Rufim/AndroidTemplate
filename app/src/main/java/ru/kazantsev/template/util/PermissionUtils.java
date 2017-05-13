package ru.kazantsev.template.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Toast;
import ru.kazantsev.template.R;
import ru.kazantsev.template.activity.BaseActivity;

import java.util.*;

/**
 * Created by 0shad on 13.05.2017.
 */
public class PermissionUtils {

    public static int PERMISSION_REQUEST_CODE = (int) (System.currentTimeMillis() / 1000);

    private static final Map<String, List<String>> dangerousPermissions = new HashMap<>();

    static {
        dangerousPermissions.put("CALENDAR", new ArrayList<String>(Arrays.asList("READ_CALENDAR", "WRITE_CALENDAR")));
        dangerousPermissions.put("CAMERA", new ArrayList<String>(Arrays.asList("CAMERA")));
        dangerousPermissions.put("CONTACTS", new ArrayList<String>(Arrays.asList("READ_CONTACTS", "WRITE_CONTACTS", "GET_ACCOUNTS")));
        dangerousPermissions.put("LOCATION", new ArrayList<String>(Arrays.asList("ACCESS_FINE_LOCATION", "ACCESS_COARSE_LOCATION")));
        dangerousPermissions.put("MICROPHONE", new ArrayList<String>(Arrays.asList("RECORD_AUDIO")));
        dangerousPermissions.put("PHONE", new ArrayList<String>(Arrays.asList("READ_PHONE_STATE", "CALL_PHONE", "READ_CALL_LOG", "WRITE_CALL_LOG", "ADD_VOICEMAIL", "USE_SIP", "PROCESS_OUTGOING_CALLS")));
        dangerousPermissions.put("SENSORS", new ArrayList<String>(Arrays.asList("BODY_SENSORS")));
        dangerousPermissions.put("SMS", new ArrayList<String>(Arrays.asList("SEND_SMS,RECEIVE_SMS", "READ_SMS", "RECEIVE_WAP_PUSH", "RECEIVE_MMS")));
        dangerousPermissions.put("STORAGE", new ArrayList<String>(Arrays.asList("READ_EXTERNAL_STORAGE", "WRITE_EXTERNAL_STORAGE")));
    }

    public static void setPermissionRequestCode(int permissionRequestCode) {
        PERMISSION_REQUEST_CODE = permissionRequestCode;
    }

    public static List<String> getDangerousPermissions(Context context) {
        List<String> localPackagePermissions = new ArrayList<>();
        try {
            List<String> requestedPermissions = Arrays.asList(context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS).requestedPermissions);
            for (Map.Entry<String, List<String>> dangerous : dangerousPermissions.entrySet()) {
                for (String dangerPermission : dangerous.getValue()) {
                    String dangerPermissionFullName = "android.permission." + dangerPermission;
                    if (requestedPermissions.contains(dangerPermissionFullName)) {
                        localPackagePermissions.add(dangerPermissionFullName);
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
        }
        return localPackagePermissions;
    }

    public static List<String> getUnhandledPermissions(Context context) {
        List<String> dangerousPermissions = getDangerousPermissions(context);
        return checkUnhandledPermissions(context, dangerousPermissions.toArray(new String[dangerousPermissions.size()]));
    }

    public static List<String> checkUnhandledPermissions(Context context, String... permissionNames) {
        List<String> unhandled = new ArrayList<>();
        int res = 0;
        for (String permissionName : permissionNames) {
            if (!isPermissionGained(context.checkCallingOrSelfPermission(permissionName))) {
                unhandled.add(permissionName);
            }
        }
        return unhandled;
    }

    public static boolean hasUnhandledPermissions(Context context) {
        return checkUnhandledPermissions(context).size() != 0;
    }

    public static boolean hasPermissions(Context context, String... permissionNames) {
        return checkUnhandledPermissions(context, permissionNames).size() == 0;
    }

    public static void requestPermissions(Activity activity, String... permissionNames) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.requestPermissions(permissionNames, PERMISSION_REQUEST_CODE);
        }
    }

    public static void requestPermissionWithRationale(Activity activity, View container, String action, String permission, String message) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            GuiUtils.makeThemeSnackbar(container, message)
                    .setAction(action, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            requestPermissions(activity, permission);
                        }
                    })
                    .show();
        } else {
            requestPermissions(activity, permission);
        }
    }

    public static List<String> getUnhandledPermissions(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) throws Exception {
        List<String> unhandled = new ArrayList<>();
        if(requestCode == PERMISSION_REQUEST_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                if(!isPermissionGained(grantResults[i])) {
                    unhandled.add(permissions[i]);
                }
            }
        } else {
            throw new Exception("Invalid request code!");
        }
        return unhandled;
    }

    public static boolean isPermissionGained(int res) {
        return res == PackageManager.PERMISSION_GRANTED;
    }

    public static void showNoStoragePermissionSnackbar(Activity activity, View container, String message, String action, String toast) {
        GuiUtils.makeThemeSnackbar(container, message)
                .setAction(action, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AndroidSystemUtils.openApplicationSettings(activity, PERMISSION_REQUEST_CODE);
                        GuiUtils.toast(activity, toast, false);
                    }
                })
                .show();
    }

}
