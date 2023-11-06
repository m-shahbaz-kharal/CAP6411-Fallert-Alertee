package com.cap6411.fallert_alertee.network;

public class FallertInformationEvent extends FallertEvent{
    private String mIPAddress;
    private String mInformation;
    public FallertInformationEvent(String eventTime, String ipAddress, String info) {
        super(FallertEventType.INFORMATION, eventTime);
        mIPAddress = ipAddress;
        mInformation = info;
    }
    public String getIPAddress() {
        return mIPAddress;
    }
    public String getInformation() {
        return mInformation;
    }
    public String toString() {
        return super.toString() + ":" + mIPAddress + ":" + mInformation;
    }
    public static FallertInformationEvent parse(String eventString) {
        String[] eventStringArray = eventString.split(":");
        try {
            return new FallertInformationEvent(eventStringArray[1], eventStringArray[2], eventStringArray[3]);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
