package com.zargidi.ccar.android.PNL;


import android.app.Application;

import com.onesignal.OneSignal;

public class VISION extends Application {
  @Override
  public void onCreate() {
    super.onCreate();

    OneSignal.initWithContext(this, "e9629426-a799-4728-b811-639d3fa8fc1d");
    OneSignal.getUser().addTag("GAME", "Nuna");
  }
}