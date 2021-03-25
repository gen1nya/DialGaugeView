package com.android.aidl;

interface VehicleDataCallback {
    void onNext(in double speed, in double rmp);
}