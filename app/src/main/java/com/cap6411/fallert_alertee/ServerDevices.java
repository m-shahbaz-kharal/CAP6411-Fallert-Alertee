package com.cap6411.fallert_alertee;

import android.content.Context;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class ServerDevices {
    private List<ServerDevice> mDevices;
    private ServerListAdapter mAdapter;

    public ServerDevices(Context context, ListView listView) {
        mDevices = new ArrayList<ServerDevice>();
        mAdapter = new ServerListAdapter(context, (ArrayList<ServerDevice>) mDevices);
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

    public void removeDevice(String ipAddress) {
        for (ServerDevice device : mDevices) {
            if (device.mLastIP.equals(ipAddress)) {
                mDevices.remove(device);
                break;
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    public void _addViaString(String deviceString) {
        String[] deviceInfo = deviceString.split(",");
        addDevice(deviceInfo[0], deviceInfo[1]);
    }

    public void addViaBarDividedString(String deviceString) {
        if(deviceString == null) return;
        String[] deviceInfo = deviceString.split("\\|");
        for (String device : deviceInfo) {
            _addViaString(device);
        }
    }

    public String getBarDividedString() {
        String deviceString = "";
        for (ServerDevice device : mDevices) {
            deviceString += device.mTitle + "," + device.mLastIP + "|";
        }
        return deviceString;
    }
}