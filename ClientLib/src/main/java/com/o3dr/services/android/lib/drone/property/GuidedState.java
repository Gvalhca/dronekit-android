package com.o3dr.services.android.lib.drone.property;

import android.os.Parcel;
import android.os.Parcelable;

import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;

/**
 * Created by fhuya on 11/5/14.
 */
public class GuidedState implements DroneAttribute {

    public static final int STATE_UNINITIALIZED = 0;
    public static final int STATE_IDLE = 1;
    public static final int STATE_ACTIVE = 2;

    private int state;
    private LatLongAlt coordinate;
    private LatLongAlt roiPoint;

    public GuidedState() {
    }

    public GuidedState(int state, LatLongAlt coordinate) {
        this.state = state;
        this.coordinate = coordinate;
        this.roiPoint = new LatLongAlt(0, 0, 0);
    }

    public GuidedState(int state, LatLongAlt coordinate, LatLongAlt roiPoint) {
        this.state = state;
        this.coordinate = coordinate;
        this.roiPoint = roiPoint;
    }

    public boolean isActive() {
        return state == STATE_ACTIVE;
    }

    public boolean isIdle() {
        return state == STATE_IDLE;
    }

    public boolean isInitialized() {
        return state != STATE_UNINITIALIZED;
    }

    public LatLongAlt getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(LatLongAlt coordinate) {
        this.coordinate = coordinate;
    }

    public LatLongAlt getRoiPoint() {
        return roiPoint;
    }

    public boolean isRoiValid() {
        return roiPoint.getLatitude() != 0 && roiPoint.getLongitude() != 0;
    }

    public void setRoiPoint(LatLongAlt roiPoint) {
        this.roiPoint = roiPoint;
    }

    public void setState(int state) {
        this.state = state;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.state);
        dest.writeParcelable(this.coordinate, flags);
        dest.writeParcelable(this.roiPoint, flags);
    }

    private GuidedState(Parcel in) {
        this.state = in.readInt();
        this.coordinate = in.readParcelable(LatLongAlt.class.getClassLoader());
        this.roiPoint = in.readParcelable(LatLongAlt.class.getClassLoader());
    }

    public static final Parcelable.Creator<GuidedState> CREATOR = new Parcelable.Creator<GuidedState>() {
        public GuidedState createFromParcel(Parcel source) {
            return new GuidedState(source);
        }

        public GuidedState[] newArray(int size) {
            return new GuidedState[size];
        }
    };
}
