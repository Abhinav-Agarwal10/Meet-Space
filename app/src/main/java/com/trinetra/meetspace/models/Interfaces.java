package com.trinetra.meetspace.models;

import android.webkit.JavascriptInterface;

import com.trinetra.meetspace.CallActivity;

public class Interfaces {

    CallActivity callActivity;

    public Interfaces(CallActivity callActivity) {
        this.callActivity = callActivity;
    }

    @JavascriptInterface
    public void onPeerConnected()
    {
        callActivity.onPeerConnected();
    }
}
