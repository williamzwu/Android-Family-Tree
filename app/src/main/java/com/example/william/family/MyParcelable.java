package com.example.william.family;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by william on 8/5/2017.
 */

public
class MyParcelable implements Parcelable {
    private String mData;

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mData);
    }

    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<MyParcelable> CREATOR
            = new Parcelable.Creator<MyParcelable>() {
        public MyParcelable createFromParcel(Parcel in) {
            return new MyParcelable(in);
        }

        public MyParcelable[] newArray(int size) {
            return new MyParcelable[size];
        }
    };

    private MyParcelable(Parcel in) {
        mData = in.readString();
    }

    public String getString()
    {
        return mData;
    }
}
