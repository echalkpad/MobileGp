package com.yuwell.mobilegp.common;

import android.app.Activity;
import android.app.Application;
import android.os.Handler;

import com.totoro.commons.Totoro;
import com.yuwell.mobilegp.BuildConfig;
import com.yuwell.mobilegp.database.DatabaseService;
import com.yuwell.mobilegp.database.DatabaseServiceImpl;

/**
 * Created by Chen on 2015/4/22.
 */
public final class GlobalContext extends Application {

    private static GlobalContext globalContext = null;

    private Activity activity = null;

    private Handler handler = new Handler();

    private static DatabaseService mDatabase = null;

    @Override
    public void onCreate() {
        super.onCreate();
        globalContext = this;

        Totoro.onCreate(this).setDebug(BuildConfig.DEBUG);
        mDatabase = DatabaseServiceImpl.getInstance();
    }

    public static GlobalContext getInstance() {
        return globalContext;
    }

    public Handler getUIHandler() {
        return handler;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public static DatabaseService getDatabase() {
        return mDatabase;
    }
}
