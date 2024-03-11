package com.cool.devskytask.br;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cool.devskytask.services.ScreenCaptureService;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, ScreenCaptureService.class);
        context.stopService(service);
    }
}
