package com.cap6411.fallert_alertee.network;

public class FallertRemoveDeviceEvent extends FallertEvent{
    private String mIPAddress;
    public FallertRemoveDeviceEvent(String eventTime, String ipAddress) {
        super(FallertEventType.REMOVE_DEVICE, eventTime);
        mIPAddress = ipAddress;
    }
    public String getIPAddress() {
        return mIPAddress;
    }
    public String toString() {
        return super.toString() + ":" + mIPAddress;
    }
    public static FallertRemoveDeviceEvent parse(String eventString) {
        String[] eventStringArray = eventString.split(":");
        try {
            return new FallertRemoveDeviceEvent(eventStringArray[1], eventStringArray[2]);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
