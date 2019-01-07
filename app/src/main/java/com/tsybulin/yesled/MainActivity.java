package com.tsybulin.yesled;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState) ;
        setContentView(R.layout.activity_main) ;

        FragmentTransaction ft = this.getFragmentManager().beginTransaction() ;
        ft.replace(R.id.activity_main_frame, new LastFragment()) ;
        ft.commit() ;
    }

    public void permissionsDidClick(View view) {
        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS) ;
        this.startActivity(intent) ;
    }

    public void notificationsDidClick(View view) {
        Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS) ;
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, this.getPackageName()) ;
        this.startActivity(intent) ;
    }

    public void preferencesDidClick(View view) {
        FragmentTransaction ft = this.getFragmentManager().beginTransaction() ;
        ft.replace(R.id.activity_main_frame, new SettingsFragment()) ;
        ft.addToBackStack(null) ;
        ft.commit() ;
    }

}
