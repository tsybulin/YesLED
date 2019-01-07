package com.tsybulin.yesled;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

public class YLApp extends Application {
    private List<App> notifications = null ;

    private BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!intent.hasExtra(AppConstants.EXTRA_NOTIFICATION_PACKAGE)) {
                return ;
            }

            while (notifications.size() >= 20) {
                notifications.remove(0) ;
            }

            String pkgName = intent.getStringExtra(AppConstants.EXTRA_NOTIFICATION_PACKAGE) ;
            boolean checked = intent.getBooleanExtra(AppConstants.EXTRA_NOTIFICATION_CHECKED, false) ;

            boolean found = false ;
            for (App app : notifications) {
                if (pkgName.equals(app.getPkgname())) {
                    found = true ;
                    break ;
                }
            }

            if (found) {
                return ;
            }

            PackageManager pm = YLApp.this.getPackageManager() ;
            try {
                ApplicationInfo appInfo = pm.getApplicationInfo(pkgName, 0) ;
                String name = appInfo.loadLabel(pm).toString() ;
                Drawable icon = appInfo.loadIcon(pm) ;
                notifications.add(new App(pkgName, name, icon, checked)) ;
            } catch (PackageManager.NameNotFoundException e) {
                //
            }
        }
    } ;

    @Override
    public void onCreate() {
        super.onCreate() ;
        notifications = new ArrayList<App>(0) ;

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE) ;
        if (notificationManager != null) {
            NotificationChannel channel = new NotificationChannel(AppConstants.SERVICE_CHANNEL_ID, AppConstants.SERVICE_CHANNEL_NAME, NotificationManager.IMPORTANCE_NONE) ;
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE) ;
            channel.setDescription(getString(com.tsybulin.yesled.R.string.service_channel_description)) ;
            notificationManager.createNotificationChannel(channel) ;

            channel = new NotificationChannel(AppConstants.APP_CHANNEL_ID, AppConstants.APP_CHANNEL_NAME, NotificationManager.IMPORTANCE_NONE) ;
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE) ;
            channel.setDescription(getString(com.tsybulin.yesled.R.string.app_channel_description)) ;
            notificationManager.createNotificationChannel(channel) ;
        }



        IntentFilter filter = new IntentFilter(AppConstants.NOTIFICATION_POSTED) ;
        this.registerReceiver(notificationReceiver, filter) ;
    }

    public List<App> getNotifications() {
        return notifications;
    }

    public int appNotificationColor() {
        return Color.YELLOW ;
    }
}
