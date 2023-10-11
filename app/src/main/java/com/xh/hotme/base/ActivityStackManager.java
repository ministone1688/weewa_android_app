package com.xh.hotme.base;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;

import com.xh.hotme.utils.AppTrace;

import java.util.Iterator;
import java.util.List;
import java.util.Stack;

public class ActivityStackManager {
    private static final String TAG = "ActivityStackManager";
    private volatile Stack<Activity> activityStack = new Stack();
    private static volatile ActivityStackManager instance;

    public ActivityStackManager() {
    }

    public static ActivityStackManager getInstance() {
        if (instance == null) {
            instance = new ActivityStackManager();
        }

        return instance;
    }

    public void popSingleActivity(Activity targetPopActivity) {
        if (targetPopActivity != null) {
            AppTrace.d("ActivityStackManager", "popActivity: " + targetPopActivity.getLocalClassName());
            this.activityStack.remove(targetPopActivity);
        }

    }

    public void popSingleActivity(Class popActivity) {
        if (null == popActivity) {
            AppTrace.d("ActivityStackManager", "cls is null");
        } else {
            Iterator var2 = this.activityStack.iterator();

            while (var2.hasNext()) {
                Activity activity = (Activity) var2.next();
                if (null != activity && activity.getClass().equals(popActivity)) {
                    activity.finish();
                }
            }

        }
    }

    public boolean hasActivity(Activity activity) {
        if (activity == null) {
            return false;
        } else {
            Iterator var2 = this.activityStack.iterator();

            Activity act;
            do {
                if (!var2.hasNext()) {
                    return false;
                }

                act = (Activity) var2.next();
            } while (!act.getClass().equals(activity.getClass()));

            return true;
        }
    }

    public boolean hasActivity(Class activity) {
        if (activity == null) {
            return false;
        } else {
            Iterator var2 = this.activityStack.iterator();

            Activity act;
            do {
                if (!var2.hasNext()) {
                    return false;
                }

                act = (Activity) var2.next();
            } while (!act.getClass().equals(activity.getClass()));

            return true;
        }
    }

    public Activity getCurrentActivity() {
        Activity activity = null;
        if (!this.activityStack.empty()) {
            activity = (Activity) this.activityStack.lastElement();
        }

        return activity;
    }

    public void pushActivity(Activity targetPushActivity) {
        AppTrace.d("ActivityStackManager", "pushActivity: " + targetPushActivity.getLocalClassName());
        this.activityStack.add(targetPushActivity);
    }

    public void popOtherActivity(Class remainActivity) {
        if (null == remainActivity) {
            AppTrace.d("ActivityStackManager", "cls is null");
        } else {
            Iterator var2 = this.activityStack.iterator();

            while (var2.hasNext()) {
                Activity activity = (Activity) var2.next();
                if (null != activity && !activity.getClass().equals(remainActivity)) {
                    activity.finish();
                }
            }

            AppTrace.d("ActivityStackManager", "activity num is : " + this.activityStack.size());
        }
    }

    public void popAllActivity() {
        while (true) {
            Activity activity = this.getCurrentActivity();
            if (activity == null) {
                AppTrace.d("ActivityStackManager", "activity num is : " + this.activityStack.size());
                return;
            }

            activity.finish();
            this.popSingleActivity(activity);
        }
    }

    public static boolean isAppRunning(Context context) {
        String packageName = context.getPackageName();
        String topActivityClassName = getTopActivityName(context);
        return packageName != null && topActivityClassName != null && topActivityClassName.startsWith(packageName);
    }

    public boolean isAppRunning() {
        return this.activityStack.size() != 0;
    }

    public static String getTopActivityName(Context context) {
        String topActivityClassName = null;
        ActivityManager activityManager = (ActivityManager) ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = activityManager.getRunningTasks(1);
        if (runningTaskInfos != null) {
            ComponentName f = ((ActivityManager.RunningTaskInfo) runningTaskInfos.get(0)).topActivity;
            topActivityClassName = f.getClassName();
        }

        return topActivityClassName;
    }
}
