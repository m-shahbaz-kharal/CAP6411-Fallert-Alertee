package com.cap6411.fallert_alertee.devices;

import android.content.Context;
import android.util.Pair;
import android.widget.ListView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ServerDevices {
    private List<ServerDevice> mDevices;
    private ServerListAdapter mAdapter;

    public ServerDevices(Context context, ListView listView, String clientIP, Consumer<Pair<String,String>> onDeviceDelete) {
        mDevices = new ArrayList<>();
        mAdapter = new ServerListAdapter(context, (ArrayList<ServerDevice>) mDevices, clientIP, onDeviceDelete);
        listView.setAdapter(mAdapter);
    }

    public List<ServerDevice> getDevices() {
        return mDevices;
    }

    public void addDevice(String title, String lastIP) {
        ServerDevice device = new ServerDevice();
        device.mTitle = title;
        device.mLastIP = lastIP;
        mDevices.add(device);
        mAdapter.notifyDataSetChanged();
    }

    public void updateDevice(String title, String lastIP) {
        for (ServerDevice device : mDevices) {
            if (device.mLastIP.equals(lastIP)) {
                device.mTitle = title;
                break;
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    public void removeDevice(String ipAddress) {
        for (ServerDevice device : mDevices) {
            if (device.mLastIP.equals(ipAddress)) {
                mDevices.remove(device);
                break;
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    public void _parse(String comma_divided_server_string) {
        String[] deviceInfo = comma_divided_server_string.split(",");
        addDevice(deviceInfo[0], deviceInfo[1]);
    }

    public void parse(String bar_divided_servers_string) {
        if(bar_divided_servers_string == null) return;
        String[] deviceInfo = bar_divided_servers_string.split("\\|");
        for (String device : deviceInfo) {
            _parse(device);
        }
    }
    @NotNull
    @Override
    public String toString() {
        String deviceString = "";
        for (ServerDevice device : mDevices) {
            deviceString += device.mTitle + "," + device.mLastIP + "|";
        }
        return deviceString;
    }
}