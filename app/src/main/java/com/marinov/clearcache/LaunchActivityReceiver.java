package com.marinov.clearcache;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LaunchActivityReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent activityIntent = new Intent(context, MainActivity.class);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(activityIntent);
    }
}