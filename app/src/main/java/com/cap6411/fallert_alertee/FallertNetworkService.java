package com.cap6411.fallert_alertee;

import java.net.Socket;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;

public class FallertNetworkService {
    public static final int SERVER_BROADCAST_PORT = 3255;
    public static final int SERVER_RECEIVE_PORT = 3256;
    private Dictionary<String, Thread> mReceiverThreads = new Hashtable<>();
    public static Queue<FallertEvent> mEventQueue = new LinkedList<>();

    public FallertNetworkService(){}
    public void startClientThread(String ipAddress) {
        mReceiverThreads.put(ipAddress, new Thread(() -> {
            Socket mSocket = StringNetwork.establishConnection(ipAddress, SERVER_BROADCAST_PORT);
            if (mSocket == null) return;
            try {
                while (true) {
                    String msgString = StringNetwork.receiveString(mSocket);
                    if (msgString == null) continue;
                    String eventType = msgString.split(":")[0];
                    switch (eventType) {
                        case "FALL":
                            FallertEventFall fallEvent = FallertEventFall.parse(msgString);
                            if (fallEvent != null) mEventQueue.add(fallEvent);
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
        mReceiverThreads.get(ipAddress).start();
    }

    public void sendToServer(String msgString){
        Enumeration<String> ips = mReceiverThreads.keys();
        while(ips.hasMoreElements()){
            String ip = ips.nextElement();
            Socket mSocket = StringNetwork.establishConnection(ip, SERVER_RECEIVE_PORT);
            StringNetwork.sendString(mSocket, msgString);
            StringNetwork.closeConnection(mSocket);
        }
    }

    public void stopClientThreads() {
        Enumeration<String> ips = mReceiverThreads.keys();
        while(ips.hasMoreElements()){
            String ip = ips.nextElement();
            mReceiverThreads.get(ip).interrupt();
        }
    }
}
