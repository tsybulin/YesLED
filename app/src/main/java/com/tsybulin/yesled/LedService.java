package com.tsybulin.yesled;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Icon;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class LedService extends NotificationListenerService {
    private final static int CMD_SETTINGS = 1 ;
    private final static int CMD_NOTIF_POSTED = 2 ;
    private final static int CMD_NOTIF_REMOVED = 3 ;

    private final LedHandler ledHandler ;
    private Timer ledTimer ;
    private boolean connected = false ;
    private Map<Integer, StatusBarNotification> statusBarNotificationMap ;
    private Set<String> pkgnames ;
    private final YLApp ylApp ;

    private BroadcastReceiver settingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ledHandler.sendEmptyMessage(CMD_SETTINGS) ;
        }
    } ;

    public LedService() {
        ylApp = (YLApp) getApplication() ;
        ledHandler = new LedHandler() ;
        pkgnames = new HashSet<String>(0) ;
        statusBarNotificationMap = new LinkedHashMap<Integer, StatusBarNotification>(0) ;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId) ;
        return START_STICKY ;
    }

    @Override
    public void onCreate() {
        super.onCreate() ;
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE) ;
        if (notificationManager == null) {
            return ;
        }

        Notification.Builder builder = new Notification.Builder(this, AppConstants.SERVICE_CHANNEL_ID) ;
        Notification notification = builder
                .setOngoing(true)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setSmallIcon(R.drawable.ic_service_notify)
                .setContentTitle(getString(R.string.led_service))
                .setContentText(getString(R.string.running))
                .build() ;
        this.startForeground(10101, notification) ;
    }

    @Override
    public void onListenerConnected() {
        this.connected = true ;
        statusBarNotificationMap.clear() ;
        if (ledTimer == null) {
            this.registerReceiver(settingReceiver, new IntentFilter(AppConstants.SETTINGS_CHANGED)) ;
            ledHandler.sendEmptyMessageDelayed(CMD_SETTINGS, 3000L) ;
            ledTimer = new Timer("LedService.LedTimer") ;
            ledTimer.scheduleAtFixedRate(new CheckTask(), 5000L, 10000L) ;
        }
    }

    @Override
    public void onListenerDisconnected() {
        this.connected = false ;
        statusBarNotificationMap.clear() ;
        this.unregisterReceiver(settingReceiver) ;
        ledTimer.cancel() ;
        ledTimer.purge() ;
        ledTimer = null ;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String pkgName = sbn.getPackageName() ;
        if (pkgName == null) {
            return ;
        }

        if (sbn.isOngoing()) {
            return ;
        }

        if (AppConstants.IGNORED_PACKAGES.contains("#" + pkgName + "#")) {
            return ;
        }

        ledHandler.sendMessage(ledHandler.obtainMessage(CMD_NOTIF_POSTED, sbn))  ;
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        String pkgName = sbn.getPackageName() ;
//        Log.d("YesLED", "LedService.onNotificationRemoved pkgName=" + pkgName) ;
        if (pkgName == null) {
            return ;
        }

        if (sbn.isOngoing()) {
            return ;
        }

        if (pkgName.equals(getPackageName())) {
            statusBarNotificationMap.clear() ;
            return ;
        }

        if (AppConstants.IGNORED_PACKAGES.contains("#" + pkgName + "#")) {
            return ;
        }

        ledHandler.sendMessage(ledHandler.obtainMessage(CMD_NOTIF_REMOVED, sbn))  ;
    }

    private final class LedHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            this.removeMessages(msg.what) ;
            if (!connected) {
                return ;
            }

            switch (msg.what) {
                case CMD_SETTINGS:
                    this.handleSettings() ;
                    break;
                case CMD_NOTIF_POSTED:
                    this.handleNotificationPosted((StatusBarNotification) msg.obj) ;
                    break ;
                case CMD_NOTIF_REMOVED:
                    this.handleNotificationRemoved((StatusBarNotification) msg.obj) ;
                    break ;
                default:
                    break ;
            }
        }

        private void handleSettings() {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LedService.this) ;
            pkgnames = prefs.getStringSet(AppConstants.PREF_PACKAGES, new HashSet<String>(0)) ;
        }

        private void handleNotificationPosted(StatusBarNotification sbn) {
            String pkgName = sbn.getPackageName() ;

            if (AppConstants.IGNORED_PACKAGES.contains("#" + pkgName + "#")) {
                return ;
            }

            boolean checked = pkgnames.contains(pkgName) ;

            Intent intent = new Intent(AppConstants.NOTIFICATION_POSTED) ;
            intent.putExtra(AppConstants.EXTRA_NOTIFICATION_PACKAGE, pkgName) ;
            intent.putExtra(AppConstants.EXTRA_NOTIFICATION_CHECKED, checked) ;
            LedService.this.sendBroadcast(intent) ;

            if (checked) {
                statusBarNotificationMap.put(sbn.getId(), sbn) ;
//                Log.d("YesLED", "LedService.LedHandler.handleNotificationPosted nc=" + statusBarNotificationMap.size()) ;
            }
        }

        private void handleNotificationRemoved(StatusBarNotification sbn) {
            String pkgName = sbn.getPackageName() ;

            if (AppConstants.IGNORED_PACKAGES.contains("#" + pkgName + "#")) {
                return ;
            }

            boolean checked = pkgnames.contains(pkgName) ;
            Intent intent = new Intent(AppConstants.NOTIFICATION_REMOVED) ;
            intent.putExtra(AppConstants.EXTRA_NOTIFICATION_PACKAGE, pkgName) ;
            intent.putExtra(AppConstants.EXTRA_NOTIFICATION_CHECKED, checked) ;
            LedService.this.sendBroadcast(intent) ;

            if (checked) {
                statusBarNotificationMap.remove(sbn.getId()) ;
//                Log.d("YesLED", "LedService.LedHandler.handleNotificationRemoved nc=" + statusBarNotificationMap.size()) ;
            }
        }
    }

    private class CheckTask extends TimerTask {
        @Override
        public void run() {
//            Log.d("YesLED", "LedService.CheckTask.run nc=" + statusBarNotificationMap.size()) ;

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE) ;
            if (notificationManager == null) {
                return ;
            }

            if (statusBarNotificationMap.size() > 0) {
                StatusBarNotification lastNotification = null ;
                for (StatusBarNotification sbn : statusBarNotificationMap.values()) {
                    lastNotification = sbn ;
                }
                Notification.Builder builder = new Notification.Builder(LedService.this, AppConstants.APP_CHANNEL_ID) ;
                builder.setOngoing(false)
                        .setColor(lastNotification != null ? lastNotification.getNotification().color : ylApp.appNotificationColor())
                        .setCategory(lastNotification != null ? lastNotification.getNotification().category : Notification.CATEGORY_SERVICE)
                        .setContentTitle(lastNotification != null ? lastNotification.getNotification().extras.getString(Notification.EXTRA_TITLE) : "Notification")
                        .setContentText(lastNotification != null ? lastNotification.getNotification().extras.getString(Notification.EXTRA_TEXT) : "There are unread notifications")
                ;

                if (lastNotification != null) {
                    builder.setSmallIcon(lastNotification.getNotification().getSmallIcon()) ;
                    builder.setLargeIcon(lastNotification.getNotification().getLargeIcon()) ;
                } else {
                    builder.setSmallIcon(R.drawable.ic_service_notify) ;
                    builder.setLargeIcon(Icon.createWithResource(LedService.this, R.drawable.ic_notify_large)) ;
                }

                Notification notification = builder.build() ;
                notificationManager.notify(AppConstants.APP_NOTIFICATION_ID, notification) ;
            } else {
                notificationManager.cancel(AppConstants.APP_NOTIFICATION_ID) ;
            }
        }
    }
}
