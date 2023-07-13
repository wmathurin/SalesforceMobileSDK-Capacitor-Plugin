package com.salesforce.mobilesdk.capacitor;

import android.util.Log;

public class CapacitorPlugin {

    public String echo(String value) {
        Log.i("Echo", value);
        return value;
    }
}
