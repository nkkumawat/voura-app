package com.vuora.nkkumawat;

import android.util.Log;

import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

public class CustomSdpObserver implements SdpObserver {
    private String logTag;

    CustomSdpObserver(String logTag) {
        this.logTag = this.getClass().getCanonicalName();
        this.logTag = this.logTag + " " + logTag;
    }

    @Override
    public void onCreateSuccess(SessionDescription sessionDescription) {
        Log.d(this.logTag, "onCreateSuccess() called with: sessionDescription = [" + sessionDescription + "]");
    }

    @Override
    public void onSetSuccess() {
        Log.d(this.logTag, "onSetSuccess() called");
    }

    @Override
    public void onCreateFailure(String s) {
        Log.d(this.logTag, "onCreateFailure() called with: s = [" + s + "]");
    }

    @Override
    public void onSetFailure(String s) {
        Log.d(this.logTag, "onSetFailure() called with: s = [" + s + "]");
    }
}
