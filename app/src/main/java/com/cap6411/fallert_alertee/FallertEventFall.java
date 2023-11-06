package com.cap6411.fallert_alertee;

import android.graphics.Bitmap;

public class FallertEventFall extends FallertEvent{
    private String mTitle = "";
    private String mDescription = "";
    private Bitmap mFallImage = null;

    public FallertEventFall(FallertEventType eventType, String eventTime, String title, String description, Bitmap fallImage) {
        super(eventType, eventTime);
        mTitle = title;
        mDescription = description;
        mFallImage = fallImage;
    }
    public String getTitle() {
        return mTitle;
    }
    public String getDescription() {
        return mDescription;
    }
    public Bitmap getFallImage() {
        return mFallImage;
    }
    public String toString() {
        return super.toString() + ":" + mTitle + ":" + mDescription + ":" + StringNetwork.BitMapToString(mFallImage);
    }
    public static FallertEventFall parse(String eventString) {
        String[] eventStringArray = eventString.split(":");
        try {
            return new FallertEventFall(FallertEventType.FALL, eventStringArray[1], eventStringArray[2], eventStringArray[3], StringNetwork.StringToBitMap(eventStringArray[4]));
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
