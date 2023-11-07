package com.cap6411.fallert_alertee.network;

import android.graphics.Bitmap;

public class FallertEventFall extends FallertEvent{
    private String mTitle = "";
    private String mDescription = "";
    private Bitmap mBitmap = null;

    public FallertEventFall(String eventTime, String title, String description, Bitmap bitmap) {
        super(FallertEventType.FALL, eventTime);
        mTitle = title;
        mDescription = description;
        mBitmap = bitmap;
    }
    public String getTitle() {
        return mTitle;
    }
    public String getDescription() {
        return mDescription;
    }
    public Bitmap getBitmap() {
        return mBitmap;
    }
    public String toString() {
        return super.toString() + ":" + mTitle + ":" + mDescription + ":" + StringNetwork.bitmapToSTring(mBitmap);
    }
    public static FallertEventFall parse(String eventString) {
        String[] eventStringArray = eventString.split(":");
        if (eventStringArray.length != 5) return null;
        return new FallertEventFall(eventStringArray[1], eventStringArray[2], eventStringArray[3], StringNetwork.stringToBitmap(eventStringArray[4]));
    }
}
