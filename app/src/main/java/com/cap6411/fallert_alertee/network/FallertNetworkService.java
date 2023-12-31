package com.cap6411.fallert_alertee.network;

import android.util.Pair;

import java.net.Socket;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;

public class FallertNetworkService {
    public static final int SERVER_BROADCAST_PORT = 3255;
    public static final int SERVER_RECV_PORT = 3256;
    private Dictionary<String, Thread> mReceiverThreads = new Hashtable<>();
    public static Queue<FallertEvent> mEventQueue = new LinkedList<>();
    public static boolean mIsClientProcessing = false;

    public FallertNetworkService(){}
    public void startClientThread(String serverIPAddress) {
        mReceiverThreads.put(serverIPAddress, new Thread(() -> {
            Socket mSocket = StringNetwork.establishConnection(serverIPAddress, SERVER_BROADCAST_PORT);
            if (mSocket == null) return;
            try {
                while (true) {
                    String msgString = StringNetwork.receiveString(mSocket);
                    if (msgString == null) throw new Exception("NULL msgString");
                    String eventType = msgString.split(":")[0];
                    switch (eventType) {
                        case "FALL":
                            if(!FallertNetworkService.mIsClientProcessing) {
                                FallertEventFall fallEvent = FallertEventFall.parse(msgString);
                                if (fallEvent != null) mEventQueue.add(fallEvent);
                                FallertNetworkService.mIsClientProcessing = true;
                            }
                            break;
                        case "INFORMATION":
                            FallertInformationEvent infoEvent = FallertInformationEvent.parse(msgString);
                            if (infoEvent != null) mEventQueue.add(infoEvent);
                            break;
                        case "REMOVE_DEVICE":
                            FallertRemoveDeviceEvent removeEvent = FallertRemoveDeviceEvent.parse(msgString);
                            if (removeEvent != null) mEventQueue.add(removeEvent);
                            break;
                    }

                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                StringNetwork.closeConnection(mSocket);
            }
        }));
        mReceiverThreads.get(serverIPAddress).start();
    }

    public void removeServer(Pair<String, String> server_and_client_IP) {
        mReceiverThreads.get(server_and_client_IP.first).interrupt();
        FallertRemoveDeviceEvent removeEvent = new FallertRemoveDeviceEvent(String.valueOf(System.currentTimeMillis()), server_and_client_IP.second);
        sendSingleEventToServer(server_and_client_IP.first, removeEvent);
    }

    public void sendSingleEventToServer(String serverIP, FallertEvent event) {
        new Thread(() -> {
            Socket mSocket = StringNetwork.establishConnection(serverIP, SERVER_RECV_PORT);
            if (mSocket == null) return;
            try {
                StringNetwork.sendString(mSocket, event.toString());
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                StringNetwork.closeConnection(mSocket);
            }
        }).start();
    }

    public void stopClientThreads() {
        for(Enumeration<String> ips = mReceiverThreads.keys(); ips.hasMoreElements();) {
            String ip = ips.nextElement();
            mReceiverThreads.get(ip).interrupt();
        }
        mReceiverThreads = new Hashtable<>();
    }
}
