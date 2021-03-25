// ISpeedDataListener.aidl
package com.android.aidl;

import com.android.aidl.VehicleDataCallback;

interface IVehicleData {
    void registerCallback(in VehicleDataCallback callback);
}