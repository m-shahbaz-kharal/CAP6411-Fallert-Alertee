package com.cap6411.fallert_alertee.devices;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cap6411.fallert_alertee.R;

import java.util.ArrayList;
import java.util.function.Consumer;

public class ServerListAdapter extends ArrayAdapter<ServerDevice> {
        private Context mContext;
        private Consumer<String> mOnDeviceDelete;

        public ServerListAdapter(Context context, ArrayList<ServerDevice> devices, Consumer<String> onDeviceDelete) {
            super(context, 0, devices);
            mContext = context;
            mOnDeviceDelete = onDeviceDelete;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            ServerDevice device = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.server, parent, false);
            }
            // Lookup view for data population
            TextView mTitle = (TextView) convertView.findViewById(R.id.server_name_and_ip);
            ImageView mDelete = (ImageView) convertView.findViewById(R.id.server_delete);
            // Populate the data into the template view using the data object
            mTitle.setText(device.mTitle + " | " + device.mLastIP);
            mDelete.setOnClickListener(v -> {
                remove(device);
                mOnDeviceDelete.accept(device.mLastIP);
                notifyDataSetChanged();
            });
            // Return the completed view to render on screen
            return convertView;
        }
    }