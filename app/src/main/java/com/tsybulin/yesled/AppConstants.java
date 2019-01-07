package com.tsybulin.yesled;

public interface AppConstants {
    String SETTINGS_CHANGED = "com.tsybulin.yesled.SETTINGS_CHANGED";
    String PREF_PACKAGES = "pref_packages";

    String SERVICE_CHANNEL_ID = "com.tsybulin.yesled.ls_ch_id" ;
    String SERVICE_CHANNEL_NAME = "Led Service" ;

    String NOTIFICATION_POSTED = "com.tsybulin.yesled.NOTIFICATION_POSTED" ;
    String NOTIFICATION_REMOVED = "com.tsybulin.yesled.NOTIFICATION_REMOVED" ;
    String EXTRA_NOTIFICATION_PACKAGE = "NOTIFICATION_PACKAGE" ;
    String EXTRA_NOTIFICATION_CHECKED = "NOTIFICATION_CHECKED" ;

    int APP_NOTIFICATION_ID = 20202 ;
    String APP_CHANNEL_ID = "com.tsybulin.yesled.app_ch_id" ;
    String APP_CHANNEL_NAME = "YesLED Notifications" ;

    String IGNORED_PACKAGES = "#com.tsybulin.yesled#" ;
}
