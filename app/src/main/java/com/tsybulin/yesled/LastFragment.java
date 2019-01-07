package com.tsybulin.yesled;


import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Set;

public class LastFragment extends Fragment {
    private final Handler handler ;
    private AppAdapter adapter ;
    private TextView tvEmpty ;

    private BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged() ;
                }
            }, 1000L) ;
        }
    } ;

    public LastFragment() {
        handler = new Handler() ;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context) ;
        IntentFilter filter = new IntentFilter(AppConstants.NOTIFICATION_POSTED) ;
        context.registerReceiver(notificationReceiver, filter) ;
    }

    @Override
    public void onDetach() {
        getActivity().unregisterReceiver(notificationReceiver) ;
        super.onDetach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_last, container, false) ;
        tvEmpty = v.findViewById(R.id.fragments_last_empty) ;

        adapter = new AppAdapter() ;
        RecyclerView recyclerView = v.findViewById(R.id.fragments_last_recycler) ;
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity())) ;
        recyclerView.setHasFixedSize(true) ;
        recyclerView.setAdapter(adapter) ;

        return v ;
    }

    private class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_last_recycler_item, parent, false) ;
            final ViewHolder vh = new ViewHolder(v, R.id.fragments_last_recycler_item_icon, R.id.fragments_last_recycler_item_name, R.id.fragments_last_recycler_item_pkgname, R.id.fragments_last_recycler_item_checked) ;
            vh.cb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final App app = ((YLApp) getActivity().getApplication()).getNotifications().get(vh.getAdapterPosition()) ;
                    app.setChecked(vh.cb.isChecked()) ;

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext()) ;
                            Set<String> pkgnames = prefs.getStringSet(AppConstants.PREF_PACKAGES, new HashSet<String>(0)) ;
                            if (app.isChecked()) {
                                pkgnames.add(app.getPkgname()) ;
                            } else {
                                pkgnames.remove(app.getPkgname()) ;
                            }

                            SharedPreferences.Editor editor = prefs.edit() ;
                            editor.putStringSet(AppConstants.PREF_PACKAGES, pkgnames) ;
                            editor.commit() ;

                            Intent intent = new Intent(AppConstants.SETTINGS_CHANGED) ;
                            getContext().sendBroadcast(intent) ;
                        }
                    }) ;
                }
            }) ;
            return vh ;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            App app = ((YLApp) getActivity().getApplication()).getNotifications().get(position) ;
            holder.iv.setImageDrawable(app.getDrawable()) ;
            holder.nv.setText(app.getName()) ;
            holder.pv.setText(app.getPkgname()) ;
            holder.cb.setChecked(app.isChecked()) ;
        }

        @Override
        public int getItemCount() {
            int count = ((YLApp) getActivity().getApplication()).getNotifications().size() ;
            tvEmpty.setVisibility(count == 0 ? View.VISIBLE : View.GONE) ;
            return count ;
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
}
