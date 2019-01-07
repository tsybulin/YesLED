package com.tsybulin.yesled;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SettingsFragment extends Fragment {
    private final List<App> allApps ;
    private final Handler handler ;
    private AppAdapter adapter ;
    private ProgressBar tvLoading ;

    public SettingsFragment() {
        allApps = new ArrayList<App>(0) ;
        HandlerThread ht = new HandlerThread("SettingFragment.HandlerThread") ;
        ht.start() ;
        handler = new Handler(ht.getLooper()) ;
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context) ;
        handler.post(new Reload()) ;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false) ;
        tvLoading = v.findViewById(R.id.fragments_settings_loading) ;

        adapter = new AppAdapter() ;
        RecyclerView recyclerView = v.findViewById(R.id.fragments_settings_recycler) ;
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity())) ;
        recyclerView.setHasFixedSize(true) ;
        recyclerView.setAdapter(adapter) ;

        v.findViewById(R.id.fragments_settings_btn_reload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.post(new Reload()) ;
            }
        }) ;

        v.findViewById(R.id.fragments_settings_btn_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Save().run() ;
            }
        }) ;

        return v ;
    }

    private class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_settings_recycler_item, parent, false) ;
            final ViewHolder vh = new ViewHolder(v, R.id.fragments_settings_recycler_item_icon, R.id.fragments_settings_recycler_item_name, R.id.fragments_settings_recycler_item_pkgname, R.id.fragments_settings_recycler_item_checked) ;
            vh.cb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    App app = allApps.get(vh.getAdapterPosition()) ;
                    app.setChecked(vh.cb.isChecked()) ;
                }
            }) ;
            return vh ;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            App app = allApps.get(position) ;
            holder.iv.setImageDrawable(app.getDrawable()) ;
            holder.nv.setText(app.getName()) ;
            holder.pv.setText(app.getPkgname()) ;
            holder.cb.setChecked(app.isChecked()) ;
        }

        @Override
        public int getItemCount() {
            tvLoading.setVisibility(allApps.size() == 0 ? View.VISIBLE : View.GONE) ;
            return allApps.size() ;
        }

        final class ViewHolder extends RecyclerView.ViewHolder {
            private ImageView iv ;
            private TextView nv ;
            private TextView pv ;
            private CheckBox cb ;

            ViewHolder(View itemView, int ivId, int nvId, int pvId, int cbId) {
                super(itemView) ;
                iv = itemView.findViewById(ivId) ;
                nv = itemView.findViewById(nvId) ;
                pv = itemView.findViewById(pvId) ;
                cb = itemView.findViewById(cbId) ;
            }
        }
    }

    private class Reload implements Runnable {
        @Override
        public void run() {
            final Context context = getContext() ;
            final List<ApplicationInfo> apps = context.getPackageManager().getInstalledApplications(0) ;
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context) ;
            final Set<String> pkgnames = prefs.getStringSet(AppConstants.PREF_PACKAGES, new HashSet<String>(0)) ;
            final List<App> appList = new ArrayList<App>(0) ;

            for (ApplicationInfo app : apps) {
                String pkgName = app.packageName ;
                String name = app.loadLabel(context.getPackageManager()).toString() ;
                Drawable icon = app.loadIcon(context.getPackageManager()) ;
                boolean checked = pkgnames.contains(pkgName) ;
                appList.add(new App(pkgName, name, icon, checked)) ;
            }

            Collections.sort(appList, new Comparator<App>() {
                @Override
                public int compare(App o1, App o2) {
                    return o1.getName().compareToIgnoreCase(o2.getName()) ;
                }
            }) ;

            allApps.clear() ;
            allApps.addAll(appList) ;

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged() ;
                }
            }) ;
        }
    }

    private class Save implements Runnable {
        @Override
        public void run() {
            final Context context = getContext() ;
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context) ;
            final Set<String> pkgnames = prefs.getStringSet(AppConstants.PREF_PACKAGES, new HashSet<String>(0)) ;
            for (App app : allApps) {
                if (app.isChecked()) {
                    pkgnames.add(app.getPkgname()) ;
                } else {
                    pkgnames.remove(app.getPkgname()) ;
                }
            }
            SharedPreferences.Editor editor = prefs.edit() ;
            editor.putStringSet(AppConstants.PREF_PACKAGES, pkgnames) ;
            editor.commit() ;

            Intent intent = new Intent(AppConstants.SETTINGS_CHANGED) ;
            context.sendBroadcast(intent) ;
        }
    }
}
