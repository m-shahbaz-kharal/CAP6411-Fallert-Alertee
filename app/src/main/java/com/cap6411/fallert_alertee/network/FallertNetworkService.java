package com.cap6411.fallert_alertee.network;

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
                            FallertEventFall fallEvent = FallertEventFall.parse(msgString);
                            if (fallEvent != null) mEventQueue.add(fallEvent);
                            break;
                        case "INFORMATION":
                            FallertInformationEvent infoEvent = FallertInformationEvent.parse(msgString);
                            if (infoEvent != null) mEventQueue.add(infoEvent);
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

    public void removeServer(String serverIP) {
        mReceiverThreads.get(serverIP).interrupt();
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
