package com.xh.hotme.utils;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Keep;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;


import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Keep
public class PermissionsUtil {

    public static final int REQUEST_READ_PHONE_STATE = 0x00000001;
    public static final int REQUEST_PERMISSION_STORAGE = 0x00000002;
    public static final int REQUEST_PERMISSION = 0x00000003;

    public static final int REQUEST_PERMISSION_BLUE = 0x00000004;
    public static final int REQUEST_PERMISSION_LOCATION = 0x00000005;

    public static final int REQUEST_CAMERA = 6;
    public static final int REQUEST_EXTERNAL_READ = 7;
    public static final int REQUEST_EXTERNAL_WRITE = 8;


    public static final String[] PERMISSIONS_CAMERA = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public static final String[] PERMISSIONS_EXTERNAL_WRITE = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public static final String[] PERMISSIONS_EXTERNAL_READ = {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };


    private static Handler HANDLER = null;

    //需要申请的权限
    private static final String[] permissions = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADVERTISE
    };

    // 记录正在延迟检查的权限
    private static final Map<String, DelayCheckRunnable> _delayMap = new HashMap<>();

    /**
     * class to check permission
     */
    private static class DelayCheckRunnable implements Runnable {
        private final String _perm;
        private final WeakReference<Activity> _act;
        private final int _delay;
        private final boolean _repeat;

        public DelayCheckRunnable(Activity act, int delay, boolean repeat, String p) {
            _perm = p;
            _repeat = repeat;
            _act = new WeakReference<>(act);
            _delay = delay;

            // add to map
            _delayMap.put(p, this);
        }

        @Override
        public void run() {
            Activity act = _act.get();
            if(act != null && !act.isDestroyed() && !act.isFinishing()) {
                int checkSelfPermission = ContextCompat.checkSelfPermission(act, _perm);
                if (checkSelfPermission != PackageManager.PERMISSION_GRANTED) {
                    // if repeat, re-schedule, otherwise remove from map
                    if(_repeat) {
                        HANDLER.postDelayed(this, _delay);
                    } else {
                        _delayMap.remove(_perm);
                    }

                    // request
                    ActivityCompat.requestPermissions(act, new String[] { _perm }, 44444);
                } else {
                    // if already granted, remove from map
                    _delayMap.remove(_perm);
                }
            } else {
                // if activity is gone, remove from map
                _delayMap.remove(_perm);
            }
        }
    }

    /**
     * 延迟检查一个权限
     * @param act activity
     * @param delay 延迟时间
     * @param repeat 是否重复检查直到获得授权, 如果为true, 则会按照delay间隔不停检查
     * @param permission 权限名称
     */
    public static void delayCheckPermission(Activity act, int delay, boolean repeat, String permission) {
        // lazy create handler
        if(HANDLER == null) {
            HANDLER = new Handler(Looper.getMainLooper());
        }

        // delay must > 0
        if(delay <= 0) {
            return;
        }

        // if already in checking, remove it
        if(_delayMap.containsKey(permission)) {
            HANDLER.removeCallbacks(_delayMap.remove(permission));
        }

        // delay
        HANDLER.postDelayed(new DelayCheckRunnable(act, delay, repeat, permission), delay);
    }

    //检测权限
    public static String[] checkPermission(Context context) {
        List<String> data = new ArrayList<>();//存储未申请的权限
        for (String permission : permissions) {
            int checkSelfPermission = ContextCompat.checkSelfPermission(context, permission);
            if (checkSelfPermission != PackageManager.PERMISSION_GRANTED) {//未申请
                data.add(permission);
            }
        }
        return data.toArray(new String[data.size()]);
    }



    public static boolean checkReadStoragePermission(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        int readStoragePermissionState =
                ContextCompat.checkSelfPermission(activity, READ_EXTERNAL_STORAGE);

        boolean readStoragePermissionGranted = readStoragePermissionState == PackageManager.PERMISSION_GRANTED;

        if (!readStoragePermissionGranted) {
            ActivityCompat.requestPermissions(activity,
                    PERMISSIONS_EXTERNAL_READ,
                    REQUEST_EXTERNAL_READ);
        }
        return readStoragePermissionGranted;
    }

    public static boolean checkWriteStoragePermission(Activity context) {

        int writeStoragePermissionState =
                ContextCompat.checkSelfPermission(context, WRITE_EXTERNAL_STORAGE);

        boolean writeStoragePermissionGranted = writeStoragePermissionState == PackageManager.PERMISSION_GRANTED;

        if (!writeStoragePermissionGranted) {
            ActivityCompat.requestPermissions(context,  PERMISSIONS_EXTERNAL_WRITE,
                    REQUEST_EXTERNAL_WRITE);
        }
        return writeStoragePermissionGranted;
    }

    public static boolean checkStoragePermission(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        int readStoragePermissionState =
                ContextCompat.checkSelfPermission(activity, READ_EXTERNAL_STORAGE);

        boolean readStoragePermissionGranted = readStoragePermissionState == PackageManager.PERMISSION_GRANTED;

        if (!readStoragePermissionGranted) {
            ActivityCompat.requestPermissions(activity,
                    PERMISSIONS_EXTERNAL_READ,
                    REQUEST_EXTERNAL_READ);
        }
        return readStoragePermissionGranted;
    }

    public static boolean checkWriteStoragePermission(Fragment fragment) {

        int writeStoragePermissionState =
                ContextCompat.checkSelfPermission(fragment.getContext(), WRITE_EXTERNAL_STORAGE);

        boolean writeStoragePermissionGranted = writeStoragePermissionState == PackageManager.PERMISSION_GRANTED;

        if (!writeStoragePermissionGranted) {
            fragment.requestPermissions(PERMISSIONS_EXTERNAL_WRITE,
                    REQUEST_EXTERNAL_WRITE);
        }
        return writeStoragePermissionGranted;
    }

    public static boolean checkCameraPermission(Fragment fragment) {
        int cameraPermissionState = ContextCompat.checkSelfPermission(fragment.getContext(), CAMERA);

        boolean cameraPermissionGranted = cameraPermissionState == PackageManager.PERMISSION_GRANTED;

        if (!cameraPermissionGranted) {
            fragment.requestPermissions(PERMISSIONS_CAMERA,
                    REQUEST_CAMERA);
        }
        return cameraPermissionGranted;
    }

    public static boolean hasPermission(Context ctx, String perm) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        int state = ContextCompat.checkSelfPermission(ctx, perm);
        return state == PackageManager.PERMISSION_GRANTED;
    }

    // 蓝牙权限
    public static boolean requestBlePermission(Activity context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN)
                    != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(context, Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(context,
                        new String[]{
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.BLUETOOTH_CONNECT,
                                Manifest.permission.BLUETOOTH_SCAN,
                                Manifest.permission.BLUETOOTH_ADVERTISE
                        },
                        REQUEST_PERMISSION_BLUE);
                return true;
            }
        } else {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(context,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_PERMISSION_LOCATION);
                return true;
            }
        }

        return false;
    }


    // 蓝牙权限
    public static boolean hasBlePermission(Activity context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN)
                    != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(context, Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED
            ) {
                return false;
            }
        } else {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(context,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_PERMISSION_LOCATION);
                return false;
            }
        }

        return true;
    }
}
