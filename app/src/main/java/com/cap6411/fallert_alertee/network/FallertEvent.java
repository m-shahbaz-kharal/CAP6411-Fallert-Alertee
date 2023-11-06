package com.cap6411.fallert_alertee.network;

public class FallertEvent {
    public enum FallertEventType {
        FALL,
        INFORMATION
    }
    private FallertEventType mEventType;
    private String mEventTime;
    public FallertEvent(FallertEventType eventType, String eventTime) {
        mEventType = eventType;
        mEventTime = eventTime;
    }
    public FallertEventType getEventType() {
        return mEventType;
    }
    public String getEventTime() {
        return mEventTime;
    }
    public String toString() {
        return mEventType.toString() + ":" + mEventTime;
    }
}
