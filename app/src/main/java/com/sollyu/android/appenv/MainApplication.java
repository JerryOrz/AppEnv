package com.sollyu.android.appenv;

import android.app.Application;

import com.beardedhen.androidbootstrap.TypefaceProvider;
import com.sollyu.android.appenv.helper.PhoneHelper;
import com.sollyu.android.logg.Logg;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.crashreport.CrashReport;
import com.umeng.analytics.MobclickAgent;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.log4j.Level;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * 作者: Sollyu
 * 时间: 16/10/23
 * 联系: sollyu@qq.com
 * 说明:
 */
public class MainApplication extends Application implements Thread.UncaughtExceptionHandler {

    private static MainApplication instance = null;

    @Override
    public void onCreate() {
        super.onCreate();
        MainApplication.instance = this;

        // Android-Bootstrap 图标注册
        TypefaceProvider.registerDefaultIconSets();

        Logg.init("AppEnv");
        Logg.L.setLevel(BuildConfig.DEBUG ? Level.ALL : Level.OFF);
        Thread.setDefaultUncaughtExceptionHandler(this);

        MobclickAgent.startWithConfigure(new MobclickAgent.UMAnalyticsConfig(this, "558a1cb667e58e7649000228", BuildConfig.FLAVOR));
        MobclickAgent.setCatchUncaughtExceptions(false);
        MobclickAgent.enableEncrypt(true);

        CrashReport.UserStrategy userStrategy = new CrashReport.UserStrategy(getApplicationContext());
        userStrategy.setAppChannel(BuildConfig.FLAVOR);
        userStrategy.setAppVersion(BuildConfig.VERSION_NAME);
        userStrategy.setAppPackageName(BuildConfig.APPLICATION_ID);
        CrashReport.initCrashReport(getApplicationContext(), BuildConfig.BUGLY_APPID, BuildConfig.DEBUG);

        Bugly.init(getApplicationContext(), BuildConfig.BUGLY_APPID, BuildConfig.DEBUG);
        Beta.init(getApplicationContext(), BuildConfig.DEBUG);

        MainConfig.getInstance().init();

        // 释放文件
        try {
            File releaseFile = new File(this.getFilesDir(), "phone.json");
            if (!releaseFile.exists()) {
                FileUtils.writeByteArrayToFile(releaseFile, InputToByte(getAssets().open("phone.json")));
            }

            PhoneHelper.getInstance().reload(this);
        } catch (Exception e) {
            MobclickAgent.reportError(this, e);
        }
    }

    public static byte[] InputToByte(InputStream inStream) throws IOException {
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[]                buff       = new byte[100];
        int                   rc         = 0;
        while ((rc = inStream.read(buff, 0, 100)) > 0) {
            swapStream.write(buff, 0, rc);
        }
        return swapStream.toByteArray();
    }

    public synchronized static MainApplication getInstance() {
        return instance;
    }

    /**
     * @return 检查XPOSED是否工作
     */
    public boolean isXposedWork() {
        return false;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        Logg.L.error(e.getMessage(), e);
    }
}
